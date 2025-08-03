package ec.com.nttdata.accounts_movements_service.producer.customer.dto;

import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovementCustomerRequest implements Serializable {
    private Long customerId;
    private BigDecimal amount;
    private MovementTypeEnum movementType;
}
