package ec.com.nttdata.accounts_movements_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.com.nttdata.accounts_movements_service.dto.movement.request.MovementRequest;
import ec.com.nttdata.accounts_movements_service.dto.movement.response.MovementResponse;
import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import ec.com.nttdata.accounts_movements_service.exception.BalanceTypeSigNumUnavailableException;
import ec.com.nttdata.accounts_movements_service.exception.InsufficientFoundsException;
import ec.com.nttdata.accounts_movements_service.exception.MovementNotFoundException;
import ec.com.nttdata.accounts_movements_service.mapper.MovementMapper;
import ec.com.nttdata.accounts_movements_service.model.Account;
import ec.com.nttdata.accounts_movements_service.model.Movement;
import ec.com.nttdata.accounts_movements_service.producer.customer.EventAccountCustomerPublisher;
import ec.com.nttdata.accounts_movements_service.repository.MovementRepository;
import ec.com.nttdata.accounts_movements_service.service.AccountService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class MovementServiceImplTest {

    @InjectMocks
    private MovementServiceImpl service;

    @Mock
    private MovementRepository repository;
    @Mock
    private MovementMapper mapper;
    @Mock
    private AccountService accountService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private EventAccountCustomerPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private MovementRequest buildRequest(MovementTypeEnum type, BigDecimal amount) {
        MovementRequest request = new MovementRequest();
        request.setMovementType(type);
        request.setAmount(amount);
        request.setAccountId(1L);
        return request;
    }

    private Account buildAccount(BigDecimal balance) {
        Account account = new Account();
        account.setId(1L);
        account.setActualBalance(balance);
        return account;
    }

    private Movement buildMovement(Account account, BigDecimal amount) {
        Movement movement = new Movement();
        movement.setId(1L);
        movement.setAccount(account);
        movement.setAmount(amount);
        return movement;
    }

    @Test
    void shouldThrowOnNegativeDeposit() {
        MovementRequest request = buildRequest(MovementTypeEnum.DEPOSIT, BigDecimal.valueOf(-10));
        assertThrows(BalanceTypeSigNumUnavailableException.class,
                () -> service.validateTransactionType(request));
    }

    @Test
    void shouldThrowOnNegativeWithdrawal() {
        MovementRequest request = buildRequest(MovementTypeEnum.WITHDRAWAL, BigDecimal.valueOf(-5));
        assertThrows(BalanceTypeSigNumUnavailableException.class,
                () -> service.validateTransactionType(request));
    }

    @Test
    void shouldThrowOnInsufficientFunds() {
        MovementRequest request = buildRequest(MovementTypeEnum.WITHDRAWAL, BigDecimal.valueOf(150));
        Account account = buildAccount(BigDecimal.valueOf(100));
        assertThrows(InsufficientFoundsException.class,
                () -> service.validateInsufficientFounds(account, request));
    }

    @Test
    void shouldReturnMovementById() {
        Movement movement = new Movement();
        movement.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(movement));
        when(mapper.toResponse(movement)).thenReturn(new MovementResponse());

        MovementResponse response = service.show(1L);
        assertNotNull(response);
    }

    @Test
    void shouldThrowWhenMovementNotFound() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(MovementNotFoundException.class, () -> service.show(2L));
    }

    @Test
    void shouldDeleteMovement() {
        Movement movement = new Movement();
        movement.setId(3L);
        when(repository.findById(3L)).thenReturn(Optional.of(movement));
        doNothing().when(repository).delete(movement);

        service.delete(3L);
        verify(repository).delete(movement);
    }

    @Test
    void shouldReturnPageOfMovements() {
        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        Page<MovementResponse> page = service.index(PageRequest.of(0, 10));
        assertNotNull(page);
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void shouldCreateDepositMovement() {
        MovementRequest request = buildRequest(MovementTypeEnum.DEPOSIT, BigDecimal.valueOf(100));
        Account account = buildAccount(BigDecimal.valueOf(500));
        Movement movement = buildMovement(account, request.getAmount());

        when(mapper.toModel(request)).thenReturn(movement);
        when(accountService.showById(1L)).thenReturn(account);
        when(repository.save(any(Movement.class))).thenReturn(movement);
        when(mapper.toResponse(any(Movement.class))).thenReturn(new MovementResponse());

        MovementResponse response = service.create(request);

        assertNotNull(response);
        verify(repository).save(any(Movement.class));
        verify(applicationEventPublisher).publishEvent(any());
    }

    @Test
    void shouldUpdateMovement() {
        MovementRequest request = buildRequest(MovementTypeEnum.WITHDRAWAL, BigDecimal.valueOf(50));
        Account account = buildAccount(BigDecimal.valueOf(500));
        Movement movement = buildMovement(account, BigDecimal.valueOf(200));

        when(repository.findById(1L)).thenReturn(Optional.of(movement));
        when(accountService.showById(1L)).thenReturn(account);
        when(mapper.updateModel(request, movement)).thenReturn(movement);
        when(repository.save(any(Movement.class))).thenReturn(movement);
        when(mapper.toResponse(any(Movement.class))).thenReturn(new MovementResponse());

        MovementResponse response = service.update(1L, request);

        assertNotNull(response);
        verify(repository).save(movement);
    }

    @Test
    void shouldBuildTransactionCustomerDto() {
        Movement movement = new Movement();
        Account account = new Account();
        account.setCustomerId(123L);
        movement.setAccount(account);
        movement.setAmount(BigDecimal.TEN);
        movement.setMovementType(MovementTypeEnum.DEPOSIT);

        var dto = service.buildTransactionCustomerDto(movement);

        assertEquals(123L, dto.getCustomerId());
        assertEquals(BigDecimal.TEN, dto.getAmount());
        assertEquals(MovementTypeEnum.DEPOSIT, dto.getMovementType());
    }

    @Test
    void shouldBuildAccountAndSetInMovement() {
        Movement movement = new Movement();
        Account account = buildAccount(BigDecimal.valueOf(500));

        when(accountService.showById(1L)).thenReturn(account);
        service.buildAccount(movement, 1L);

        assertEquals(account, movement.getAccount());
    }

    @Test
    void shouldBuildAccountBalanceDto() {
        var dto = service.buildAccountBalanceDto(1L, BigDecimal.valueOf(123.45));
        assertEquals(1L, dto.getAccountId());
        assertEquals(BigDecimal.valueOf(123.45), dto.getBalance());
    }
}