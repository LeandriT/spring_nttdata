package ec.com.nttdata.accounts_movements_service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private final String pathV1 = "/reports/v1";
    private final String pathV2 = "/reports/v2";

    @Test
    void testReportV1WithCustomerId() throws Exception {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        Mockito.when(
                reportService.accountStatementReport(Mockito.any(), Mockito.eq(1L), Mockito.eq(start), Mockito.eq(end))
        ).thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get(pathV1)
                        .param("page", "0")
                        .param("size", "10")
                        .param("customerId", "1")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testReportV1WithoutCustomerId() throws Exception {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        Mockito.when(
                reportService.accountStatementReport(Mockito.any(), Mockito.isNull(), Mockito.eq(start),
                        Mockito.eq(end))
        ).thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get(pathV1)
                        .param("page", "0")
                        .param("size", "10")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testReportV1StartDateAfterEndDateShouldFail() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        mockMvc.perform(get(pathV1)
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testReportV2WithCustomerId() throws Exception {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        Mockito.when(
                reportService.generatePlainReport(Mockito.any(), Mockito.eq(1L), Mockito.eq(start), Mockito.eq(end))
        ).thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get(pathV2)
                        .param("page", "0")
                        .param("size", "10")
                        .param("customerId", "1")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testReportV2WithoutCustomerId() throws Exception {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        Mockito.when(
                reportService.generatePlainReport(Mockito.any(), Mockito.isNull(), Mockito.eq(start), Mockito.eq(end))
        ).thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get(pathV2)
                        .param("page", "0")
                        .param("size", "10")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testReportV2StartDateAfterEndDateShouldFail() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        mockMvc.perform(get(pathV2)
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isBadRequest());
    }
}