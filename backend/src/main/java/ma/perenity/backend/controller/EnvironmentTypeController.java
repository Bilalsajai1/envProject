package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironmentTypeDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.service.EnvironmentTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/environment-types")
@RequiredArgsConstructor
public class EnvironmentTypeController {

    private final EnvironmentTypeService service;

    @GetMapping
    public ResponseEntity<List<EnvironmentTypeDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/actives")
    public ResponseEntity<List<EnvironmentTypeDTO>> getAllActive() {
        return ResponseEntity.ok(service.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvironmentTypeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<EnvironmentTypeDTO> create(@Valid @RequestBody EnvironmentTypeDTO dto) {
        EnvironmentTypeDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<EnvironmentTypeDTO>> search(@RequestBody PaginationRequest req) {
        return ResponseEntity.ok(service.search(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnvironmentTypeDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody EnvironmentTypeDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
