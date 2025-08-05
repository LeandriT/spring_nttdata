package ec.com.nttdata.accounts_movements_service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import ec.com.nttdata.accounts_movements_service.client.CustomerClient;
import ec.com.nttdata.accounts_movements_service.client.dto.CustomerDto;
import ec.com.nttdata.accounts_movements_service.dto.report.PlainMovementReport;
import ec.com.nttdata.accounts_movements_service.enums.AccountTypeEnum;
import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import ec.com.nttdata.accounts_movements_service.exception.CustomerNotFoundException;
import ec.com.nttdata.accounts_movements_service.model.Account;
import ec.com.nttdata.accounts_movements_service.model.Movement;
import ec.com.nttdata.accounts_movements_service.repository.AccountRepository;
import feign.FeignException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ReportServiceImplTest {

    @InjectMocks
    private ReportServiceImpl reportService;

    @Mock
    private AccountRepository repository;

    @Mock
    private CustomerClient customerClient;

    private Account account;
    private CustomerDto customerDto;
    private Pageable pageable;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setup() {
        account = new Account();
        account.setId(1L);
        account.setCustomerId(1L);
        account.setAccountNumber("12345");
        account.setAccountType(AccountTypeEnum.SAVINGS);
        account.setInitialBalance(BigDecimal.TEN);
        account.setActualBalance(BigDecimal.valueOf(20));
        account.setMovements(Set.of(new Movement()));

        customerDto = new CustomerDto();
        customerDto.setId(1L);
        customerDto.setName("John Doe");

        pageable = PageRequest.of(0, 10);
        startDate = LocalDate.now().minusDays(5);
        endDate = LocalDate.now();
    }

    @Test
    void accountStatementReport_withValidCustomerId_shouldReturnReport() {
        when(repository.findByCustomerIdAndStartDateAndEndDate(any(), eq(1L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(customerClient.show(1L)).thenReturn(customerDto);

        Page<?> result = reportService.accountStatementReport(pageable, 1L, startDate, endDate);

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    void accountStatementReport_withNullCustomerIdAndEmptyAccounts_shouldReturnEmpty() {
        when(repository.findByCustomerIdAndStartDateAndEndDate(any(), isNull(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        Page<?> result = reportService.accountStatementReport(pageable, null, startDate, endDate);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void accountStatementReport_withNullCustomerIdAndUnknownCustomers_shouldReturnEmpty() {
        account.setCustomerId(99L);
        account.setMovements(Set.of());
        when(repository.findByCustomerIdAndStartDateAndEndDate(any(), isNull(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(customerClient.showByIds(Set.of(99L))).thenReturn(Set.of());

        Page<?> result = reportService.accountStatementReport(pageable, null, startDate, endDate);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void accountStatementReport_withNoMatchingCustomerDto_shouldFilterOutAccount() {
        account.setCustomerId(99L);
        account.setMovements(Set.of());
        when(repository.findByCustomerIdAndStartDateAndEndDate(any(), isNull(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(customerClient.showByIds(Set.of(99L))).thenReturn(Set.of());

        Page<?> result = reportService.accountStatementReport(pageable, null, startDate, endDate);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void validateCustomerExists_shouldPass_WhenCustomerExists() throws Exception {
        when(customerClient.show(1L)).thenReturn(customerDto);
        invokePrivateMethod("validateCustomerExists", new Class[] {Long.class}, 1L);
    }

    @Test
    void validateCustomerExists_shouldThrowCustomerNotFound_WhenNotFound() {
        when(customerClient.show(1L)).thenThrow(new CustomerNotFoundException("Not found"));

        assertThatThrownBy(() -> invokePrivateMethod("validateCustomerExists", new Class[] {Long.class}, 1L))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void fetchCustomer_shouldReturnDto_WhenFound() throws Exception {
        when(customerClient.show(1L)).thenReturn(customerDto);

        CustomerDto dto = (CustomerDto) invokePrivateMethod("fetchCustomer", new Class[] {Long.class}, 1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void fetchCustomer_shouldThrow_WhenNotFound() {
        when(customerClient.show(1L)).thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> invokePrivateMethod("fetchCustomer", new Class[] {Long.class}, 1L))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void validateDateRange_shouldThrowIfStartAfterEnd() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        assertThatThrownBy(() -> invokePrivateMethod("validateDateRange",
                new Class[] {LocalDate.class, LocalDate.class}, start, end))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateDateRange_shouldThrowIfStartInFuture() {
        LocalDate future = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> invokePrivateMethod("validateDateRange",
                new Class[] {LocalDate.class, LocalDate.class}, future, future))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateDateRange_shouldPassWithValidRange() throws Exception {
        invokePrivateMethod("validateDateRange", new Class[] {LocalDate.class, LocalDate.class}, startDate, endDate);
    }

    @Test
    void generatePlainReport_withValidCustomerId_shouldReturnAggregatedReport() {
        Movement m1 = new Movement();
        m1.setAmount(BigDecimal.valueOf(100));
        m1.setMovementType(MovementTypeEnum.DEPOSIT);
        m1.setDate(startDate.atStartOfDay());

        Movement m2 = new Movement();
        m2.setAmount(BigDecimal.valueOf(50));
        m2.setMovementType(MovementTypeEnum.WITHDRAWAL);
        m2.setDate(startDate.atStartOfDay().plusDays(1));

        account.setMovements(new HashSet<>(List.of(m1, m2)));

        when(repository.findByCustomerIdAndStartDateAndEndDate(any(), eq(1L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(customerClient.show(1L)).thenReturn(customerDto);

        Page<PlainMovementReport> result = reportService.generatePlainReport(pageable, 1L, startDate, endDate);

        assertThat(result.getContent()).hasSize(1);
        PlainMovementReport report = result.getContent().get(0);
        assertThat(report.getCliente()).isEqualTo("John Doe");
        assertThat(report.getMovimiento()).isEqualTo(BigDecimal.valueOf(100)); // último movimiento (retiro) con signo
        assertThat(report.getSaldoDisponible()).isEqualTo(BigDecimal.valueOf(100)); // 100 - 50
    }

    @Test
    void generatePlainReport_withNullCustomerId_shouldReturnAggregatedReport() {
        Movement deposit = new Movement();
        deposit.setAmount(BigDecimal.valueOf(200));
        deposit.setMovementType(MovementTypeEnum.DEPOSIT);
        deposit.setDate(startDate.atStartOfDay());

        account.setMovements(Set.of(deposit));

        when(repository.findByCustomerIdAndStartDateAndEndDate(any(), isNull(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(customerClient.showByIds(Set.of(1L))).thenReturn(Set.of(customerDto));

        Page<PlainMovementReport> result = reportService.generatePlainReport(pageable, null, startDate, endDate);

        assertThat(result.getContent()).hasSize(1);
        PlainMovementReport report = result.getContent().get(0);
        assertThat(report.getMovimiento()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(report.getSaldoDisponible()).isEqualTo(BigDecimal.valueOf(200));
    }

    @Test
    void generatePlainReport_withNoCustomerDto_shouldSkipAccount() {
        Movement deposit = new Movement();
        deposit.setAmount(BigDecimal.valueOf(200));
        deposit.setMovementType(MovementTypeEnum.DEPOSIT);
        deposit.setDate(startDate.atStartOfDay());

        account.setMovements(Set.of(deposit));

        when(repository.findByCustomerIdAndStartDateAndEndDate(any(), isNull(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(customerClient.showByIds(Set.of(1L))).thenReturn(Set.of()); // sin dto

        Page<PlainMovementReport> result = reportService.generatePlainReport(pageable, null, startDate, endDate);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void generatePlainReport_withNoMovements_shouldSkipAccount() {
        account.setMovements(Set.of()); // sin movimientos

        when(repository.findByCustomerIdAndStartDateAndEndDate(any(), eq(1L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(customerClient.show(1L)).thenReturn(customerDto);

        Page<PlainMovementReport> result = reportService.generatePlainReport(pageable, 1L, startDate, endDate);

        assertThat(result.getContent()).isEmpty();
    }

    private Object invokePrivateMethod(String name, Class<?>[] params, Object... args) throws Exception {
        Method method = ReportServiceImpl.class.getDeclaredMethod(name, params);
        method.setAccessible(true);
        return method.invoke(reportService, args);
    }
}