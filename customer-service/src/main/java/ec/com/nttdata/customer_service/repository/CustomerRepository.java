package ec.com.nttdata.customer_service.repository;

import ec.com.nttdata.customer_service.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByDni(String dni);
}
