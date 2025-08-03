package ec.com.nttdata.accounts_movements_service.dto.report;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerReport {
    private String name;
    @Builder.Default
    private List<CustomerAccountStatementReport> accounts = new ArrayList<>();
}