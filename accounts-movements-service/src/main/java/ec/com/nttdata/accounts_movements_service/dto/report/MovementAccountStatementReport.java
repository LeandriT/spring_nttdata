package ec.com.nttdata.accounts_movements_service.dto.report;


import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class MovementAccountStatementReport {
    private BigDecimal balance;
    private BigDecimal amount;
    private LocalDateTime date;
    private MovementTypeEnum movementType;

}