package ec.com.nttdata.customer_service.service.impl;

import ec.com.nttdata.customer_service.dto.request.CustomerRequest;
import ec.com.nttdata.customer_service.dto.response.CustomerResponse;
import ec.com.nttdata.customer_service.exception.CustomerDniFoundException;
import ec.com.nttdata.customer_service.exception.CustomerDniInvalidException;
import ec.com.nttdata.customer_service.exception.CustomerNotFoundException;
import ec.com.nttdata.customer_service.listener.eventTransaction.dto.TransactionCustomerDto;
import ec.com.nttdata.customer_service.mapper.CustomerMapper;
import ec.com.nttdata.customer_service.model.Customer;
import ec.com.nttdata.customer_service.repository.CustomerRepository;
import ec.com.nttdata.customer_service.service.CustomerService;
import ec.com.nttdata.customer_service.util.IdentificationValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<CustomerResponse> index(Pageable pageable) {
        return repository.findAll(pageable).map(customerMapper::toResponse);
    }

    @Override
    public CustomerResponse show(Long id) {
        String message = String.format("Customer doest no exists %d", id);
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(message));
        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse create(CustomerRequest request) {
        Customer entity = customerMapper.toModel(request);
        this.validate(request);
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        entity.setPassword(encryptedPassword);
        repository.save(entity);
        return customerMapper.toResponse(entity);
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest customerRequest) {
        String message = String.format("Customer doest no exists %d", id);
        Customer entity = repository.findById(id).orElseThrow(() -> new CustomerNotFoundException(message));
        customerMapper.updateModel(customerRequest, entity);
        entity.setCustomerId(UUID.randomUUID().toString());
        repository.save(entity);
        return customerMapper.toResponse(entity);
    }

    @Override
    public void delete(Long id) {
        String message = String.format("Customer doest no exists %d", id);
        Customer entity = repository.findById(id).orElseThrow(() -> new CustomerNotFoundException(message));
        repository.delete(entity);
    }

    @Override
    public void eventTransactionAccountProcessed(TransactionCustomerDto transactionCustomerDto) {
        String message = String.format(
                "Account transaction event processed: id: %s amount: %.2f type: %s",
                transactionCustomerDto.getCustomerId(),
                transactionCustomerDto.getAmount(),
                transactionCustomerDto.getTransactionType()
        );
        log.info(message);
    }

    @Override
    public List<CustomerResponse> findByIds(List<Long> ids) {
        List<Customer> customers = repository.findAllById(ids);
        return customers.stream()
                .map(customerMapper::toResponse) // convierte entidad a DTO
                .toList();
    }

    void validateIdentificationExists(CustomerRequest request) {
        boolean exists = repository.existsByDni(request.getDni());
        if (exists) {
            String message = String.format("customer with %s already exists", request.getDni());
            throw new CustomerDniFoundException(message);
        }
    }

    void validateIdentificationEc(CustomerRequest request) {
        boolean validDni = IdentificationValidator.isValidIdentifier(request.getDni());
        if (!validDni) {
            String message = String.format("customer with dni %s is invalid", request.getDni());
            throw new CustomerDniInvalidException(message);
        }
    }

    void validate(CustomerRequest request) {
        this.validateIdentificationExists(request);
        this.validateIdentificationEc(request);
    }
}
