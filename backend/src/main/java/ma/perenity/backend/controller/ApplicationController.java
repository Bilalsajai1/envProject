package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.ApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService service;

    @GetMapping
    public ResponseEntity<List<ApplicationDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/actives")
    public ResponseEntity<List<ApplicationDTO>> getAllActive() {
        return ResponseEntity.ok(service.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApplicationDTO> create(@Valid @RequestBody ApplicationDTO dto) {
        ApplicationDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<ApplicationDTO>> search(@RequestBody PaginationRequest req) {
        return ResponseEntity.ok(service.search(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationDTO> update(@PathVariable Long id,
                                                 @Valid @RequestBody ApplicationDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
