package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.service.EnvApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/env-applications")
@RequiredArgsConstructor
public class EnvApplicationController {

    private final EnvApplicationService service;

    @GetMapping("/by-env/{envId}")
    public ResponseEntity<List<EnvApplicationDTO>> getByEnv(@PathVariable Long envId) {
        return ResponseEntity.ok(service.getByEnvironnement(envId));
    }

    @PostMapping
    public ResponseEntity<EnvApplicationDTO> create(@Valid @RequestBody EnvApplicationDTO dto) {
        EnvApplicationDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<EnvApplicationDTO>> search(@RequestBody PaginationRequest req) {
        return ResponseEntity.ok(service.search(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnvApplicationDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody EnvApplicationDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
