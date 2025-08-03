package ec.com.nttdata.customer_service.controller;

import ec.com.nttdata.customer_service.dto.request.CustomerRequest;
import ec.com.nttdata.customer_service.dto.response.CustomerResponse;
import ec.com.nttdata.customer_service.service.CustomerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;


    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> index(Pageable pageable) {
        return new ResponseEntity<>(service.index(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> show(@PathVariable Long id) {
        return new ResponseEntity<>(service.show(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestBody CustomerRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id,
                                                   @RequestBody CustomerRequest customerRequest) {
        return new ResponseEntity<>(service.update(id, customerRequest), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PostMapping("/by-ids")
    public ResponseEntity<List<CustomerResponse>> getCustomersByIds(@RequestBody List<Long> customerIds) {
        List<CustomerResponse> customers = service.findByIds(customerIds);
        return ResponseEntity.ok(customers);
    }
}
