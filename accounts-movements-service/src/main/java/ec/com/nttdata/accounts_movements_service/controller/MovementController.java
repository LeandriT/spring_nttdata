package ec.com.nttdata.accounts_movements_service.controller;

import ec.com.nttdata.accounts_movements_service.dto.movement.request.MovementRequest;
import ec.com.nttdata.accounts_movements_service.dto.movement.response.MovementResponse;
import ec.com.nttdata.accounts_movements_service.dto.retentions.OnCreate;
import ec.com.nttdata.accounts_movements_service.dto.retentions.OnUpdate;
import ec.com.nttdata.accounts_movements_service.service.MovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movements")
@RequiredArgsConstructor
@Validated
public class MovementController {
    private final MovementService service;

    @GetMapping
    public ResponseEntity<Page<MovementResponse>> index(Pageable pageable) {
        return new ResponseEntity<>(service.index(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovementResponse> show(@PathVariable Long id) {
        return new ResponseEntity<>(service.show(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MovementResponse> create(@Validated(OnCreate.class) @RequestBody MovementRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovementResponse> update(@PathVariable Long id,
                                                   @Validated(OnUpdate.class) @RequestBody MovementRequest request) {
        return new ResponseEntity<>(service.update(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}