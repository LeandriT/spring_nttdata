package ec.com.nttdata.accounts_movements_service.service.impl;

import ec.com.nttdata.accounts_movements_service.client.CustomerClient;
import ec.com.nttdata.accounts_movements_service.client.dto.CustomerDto;
import ec.com.nttdata.accounts_movements_service.dto.report.AccountStatementReport;
import ec.com.nttdata.accounts_movements_service.dto.report.CustomerAccountStatementReport;
import ec.com.nttdata.accounts_movements_service.dto.report.CustomerReport;
import ec.com.nttdata.accounts_movements_service.dto.report.MovementAccountStatementReport;
import ec.com.nttdata.accounts_movements_service.dto.report.PlainMovementReport;
import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import ec.com.nttdata.accounts_movements_service.exception.CustomerNotFoundException;
import ec.com.nttdata.accounts_movements_service.model.Account;
import ec.com.nttdata.accounts_movements_service.model.Movement;
import ec.com.nttdata.accounts_movements_service.repository.AccountRepository;
import ec.com.nttdata.accounts_movements_service.service.ReportService;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final AccountRepository repository;
    private final CustomerClient customerClient;
    private static final String CUSTOMER_NOT_FOUND_MESSAGE = "Customer with ID %d does not exist";

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

    @Override
    public Page<PlainMovementReport> generatePlainReport(Pageable pageable, Long customerId,
                                                         LocalDate startDate, LocalDate endDate) {
        log.info("Generating plain movement report for customer ID: {} from {} to {}",
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
            accountsPage = repository.findByCustomerIdAndStartDateAndEndDate(
                    pageable, null, startDateTime, endDateTime
            );
            Set<Long> customerIds = accountsPage.getContent().stream()
                    .map(Account::getCustomerId)
                    .collect(Collectors.toSet());
            customers = customerClient.showByIds(customerIds);
        }

        Map<Long, CustomerDto> customerMap = customers.stream()
                .collect(Collectors.toMap(CustomerDto::getId, customer -> customer));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        List<PlainMovementReport> result = new ArrayList<>();

        for (Account account : accountsPage.getContent()) {
            CustomerDto dto = customerMap.get(account.getCustomerId());
            if (dto == null) {
                log.warn("No customer DTO found for account ID: {}", account.getId());
                continue;
            }

            // Filtrar y ordenar movimientos en el rango de fechas
            Set<Movement> movimientosOrdenados = account.getMovements();
            if (movimientosOrdenados.isEmpty()) {
                continue; // o skip account
            }

            BigDecimal saldoInicial = account.getActualBalance();
            BigDecimal totalMovimientos = BigDecimal.ZERO;

            for (Movement mov : movimientosOrdenados) {
                if (mov.getMovementType() == MovementTypeEnum.DEPOSIT) {
                    totalMovimientos = totalMovimientos.add(mov.getAmount());
                } else if (mov.getMovementType() == MovementTypeEnum.WITHDRAWAL) {
                    totalMovimientos = totalMovimientos.subtract(mov.getAmount());
                }
            }

            LocalDate fechaUltimoMovimiento = movimientosOrdenados.stream().findFirst().get().getDate().toLocalDate();

            List<Movement> sortedMovements = account.getMovements()
                    .stream()
                    .sorted(Comparator.comparing(Movement::getDate).reversed())
                    .toList();
            BigDecimal lastMovement = sortedMovements.getFirst().getAmount();
            BigDecimal lastMovementWithSign =
                    sortedMovements.getFirst().getMovementType().equals(MovementTypeEnum.WITHDRAWAL)
                            ? lastMovement.negate()
                            : lastMovement;
            result.add(PlainMovementReport.builder()
                    .fecha(formatter.format(fechaUltimoMovimiento))
                    .cliente(dto.getName())
                    .numeroCuenta(account.getAccountNumber())
                    .tipo(account.getAccountType().toString())
                    .saldoInicial(saldoInicial)
                    .estado(account.getStatus())
                    .movimiento(lastMovementWithSign)
                    .saldoDisponible(totalMovimientos)
                    .build()
            );
        }

        return new PageImpl<>(result, pageable, result.size());
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
