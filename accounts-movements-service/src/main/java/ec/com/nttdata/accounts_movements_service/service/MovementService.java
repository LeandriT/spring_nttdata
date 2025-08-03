package ec.com.nttdata.accounts_movements_service.service;

import ec.com.nttdata.accounts_movements_service.dto.movement.request.MovementRequest;
import ec.com.nttdata.accounts_movements_service.dto.movement.response.MovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovementService {
    Page<MovementResponse> index(Pageable pageable);

    MovementResponse show(Long id);

    MovementResponse create(MovementRequest request);

    MovementResponse update(Long id, MovementRequest request);

    void delete(Long id);
}
