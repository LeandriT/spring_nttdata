package ec.com.nttdata.accounts_movements_service.event_handler;

import ec.com.nttdata.accounts_movements_service.event_handler.dto.AccountBalanceDto;
import ec.com.nttdata.accounts_movements_service.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountBalanceEventListener implements ApplicationListener<AccountBalanceDto> {
    private final AccountService service;

    @EventListener
    public void handleAccountBalance(AccountBalanceDto event) {
        log.info("Received account event with data: " + event.toString());
        service.updateAccountBalance(event);
    }

    @Override
    public void onApplicationEvent(AccountBalanceDto event) {
        log.info("Received account onApplicationEvent");
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}