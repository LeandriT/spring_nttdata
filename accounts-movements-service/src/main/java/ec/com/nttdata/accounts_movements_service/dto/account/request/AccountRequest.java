package ec.com.nttdata.accounts_movements_service.dto.account.request;

import ec.com.nttdata.accounts_movements_service.dto.retentions.OnCreate;
import ec.com.nttdata.accounts_movements_service.enums.AccountTypeEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequest {
    @NotNull(message = "account number cannot be null", groups = {OnCreate.class})
    private String accountNumber;
    @NotNull(message = "account type cannot be null", groups = {OnCreate.class})
    private AccountTypeEnum accountType;
    @NotNull(message = "initial balance cannot be null", groups = {OnCreate.class})
    @Min(value = 1, message = "initial balance must be greater than 0", groups = {OnCreate.class})
    private BigDecimal initialBalance;
    @NotNull(message = "status cannot be null", groups = {OnCreate.class})
    private Boolean status;
    @NotNull(message = "customer_id cannot be null", groups = {OnCreate.class})
    private Long customerId;
}