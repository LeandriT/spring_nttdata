package ec.com.nttdata.customer_service.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.nttdata.customer_service.config.TestSecurityConfig;
import ec.com.nttdata.customer_service.dto.request.CustomerRequest;
import ec.com.nttdata.customer_service.dto.response.CustomerResponse;
import ec.com.nttdata.customer_service.service.CustomerService;
import java.util.Collections;
import java.util.Random;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


//@WebMvcTest(CustomerController.class)
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;
    private final String path = "/customers";

    @Test
    void testGetIndex() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        when(customerService.index(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        mockMvc.perform(get(path)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testShow() throws Exception {
        Long uuid = new Random().nextLong();
        CustomerResponse customerDto = this.buildCustomerResponse();
        customerDto.setId(uuid);
        when(customerService.show(uuid)).thenReturn(customerDto);

        mockMvc.perform(get(path + "/{uuid}", uuid)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        Matchers.equalTo(uuid))); // Adjust the jsonPath as per your DTO
    }

    @Test
    void testCreate() throws Exception {
        CustomerRequest customerRequest = this.buildCustomerRequest();
        CustomerResponse customerDto = this.buildCustomerResponse();

        when(customerService.create(any(CustomerRequest.class))).thenReturn(customerDto);

        mockMvc.perform(post(path)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(
                        Matchers.equalTo(customerDto.getId()))); // Adjust the jsonPath as per your DTO
    }

    @Test
    void testUpdate() throws Exception {
        Long uuid = new Random().nextLong();
        CustomerRequest customerRequest = new CustomerRequest();
        CustomerResponse customerDto = this.buildCustomerResponse();
        customerDto.setId(uuid);
        when(customerService.update(eq(uuid), any(CustomerRequest.class))).thenReturn(customerDto);

        mockMvc.perform(put(path + "/{uuid}", uuid)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        Matchers.equalTo(customerDto.getId()))); // Adjust the jsonPath as per your DTO
    }

    @Test
    void testDelete() throws Exception {
        Long uuid = new Random().nextLong();
        doNothing().when(customerService).delete(uuid);

        mockMvc.perform(delete(path + "/{uuid}", uuid))
                .andExpect(status().isNoContent());
    }

    private CustomerRequest buildCustomerRequest() {
        CustomerRequest object = new CustomerRequest();
        object.setName("JUAN");
        object.setGender("MALE");
        object.setAge(12);
        object.setDni("1703256897");
        object.setAddress("QUITO");
        object.setPhone("");
        object.setPassword("12345");
        return object;
    }

    private CustomerResponse buildCustomerResponse() {
        CustomerResponse object = new CustomerResponse();
        object.setId(new Random().nextLong());
        object.setName("JUAN");
        object.setGender("MALE");
        object.setAge(12);
        object.setDni("1703256897");
        object.setAddress("QUITO");
        object.setPhone("");
        return object;
    }
}