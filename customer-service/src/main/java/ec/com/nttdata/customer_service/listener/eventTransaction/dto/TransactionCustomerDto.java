package ec.com.nttdata.customer_service.listener.eventTransaction.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TransactionCustomerDto implements Serializable {
    private Long customerId;
    private BigDecimal amount;
    private String movementType;
}