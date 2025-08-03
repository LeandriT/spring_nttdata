package ec.com.nttdata.accounts_movements_service.dto.report;

import ec.com.nttdata.accounts_movements_service.enums.AccountTypeEnum;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
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
public class CustomerAccountStatementReport {
    private String number;
    private AccountTypeEnum type;
    private BigDecimal initialBalance;
    private BigDecimal actualBalance;
    @Builder.Default
    private Set<MovementAccountStatementReport> movements =
            new TreeSet<>(Comparator.comparing(MovementAccountStatementReport::getDate));

}