package ec.com.nttdata.accounts_movements_service.dto.report;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementReport {
    private CustomerReport customer;
}