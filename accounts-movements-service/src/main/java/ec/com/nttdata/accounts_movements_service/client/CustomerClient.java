package ec.com.nttdata.accounts_movements_service.client;

import ec.com.nttdata.accounts_movements_service.client.dto.CustomerDto;
import ec.com.nttdata.accounts_movements_service.exception.CustomerNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Set;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-client", url = "${app.ms.customer-service.url}")
public interface CustomerClient {

    @GetMapping("/customers/{id}")
    @CircuitBreaker(name = "customerClientCircuit", fallbackMethod = "fallbackDataById")
    @Retry(name = "customerClientRetry")
    CustomerDto show(@PathVariable("id") Long id);

    @PostMapping("/customers/by-ids")
    @CircuitBreaker(name = "customerClientCircuit", fallbackMethod = "fallbackDataByIds")
    @Retry(name = "customerClientRetry")
    Set<CustomerDto> showByIds(@RequestBody Set<Long> customerIds);

    default CustomerDto fallbackDataById(Throwable throwable) {
        throw new CustomerNotFoundException("Customer service is unavailable. Error: " + throwable.getMessage());
    }

    default Set<CustomerDto> fallbackDataByIds(Throwable throwable) {
        throw new CustomerNotFoundException("Customer service is unavailable. Error: " + throwable.getMessage());
    }
}
