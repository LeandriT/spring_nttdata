package ec.com.nttdata.accounts_movements_service.service;

import ec.com.nttdata.accounts_movements_service.dto.report.AccountStatementReport;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    Page<AccountStatementReport> accountStatementReport(Pageable pageable, Long customerId, LocalDate startDate,
                                                        LocalDate endDate);
}
