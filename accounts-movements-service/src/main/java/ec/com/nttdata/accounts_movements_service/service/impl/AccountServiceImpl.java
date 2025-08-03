package ec.com.nttdata.accounts_movements_service.service.impl;

import ec.com.nttdata.accounts_movements_service.client.CustomerClient;
import ec.com.nttdata.accounts_movements_service.client.dto.CustomerDto;
import ec.com.nttdata.accounts_movements_service.dto.account.request.AccountRequest;
import ec.com.nttdata.accounts_movements_service.dto.account.response.AccountResponse;
import ec.com.nttdata.accounts_movements_service.dto.report.AccountStatementReport;
import ec.com.nttdata.accounts_movements_service.dto.report.CustomerAccountStatementReport;
import ec.com.nttdata.accounts_movements_service.dto.report.CustomerReport;
import ec.com.nttdata.accounts_movements_service.dto.report.MovementAccountStatementReport;
import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import ec.com.nttdata.accounts_movements_service.event_handler.dto.AccountBalanceDto;
import ec.com.nttdata.accounts_movements_service.event_handler.dto.MovementDto;
import ec.com.nttdata.accounts_movements_service.exception.AccountNotFoundException;
import ec.com.nttdata.accounts_movements_service.exception.CustomerNotFoundException;
import ec.com.nttdata.accounts_movements_service.mapper.AccountMapper;
import ec.com.nttdata.accounts_movements_service.model.Account;
import ec.com.nttdata.accounts_movements_service.model.Movement;
import ec.com.nttdata.accounts_movements_service.repository.AccountRepository;
import ec.com.nttdata.accounts_movements_service.service.AccountService;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account with ID %d does not exist";
    private static final String CUSTOMER_NOT_FOUND_MESSAGE = "Customer with ID %d does not exist";

    private final AccountRepository repository;
    private final AccountMapper mapper;
    private final CustomerClient customerClient;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public AccountResponse show(Long id) {
        log.debug("Fetching account with ID: {}", id);
        Account entity = findAccountById(id);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public AccountResponse create(AccountRequest request) {
        log.info("Creating new account for customer ID: {}", request.getCustomerId());

        validateCustomerExists(request.getCustomerId());

        Account entity = mapper.toModel(request);
        entity.setActualBalance(request.getInitialBalance());

        Account savedAccount = repository.save(entity);
        log.info("Account created successfully with ID: {}", savedAccount.getId());

        // 🚀 Crear movimiento de tipo DEPÓSITO con el mismo valor que el saldo inicial
        if (request.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
            MovementDto movementDto = new MovementDto(
                    this, // fuente del evento (usualmente `this`)
                    LocalDateTime.now(),
                    MovementTypeEnum.DEPOSIT,
                    request.getInitialBalance(),
                    savedAccount.getId(),
                    request.getInitialBalance(),
                    true
            );
            applicationEventPublisher.publishEvent(movementDto);
        }

        return mapper.toResponse(savedAccount);
    }

    @Override
    @Transactional
    public AccountResponse update(Long id, AccountRequest request) {
        log.info("Updating account with ID: {} for customer ID: {}", id, request.getCustomerId());

        validateCustomerExists(request.getCustomerId());
        Account entity = findAccountById(id);

        mapper.updateModel(request, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);

        log.info("Account updated successfully with ID: {}", entity.getId());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting account with ID: {}", id);
        Account entity = findAccountById(id);
        repository.delete(entity);
        log.info("Account deleted successfully with ID: {}", id);
    }

    @Override
    public Page<AccountResponse> index(Pageable pageable) {
        log.debug("Fetching accounts with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public Account showById(Long id) {
        return findAccountById(id);
    }

    @Override
    @Transactional
    public void updateAccountBalance(AccountBalanceDto accountBalanceDto) {
        log.info("Updating balance for account ID: {} to amount: {}",
                accountBalanceDto.getAccountId(), accountBalanceDto.getBalance());

        Account account = findAccountById(accountBalanceDto.getAccountId());
        account.setActualBalance(accountBalanceDto.getBalance());

        repository.save(account);
        log.info("Account balance updated successfully for account ID: {}", accountBalanceDto.getAccountId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountStatementReport> accountStatementReport(Pageable pageable, Long customerId,
                                                               LocalDate startDate, LocalDate endDate) {
        log.info("Generating account statement report for customer ID: {} from {} to {}",
                customerId, startDate, endDate);

        validateDateRange(startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Page<Account> accountsPage;
        Set<CustomerDto> customers;

        if (Objects.nonNull(customerId)) {
            validateCustomerExists(customerId);
            accountsPage = repository.findByCustomerIdAndStartDateAndEndDate(
                    pageable, customerId, startDateTime, endDateTime
            );
            customers = Set.of(fetchCustomer(customerId));
        } else {
            accountsPage =
                    repository.findByCustomerIdAndStartDateAndEndDate(pageable, null, startDateTime, endDateTime);
            Set<Long> customerIds = accountsPage.getContent().stream()
                    .map(Account::getCustomerId)
                    .collect(Collectors.toSet());
            if (customerIds.isEmpty()) {
                log.warn("No customers found for the given date range.");
                return Page.empty(pageable);
            }
            customers = customerClient.showByIds(customerIds);
        }

        if (accountsPage.isEmpty()) {
            log.warn("No accounts found for customer ID: {} in date range {} to {}",
                    customerId, startDate, endDate);
            return Page.empty(pageable);
        }

        Map<Long, CustomerDto> customerMap = customers.stream()
                .collect(Collectors.toMap(CustomerDto::getId, customer -> customer));

        List<AccountStatementReport> reports = accountsPage.getContent().stream()
                .map(account -> {
                    CustomerDto dto = customerMap.get(account.getCustomerId());
                    if (dto == null) {
                        log.warn("No customer DTO found for account ID: {}", account.getId());
                        return null;
                    }
                    return buildAccountStatementReport(dto, account);
                })
                .filter(Objects::nonNull)
                .toList();
        log.info("Account statement report generated successfully for customer ID: {}", customerId);
        return new PageImpl<>(reports, pageable, accountsPage.getTotalElements());
    }

    // Private helper methods
    private Account findAccountById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Account not found with ID: {}", id);
                    return new AccountNotFoundException(String.format(ACCOUNT_NOT_FOUND_MESSAGE, id));
                });
    }

    private void validateCustomerExists(Long customerId) {
        try {
            customerClient.show(customerId);
            log.debug("Customer validation successful for ID: {}", customerId);
        } catch (CustomerNotFoundException | FeignException.NotFound e) {
            log.error("Customer not found with ID: {}", customerId);
            throw new CustomerNotFoundException(String.format(CUSTOMER_NOT_FOUND_MESSAGE, customerId));
        } catch (FeignException e) {
            log.error("Error occurred while fetching customer with ID {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Service unavailable: Unable to validate customer", e);
        }
    }

    private CustomerDto fetchCustomer(Long customerId) {
        try {
            return customerClient.show(customerId);
        } catch (FeignException.NotFound e) {
            throw new CustomerNotFoundException(String.format(CUSTOMER_NOT_FOUND_MESSAGE, customerId));
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        LocalDate now = LocalDate.now();
        if (startDate.isAfter(now)) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }

        // Optional: Add maximum date range validation
        if (startDate.isBefore(now.minusYears(1))) {
            log.warn("Requesting data older than 1 year: {}", startDate);
        }
    }

    private AccountStatementReport buildAccountStatementReport(CustomerDto customerDto, Account account) {
        CustomerReport customerReport = buildCustomerReport(customerDto);

        Set<MovementAccountStatementReport> movementsSet = account.getMovements().stream()
                .map(this::buildMovementAccountStatementReport)
                .collect(Collectors.toSet());

        CustomerAccountStatementReport accountReport = CustomerAccountStatementReport.builder()
                .type(account.getAccountType())
                .actualBalance(account.getActualBalance())
                .number(account.getAccountNumber())
                .initialBalance(account.getInitialBalance())
                .movements(movementsSet)
                .build();

        customerReport.setAccounts(List.of(accountReport));

        return AccountStatementReport.builder()
                .customer(customerReport)
                .build();
    }

    private CustomerReport buildCustomerReport(CustomerDto dto) {
        return CustomerReport.builder()
                .name(dto.getName())
                .build();
    }

    private MovementAccountStatementReport buildMovementAccountStatementReport(Movement movement) {
        return MovementAccountStatementReport.builder()
                .date(movement.getDate())
                .balance(movement.getBalance())
                .amount(movement.getAmount())
                .movementType(movement.getMovementType())
                .build();
    }
}
