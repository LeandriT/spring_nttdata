package ec.com.nttdata.accounts_movements_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AccountsMovementsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountsMovementsServiceApplication.class, args);
    }

}
