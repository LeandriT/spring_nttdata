package ec.com.nttdata.accounts_movements_service.repository;

import ec.com.nttdata.accounts_movements_service.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
}
