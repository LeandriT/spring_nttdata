package ec.com.nttdata.accounts_movements_service.event_handler.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
@ToString(of = "accountId")
public class AccountBalanceDto extends ApplicationEvent implements Serializable {
    private Long accountId;
    private BigDecimal balance;
    private boolean isInitial;

    public AccountBalanceDto(Object source, Long id, BigDecimal balance) {
        super(source);
        this.accountId = id;
        this.balance = balance;

    }
}