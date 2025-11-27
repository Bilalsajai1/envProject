package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.ProjetDTO;
import ma.perenity.backend.service.ProjetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;

    @GetMapping("/by-environment-type/{typeCode}")
    public ResponseEntity<List<ProjetDTO>> getProjectsByEnvironmentType(@PathVariable String typeCode) {
        return ResponseEntity.ok(projetService.getProjectsByEnvironmentType(typeCode));
    }

    @GetMapping
    public ResponseEntity<List<ProjetDTO>> getAll() {
        return ResponseEntity.ok(projetService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projetService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProjetDTO> create(@Valid @RequestBody ProjetDTO dto) {
        ProjetDTO created = projetService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<ProjetDTO>> search(@RequestBody PaginationRequest req) {
        return ResponseEntity.ok(projetService.search(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetDTO> update(@PathVariable Long id,
                                            @Valid @RequestBody ProjetDTO dto) {
        return ResponseEntity.ok(projetService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
