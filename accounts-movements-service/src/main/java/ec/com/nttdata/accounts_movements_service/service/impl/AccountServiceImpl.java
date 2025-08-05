package ec.com.nttdata.accounts_movements_service.service.impl;

import ec.com.nttdata.accounts_movements_service.client.CustomerClient;
import ec.com.nttdata.accounts_movements_service.dto.account.request.AccountRequest;
import ec.com.nttdata.accounts_movements_service.dto.account.response.AccountResponse;
import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import ec.com.nttdata.accounts_movements_service.event_handler.dto.AccountBalanceDto;
import ec.com.nttdata.accounts_movements_service.event_handler.dto.MovementDto;
import ec.com.nttdata.accounts_movements_service.exception.AccountNotFoundException;
import ec.com.nttdata.accounts_movements_service.exception.CustomerNotFoundException;
import ec.com.nttdata.accounts_movements_service.mapper.AccountMapper;
import ec.com.nttdata.accounts_movements_service.model.Account;
import ec.com.nttdata.accounts_movements_service.repository.AccountRepository;
import ec.com.nttdata.accounts_movements_service.service.AccountService;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
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

}
