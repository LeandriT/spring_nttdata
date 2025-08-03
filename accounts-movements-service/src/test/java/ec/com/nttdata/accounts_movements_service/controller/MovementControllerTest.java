package ec.com.nttdata.accounts_movements_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.nttdata.accounts_movements_service.dto.account.response.AccountResponse;
import ec.com.nttdata.accounts_movements_service.dto.movement.request.MovementRequest;
import ec.com.nttdata.accounts_movements_service.dto.movement.response.MovementResponse;
import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import ec.com.nttdata.accounts_movements_service.service.MovementService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MovementController.class)
class MovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovementService service;

    @Autowired
    private ObjectMapper objectMapper;

    private final String path = "/movements";

    @Test
    void testIndex() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(service.index(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        mockMvc.perform(get(path).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testShow() throws Exception {
        Long id = new Random().nextLong();
        MovementResponse dto = buildResponse();
        dto.setId(id);
        when(service.show(id)).thenReturn(dto);

        mockMvc.perform(get(path + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.equalTo(id)));
    }

    @Test
    void testCreate() throws Exception {
        MovementRequest request = buildRequest();
        MovementResponse response = buildResponse();
        when(service.create(any(MovementRequest.class))).thenReturn(response);

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(Matchers.equalTo(response.getId())));
    }

    @Test
    void testUpdate() throws Exception {
        Long id = new Random().nextLong();
        MovementRequest request = buildRequest();
        MovementResponse response = buildResponse();
        response.setId(id);
        when(service.update(eq(id), any(MovementRequest.class))).thenReturn(response);

        mockMvc.perform(put(path + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.equalTo(response.getId())));
    }

    @Test
    void testDelete() throws Exception {
        Long id = new Random().nextLong();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete(path + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    private MovementRequest buildRequest() {
        return MovementRequest.builder()
                .accountId(new Random().nextLong())
                .amount(BigDecimal.valueOf(100))
                .movementType(MovementTypeEnum.DEPOSIT)
                .date(LocalDateTime.now())
                .build();
    }

    private MovementResponse buildResponse() {
        MovementResponse dto = new MovementResponse();
        dto.setId(new Random().nextLong());
        dto.setAccount(new AccountResponse());
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setMovementType(MovementTypeEnum.DEPOSIT);
        dto.setBalance(BigDecimal.valueOf(100));
        dto.setDate(LocalDateTime.now());
        return dto;
    }
}