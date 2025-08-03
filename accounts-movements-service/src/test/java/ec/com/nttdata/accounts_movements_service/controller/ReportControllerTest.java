package ec.com.nttdata.accounts_movements_service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.nttdata.accounts_movements_service.service.ReportService;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String path = "/reports";

    @Test
    void testReportWithCustomerId() throws Exception {

        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        Mockito.when(
                        reportService.accountStatementReport(Mockito.any(), Mockito.eq(1L), Mockito.eq(start),
                                Mockito.eq(end)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get(path)
                        .param("page", "0")
                        .param("size", "10")
                        .param("customerId", "1")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testReportWithoutCustomerId() throws Exception {

        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        Mockito.when(reportService.accountStatementReport(Mockito.any(), Mockito.isNull(), Mockito.eq(start),
                        Mockito.eq(end)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get(path)
                        .param("page", "0")
                        .param("size", "10")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testReportStartDateAfterEndDateShouldFail() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        mockMvc.perform(get(path)
                        .param("startDate", start.toString())
                        .param("endDate", end.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}