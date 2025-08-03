package ec.com.nttdata.accounts_movements_service.controller;

import ec.com.nttdata.accounts_movements_service.dto.report.AccountStatementReport;
import ec.com.nttdata.accounts_movements_service.service.ReportService;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {
    private final ReportService service;

    @GetMapping()
    public ResponseEntity<Page<AccountStatementReport>> reportV3(
            Pageable pageable,

            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @NotNull(message = "Start date is required") LocalDate startDate,

            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @NotNull(message = "End date is required") LocalDate endDate,

            @RequestParam(value = "customerId", required = false)
            Long customerId
    ) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        Page<AccountStatementReport> accountStatementReports =
                service.accountStatementReport(pageable, customerId, startDate, endDate);
        return ResponseEntity.ok(accountStatementReports);
    }
}
