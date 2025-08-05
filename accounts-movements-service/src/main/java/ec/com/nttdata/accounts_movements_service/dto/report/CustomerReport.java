package ec.com.nttdata.accounts_movements_service.dto.report;


import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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