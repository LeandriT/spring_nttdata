package ec.com.nttdata.accounts_movements_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.nttdata.accounts_movements_service.dto.account.request.AccountRequest;
import ec.com.nttdata.accounts_movements_service.dto.account.response.AccountResponse;
import ec.com.nttdata.accounts_movements_service.dto.report.AccountStatementReport;
import ec.com.nttdata.accounts_movements_service.dto.report.CustomerReport;
import ec.com.nttdata.accounts_movements_service.enums.AccountTypeEnum;
import ec.com.nttdata.accounts_movements_service.service.AccountService;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Random;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService service;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private AccountController customerController;
    String path = "/accounts";

    @Test
    void testGetIndex() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        when(service.index(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        mockMvc.perform(get(path)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testShow() throws Exception {
        Long id = new Random().nextLong();
        AccountResponse accountDto = this.buildDto();
        accountDto.setId(id);
        when(service.show(id)).thenReturn(accountDto);

        mockMvc.perform(get(path + "/{id}", id)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.equalTo(id))); // Adjust the jsonPath as per your DTO
    }

    @Test
    void testCreate() throws Exception {
        AccountRequest request = this.buildRequest();
        AccountResponse accountResponse = this.buildDto();
        when(service.create(any(AccountRequest.class))).thenReturn(accountResponse);

        mockMvc.perform(post(path)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(
                        Matchers.equalTo(accountResponse.getId()))); // Adjust the jsonPath as per your DTO
    }

    @Test
    void testUpdate() throws Exception {
        Long id = new Random().nextLong();
        AccountRequest customerRequest = new AccountRequest();
        AccountResponse dto = this.buildDto();
        dto.setId(id);
        when(service.update(eq(id), any(AccountRequest.class))).thenReturn(dto);

        mockMvc.perform(put(path + "/{id}", id)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        Matchers.equalTo(dto.getId()))); // Adjust the jsonPath as per your DTO
    }

    @Test
    void testDelete() throws Exception {
        Long id = new Random().nextLong();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete(path + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void reportV1_ReturnsAccountStatementReports() throws Exception {
        // Datos de prueba
        Long id = new Random().nextLong();
        String startDate = "2023-07-01";
        String endDate = "2023-07-31";
        Pageable pageable = Pageable.unpaged();

        CustomerReport customerReport = new CustomerReport();
        customerReport.setName("John Doe");

        AccountStatementReport accountStatementReport = AccountStatementReport.builder()
                .customer(customerReport)
                .build();

        Page<AccountStatementReport> accountStatementReports = new PageImpl<>(
                Collections.singletonList(accountStatementReport), pageable, 1);

        // Mockear la respuesta del servicio
        Mockito.when(service.accountStatementReport(any(Pageable.class), any(Long.class), any(LocalDate.class),
                        any(LocalDate.class)))
                .thenReturn(accountStatementReports);

        // Realizar la solicitud y verificar la respuesta
        mockMvc.perform(get(path + "/reports")
                        .param("customerId", id.toString())
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(accountStatementReports)));
    }

    private AccountRequest buildRequest() {
        AccountRequest object = new AccountRequest();
        object.setInitialBalance(BigDecimal.valueOf(100));
        object.setAccountNumber(new SecureRandom().nextInt(10) + "");
        object.setStatus(true);
        object.setAccountType(AccountTypeEnum.SAVINGS);
        object.setCustomerId(new Random().nextLong());
        return object;
    }

    private AccountResponse buildDto() {
        AccountResponse object = new AccountResponse();
        object.setId(new Random().nextLong());
        object.setActualBalance(BigDecimal.valueOf(100));
        object.setInitialBalance(BigDecimal.ZERO);
        object.setAccountNumber(new SecureRandom().nextInt(10) + "");
        object.setCustomerId(new Random().nextLong());
        return object;
    }
}