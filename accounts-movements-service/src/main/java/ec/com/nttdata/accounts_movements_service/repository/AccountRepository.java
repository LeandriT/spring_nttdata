package ec.com.nttdata.accounts_movements_service.repository;

import ec.com.nttdata.accounts_movements_service.model.Account;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query(value = "select a from Account a join fetch a.movements m " +
            "where (:customerId is null or a.customerId = :customerId) " +
            "and m.date between :startDate and :endDate")
    Page<Account> findByCustomerIdAndStartDateAndEndDate(
            Pageable pageable,
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
