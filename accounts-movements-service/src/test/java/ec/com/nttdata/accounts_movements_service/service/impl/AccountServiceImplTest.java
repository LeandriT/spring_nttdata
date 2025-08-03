package ec.com.nttdata.accounts_movements_service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.com.nttdata.accounts_movements_service.client.CustomerClient;
import ec.com.nttdata.accounts_movements_service.client.dto.CustomerDto;
import ec.com.nttdata.accounts_movements_service.dto.account.request.AccountRequest;
import ec.com.nttdata.accounts_movements_service.dto.account.response.AccountResponse;
import ec.com.nttdata.accounts_movements_service.dto.report.AccountStatementReport;
import ec.com.nttdata.accounts_movements_service.enums.AccountTypeEnum;
import ec.com.nttdata.accounts_movements_service.event_handler.dto.AccountBalanceDto;
import ec.com.nttdata.accounts_movements_service.exception.AccountNotFoundException;
import ec.com.nttdata.accounts_movements_service.exception.CustomerNotFoundException;
import ec.com.nttdata.accounts_movements_service.mapper.AccountMapper;
import ec.com.nttdata.accounts_movements_service.model.Account;
import ec.com.nttdata.accounts_movements_service.repository.AccountRepository;
import feign.FeignException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AccountServiceImplTest {

    private static final Long VALID_CUSTOMER_ID = 1L;
    private static final String ACCOUNT_NUMBER = "123";

    @InjectMocks
    private AccountServiceImpl service;

    @Mock
    private AccountRepository repository;

    @Mock
    private AccountMapper mapper;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private ApplicationEventPublisher publisher;

    private Account account;
    private AccountRequest request;
    private AccountResponse response;
    private CustomerDto customerDto;

    @BeforeEach
    void setUp() {
        account = buildSampleAccount();

        request = new AccountRequest();
        request.setCustomerId(VALID_CUSTOMER_ID);
        request.setAccountNumber(ACCOUNT_NUMBER);
        request.setAccountType(AccountTypeEnum.SAVINGS);
        request.setInitialBalance(BigDecimal.valueOf(100));
        request.setStatus(true);

        response = new AccountResponse();
        response.setId(1L);
        response.setCustomerId(VALID_CUSTOMER_ID);
        response.setAccountNumber(ACCOUNT_NUMBER);

        customerDto = new CustomerDto();
        customerDto.setId(VALID_CUSTOMER_ID);
        customerDto.setName("Test Name");
    }

    private Account buildSampleAccount() {
        Account acc = new Account();
        acc.setId(1L);
        acc.setCustomerId(VALID_CUSTOMER_ID);
        acc.setAccountNumber(ACCOUNT_NUMBER);
        acc.setAccountType(AccountTypeEnum.SAVINGS);
        acc.setInitialBalance(BigDecimal.valueOf(100));
        acc.setActualBalance(BigDecimal.valueOf(100));
        return acc;
    }

    private Object invokePrivateMethod(String methodName, Class<?>[] paramTypes, Object... args)
            throws Exception {
        Method method = service.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(service, args);
    }

    @Test
    @DisplayName("Should create account and publish event")
    void create_ShouldCreateAccountAndPublishDepositEvent() {
        when(customerClient.show(any())).thenReturn(customerDto);
        when(mapper.toModel(any(AccountRequest.class))).thenReturn(account);
        when(repository.save(any(Account.class))).thenReturn(account);
        when(mapper.toResponse(any(Account.class))).thenReturn(response);

        AccountResponse result = service.create(request);

        assertThat(result).isNotNull();
        verify(publisher).publishEvent(any());
        verify(repository).save(account);
    }

    @Test
    void show_ShouldReturnAccount_WhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(mapper.toResponse(account)).thenReturn(response);

        AccountResponse result = service.show(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void show_ShouldThrow_WhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.show(99L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void update_ShouldUpdateAccount_WhenValid() {
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(customerClient.show(any())).thenReturn(customerDto);
        doAnswer(inv -> {
            AccountRequest req = inv.getArgument(0);
            Account acc = inv.getArgument(1);
            acc.setAccountNumber(req.getAccountNumber());
            return null;
        }).when(mapper).updateModel(any(), any());

        when(repository.save(account)).thenReturn(account);
        when(mapper.toResponse(account)).thenReturn(response);

        AccountResponse result = service.update(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void delete_ShouldDeleteAccount_WhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(account));

        service.delete(1L);

        verify(repository).delete(account);
    }

    @Test
    void index_ShouldReturnPageOfAccounts() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Account> page = new PageImpl<>(List.of(account));
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(account)).thenReturn(response);

        Page<AccountResponse> result = service.index(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateAccountBalance_ShouldUpdateActualBalance() {
        AccountBalanceDto dto = new AccountBalanceDto(this, 1L, BigDecimal.valueOf(500));
        when(repository.findById(1L)).thenReturn(Optional.of(account));

        service.updateAccountBalance(dto);

        assertThat(account.getActualBalance()).isEqualTo(BigDecimal.valueOf(500));
        verify(repository).save(account);
    }

    @Test
    void validateCustomerExists_ShouldThrow_WhenCustomerNotFound() {
        when(customerClient.show(999L)).thenThrow(new CustomerNotFoundException("Not found"));

        assertThatThrownBy(() -> {
            request.setCustomerId(999L);
            service.create(request);
        }).isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void fetchCustomer_ShouldThrow_WhenCustomerNotFound() throws Exception {
        when(customerClient.show(99L)).thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> invokePrivateMethod("fetchCustomer",
                new Class[] {Long.class}, 99L))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void validateDateRange_ShouldThrow_WhenStartAfterEnd() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        assertThatThrownBy(() -> invokePrivateMethod("validateDateRange",
                new Class[] {LocalDate.class, LocalDate.class}, start, end))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateDateRange_ShouldAllowOldDates() throws Exception {
        LocalDate start = LocalDate.of(2024, 8, 2);
        LocalDate end = LocalDate.of(2025, 8, 2);

        invokePrivateMethod("validateDateRange", new Class[] {LocalDate.class, LocalDate.class}, start, end);
    }

    @Test
    void accountStatementReport_ShouldReturnEmptyPage_WhenNoCustomerIdsExtractedFromAccounts() {
        Pageable pageable = PageRequest.of(0, 5);
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        Account acc = buildSampleAccount();
        acc.setCustomerId(99L);
        acc.setMovements(Set.of());

        Page<Account> page = new PageImpl<>(List.of(acc));

        when(repository.findByCustomerIdAndStartDateAndEndDate(
                pageable, null, start.atStartOfDay(), end.atTime(LocalTime.MAX)))
                .thenReturn(page);

        when(customerClient.showByIds(Set.of(99L))).thenReturn(Set.of());

        Page<AccountStatementReport> result = service.accountStatementReport(pageable, null, start, end);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void accountStatementReport_ShouldReturnReports_WhenCustomerIdPresent() {
        Pageable pageable = PageRequest.of(0, 1);
        when(repository.findByCustomerIdAndStartDateAndEndDate(any(), anyLong(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(account)));

        when(customerClient.show(anyLong())).thenReturn(customerDto);

        account.setMovements(Set.of());
        Page<AccountStatementReport> result = service.accountStatementReport(pageable, 1L,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 2));

        assertThat(result.getContent()).hasSize(1);
    }
}
