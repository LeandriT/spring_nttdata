package ec.com.nttdata.customer_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.com.nttdata.customer_service.dto.request.CustomerRequest;
import ec.com.nttdata.customer_service.dto.response.CustomerResponse;
import ec.com.nttdata.customer_service.exception.CustomerDniFoundException;
import ec.com.nttdata.customer_service.exception.CustomerDniInvalidException;
import ec.com.nttdata.customer_service.exception.CustomerNotFoundException;
import ec.com.nttdata.customer_service.listener.eventTransaction.dto.TransactionCustomerDto;
import ec.com.nttdata.customer_service.mapper.CustomerMapper;
import ec.com.nttdata.customer_service.model.Customer;
import ec.com.nttdata.customer_service.repository.CustomerRepository;
import ec.com.nttdata.customer_service.util.IdentificationValidator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;
    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerRequest customerRequest;
    private CustomerResponse customerDto;

    @BeforeEach
    void setUp() {
        Long uuid = new Random().nextLong();
        String dni = "1234567890";

        customer = new Customer();
        customer.setId(uuid);
        customer.setName("John Doe");
        customer.setDni(dni);

        customerRequest = new CustomerRequest();
        customerRequest.setDni(dni);
        customerRequest.setPassword("myPassword123");

        customerDto = new CustomerResponse();
        customerDto.setName("John Doe");
        customerDto.setDni("1712345678");
    }

    @Test
    void show_CustomerExists_ReturnsCustomerDto() {
        when(repository.findById(any(Long.class))).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerDto);

        CustomerResponse result = customerService.show(customer.getId());

        assertEquals(customerDto, result);
    }

    @Test
    void show_CustomerDoesNotExist_ThrowsCustomerNotFoundException() {
        Long uuid = new Random().nextLong();
        String message = String.format("Customer doest no exists %s", uuid);
        when(repository.findById(uuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CustomerNotFoundException.class, () -> customerService.show(uuid));
        assertEquals(message, exception.getMessage());
    }

    @Test
    void create_ValidRequest_ReturnsCustomerDto() {
        String identification = customerRequest.getDni();

        when(customerMapper.toModel(any(CustomerRequest.class))).thenReturn(customer);
        when(repository.existsByDni(identification)).thenReturn(false);

        try (MockedStatic<IdentificationValidator> mockedValidator = mockStatic(IdentificationValidator.class)) {
            mockedValidator.when(() -> IdentificationValidator.isValidIdentifier(identification)).thenReturn(true);

            when(passwordEncoder.encode(any(String.class))).thenReturn("encryptedPassword");
            when(repository.save(any(Customer.class))).thenReturn(customer);
            when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerDto);

            CustomerResponse result = customerService.create(customerRequest);

            assertEquals(customerDto, result);
            verify(passwordEncoder).encode("myPassword123");
            assertEquals("encryptedPassword", customer.getPassword());
        }
    }

    @Test
    void create_IdentificationExists_ThrowsCustomerIdentificationFoundException() {
        String identification = customerRequest.getDni();
        when(repository.existsByDni(identification)).thenReturn(true);

        Exception exception =
                assertThrows(CustomerDniFoundException.class, () -> customerService.create(customerRequest));
        assertEquals("customer with " + identification + " already exists", exception.getMessage());
    }

    @Test
    void create_InvalidIdentification_ThrowsCustomerIdentificationInvalidException() {
        String identification = customerRequest.getDni();
        when(repository.existsByDni(identification)).thenReturn(false);

        try (MockedStatic<IdentificationValidator> mockedValidator = mockStatic(IdentificationValidator.class)) {
            mockedValidator.when(() -> IdentificationValidator.isValidIdentifier(identification)).thenReturn(false);

            Exception exception =
                    assertThrows(CustomerDniInvalidException.class, () -> customerService.create(customerRequest));
            assertEquals("customer with dni " + identification + " is invalid", exception.getMessage());
        }
    }

    @Test
    void update_CustomerExists_ReturnsUpdatedCustomerDto() {
        when(repository.findById(any(Long.class))).thenReturn(Optional.of(customer));
        doNothing().when(customerMapper).updateModel(any(CustomerRequest.class), any(Customer.class));
        when(repository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerDto);

        CustomerResponse result = customerService.update(customer.getId(), customerRequest);

        assertEquals(customerDto, result);
        verify(repository).save(customer);
    }

    @Test
    void update_CustomerDoesNotExist_ThrowsCustomerNotFoundException() {
        Long uuid = new Random().nextLong();
        String message = String.format("Customer doest no exists %s", uuid);
        when(repository.findById(uuid)).thenReturn(Optional.empty());

        Exception exception =
                assertThrows(CustomerNotFoundException.class, () -> customerService.update(uuid, customerRequest));
        assertEquals(message, exception.getMessage());
    }

    @Test
    void delete_CustomerExists_DeletesCustomer() {
        when(repository.findById(any(Long.class))).thenReturn(Optional.of(customer));
        doNothing().when(repository).delete(any(Customer.class));

        customerService.delete(customer.getId());

        verify(repository, times(1)).delete(customer);
    }

    @Test
    void delete_CustomerDoesNotExist_ThrowsCustomerNotFoundException() {
        Long uuid = new Random().nextLong();
        String message = String.format("Customer doest no exists %s", uuid);
        when(repository.findById(uuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CustomerNotFoundException.class, () -> customerService.delete(uuid));
        assertEquals(message, exception.getMessage());
    }

    @Test
    void findAll_ReturnsPageOfCustomerDto() {
        Page<Customer> customerPage = new PageImpl<>(List.of(customer));
        when(repository.findAll(any(Pageable.class))).thenReturn(customerPage);
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerDto);

        Page<CustomerResponse> result = customerService.index(Pageable.unpaged());

        assertEquals(1, result.getContent().size());
        assertEquals(customerDto.getDni(), result.getContent().get(0).getDni());
    }

    @Test
    void eventTransactionAccountProcessed_logsCorrectly() {
        TransactionCustomerDto transactionDto = new TransactionCustomerDto();
        transactionDto.setCustomerId(1L);
        transactionDto.setAmount(BigDecimal.valueOf(100.0));
        transactionDto.setMovementType("DEPOSIT");

        // No exception expected, just verifying it runs
        customerService.eventTransactionAccountProcessed(transactionDto);
    }

    @Test
    void findByIds_ReturnsCustomerDtos() {
        List<Long> ids = List.of(1L, 2L);
        List<Customer> customers = List.of(customer, customer);
        when(repository.findAllById(ids)).thenReturn(customers);
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerDto);

        List<CustomerResponse> result = customerService.findByIds(ids);

        assertEquals(2, result.size());
        verify(repository).findAllById(ids);
    }
}