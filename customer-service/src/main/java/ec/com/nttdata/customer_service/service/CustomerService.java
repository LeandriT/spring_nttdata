package ec.com.nttdata.customer_service.service;

import ec.com.nttdata.customer_service.dto.request.CustomerRequest;
import ec.com.nttdata.customer_service.dto.response.CustomerResponse;
import ec.com.nttdata.customer_service.listener.eventTransaction.dto.TransactionCustomerDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Page<CustomerResponse> index(Pageable pageable);

    CustomerResponse show(Long id);

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Long id, CustomerRequest customerRequest);

    void delete(Long id);

    void eventTransactionAccountProcessed(TransactionCustomerDto transactionCustomerDto);

    List<CustomerResponse> findByIds(List<Long> customerIds);
}
