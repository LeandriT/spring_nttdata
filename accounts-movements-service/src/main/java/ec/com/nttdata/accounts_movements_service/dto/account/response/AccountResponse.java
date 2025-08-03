package ec.com.nttdata.accounts_movements_service.dto.account.response;

import ec.com.nttdata.accounts_movements_service.enums.AccountTypeEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private AccountTypeEnum accountType;
    private BigDecimal initialBalance;
    private BigDecimal actualBalance;
    private Boolean status;
    private Long customerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
