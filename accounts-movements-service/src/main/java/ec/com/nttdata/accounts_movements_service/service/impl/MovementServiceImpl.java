package ec.com.nttdata.accounts_movements_service.service.impl;

import ec.com.nttdata.accounts_movements_service.dto.movement.request.MovementRequest;
import ec.com.nttdata.accounts_movements_service.dto.movement.response.MovementResponse;
import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import ec.com.nttdata.accounts_movements_service.event_handler.dto.AccountBalanceDto;
import ec.com.nttdata.accounts_movements_service.exception.BalanceTypeSigNumUnavailableException;
import ec.com.nttdata.accounts_movements_service.exception.InsufficientFoundsException;
import ec.com.nttdata.accounts_movements_service.exception.MovementNotFoundException;
import ec.com.nttdata.accounts_movements_service.mapper.MovementMapper;
import ec.com.nttdata.accounts_movements_service.model.Account;
import ec.com.nttdata.accounts_movements_service.model.Movement;
import ec.com.nttdata.accounts_movements_service.producer.customer.EventAccountCustomerPublisher;
import ec.com.nttdata.accounts_movements_service.producer.customer.dto.MovementCustomerRequest;
import ec.com.nttdata.accounts_movements_service.repository.MovementRepository;
import ec.com.nttdata.accounts_movements_service.service.AccountService;
import ec.com.nttdata.accounts_movements_service.service.MovementService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService {
    private final MovementRepository repository;
    private final MovementMapper mapper;
    private final AccountService accountService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EventAccountCustomerPublisher eventAccountCustomerPublisher;

    @Override
    public MovementResponse show(Long id) {
        String message = String.format("Movement doest no exists %d", id);
        Movement entity = repository.findById(id).orElseThrow(() -> new MovementNotFoundException(message));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MovementResponse create(MovementRequest request) {
        this.validateTransactionType(request);
        Movement entity = mapper.toModel(request);
        this.buildAccount(entity, request.getAccountId());
        this.validateInsufficientFounds(entity.getAccount(), request);
        this.buildTransactionType(request, entity);
        repository.save(entity);
        AccountBalanceDto accountBalanceDto =
                this.buildAccountBalanceDto(entity.getAccount().getId(), entity.getBalance());
        if (!request.isStart()) {
            applicationEventPublisher.publishEvent(accountBalanceDto);
        }
        CompletableFuture.runAsync(
                () -> eventAccountCustomerPublisher.sendTransactionEvent(this.buildTransactionCustomerDto(entity)));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MovementResponse update(Long id, MovementRequest request) {
        this.validateTransactionType(request);
        String message = String.format("Transaction doest no exists %d", id);
        Movement entity = repository.findById(id).orElseThrow(() -> new MovementNotFoundException(message));
        this.buildAccount(entity, request.getAccountId());
        this.validateInsufficientFounds(entity.getAccount(), request);
        this.buildTransactionType(request, entity);
        entity = mapper.updateModel(request, entity);
        repository.save(entity);
        AccountBalanceDto accountBalanceDto =
                this.buildAccountBalanceDto(entity.getAccount().getId(), entity.getBalance());
        applicationEventPublisher.publishEvent(accountBalanceDto);
        return mapper.toResponse(entity);
    }

    @Override
    public void delete(Long id) {
        String message = String.format("Transaction doest no exists %d", id);
        Movement entity = repository.findById(id).orElseThrow(() -> new MovementNotFoundException(message));
        repository.delete(entity);
    }

    @Override
    public Page<MovementResponse> index(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }


    void buildAccount(Movement movement, Long accountId) {
        Account account = accountService.showById(accountId);
        movement.setAccount(account);
    }

    void validateTransactionType(MovementRequest request) {
        // Usa el método fromDisplayName para convertir el string a un Enum
        MovementTypeEnum movementType = request.getMovementType();

        if (movementType == MovementTypeEnum.DEPOSIT) {
            // Un depósito no puede tener un valor negativo
            if (request.getAmount().signum() == -1) {
                String message = String.format("El monto del DEPOSITO no puede ser negativo: %s", request.getAmount());
                throw new BalanceTypeSigNumUnavailableException(message);
            }
        } else if (movementType == MovementTypeEnum.WITHDRAWAL) {
            // Un retiro no puede tener un valor negativo
            if (request.getAmount().signum() == -1) {
                String message = String.format("El monto del RETIRO no puede ser negativo: %s", request.getAmount());
                throw new BalanceTypeSigNumUnavailableException(message);
            }
        }
    }

    void buildTransactionType(MovementRequest request, Movement entity) {
        // Usa el método fromDisplayName para convertir el string a un Enum
        MovementTypeEnum movementType = request.getMovementType();
        if (!request.isStart()) {
            if (movementType == MovementTypeEnum.DEPOSIT) {
                entity.setBalance(entity.getAccount().getActualBalance().add(request.getAmount()));
            } else if (movementType == MovementTypeEnum.WITHDRAWAL) {
                // Al hacer un retiro, el monto debe ser negativo para la operación
                entity.setBalance(entity.getAccount().getActualBalance().subtract(request.getAmount()));
            }
        } else {
            entity.setBalance(entity.getAccount().getActualBalance());
        }
    }

    AccountBalanceDto buildAccountBalanceDto(Long accountId, BigDecimal balance) {
        return new AccountBalanceDto(this, accountId, balance);
    }

    void validateInsufficientFounds(Account account, MovementRequest request) {
        BigDecimal balance = account.getActualBalance();
        // El monto a retirar es un valor positivo, por lo que no es necesario el .negate()
        BigDecimal toWithdrawal = request.getAmount();

        // Usa el método fromDisplayName para convertir el string a un Enum
        MovementTypeEnum movementType = request.getMovementType();

        if (movementType == MovementTypeEnum.WITHDRAWAL) {
            if (balance.subtract(toWithdrawal).signum() == -1) {
                String message = "Fondos insuficientes";
                throw new InsufficientFoundsException(message);
            }
        }
    }

    MovementCustomerRequest buildTransactionCustomerDto(Movement movement) {
        return MovementCustomerRequest.builder()
                .movementType(movement.getMovementType())
                .customerId(movement.getAccount().getCustomerId())
                .amount(movement.getAmount())
                .build();
    }
}
