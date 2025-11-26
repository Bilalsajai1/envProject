package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.ProfilCreateUpdateDTO;
import ma.perenity.backend.dto.ProfilDTO;
import ma.perenity.backend.service.ProfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profils")
@RequiredArgsConstructor
public class ProfilController {

    private final ProfilService service;

    @GetMapping
    public ResponseEntity<List<ProfilDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfilDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProfilDTO> create(@Valid @RequestBody ProfilCreateUpdateDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfilDTO> update(@PathVariable Long id,
                                            @Valid @RequestBody ProfilCreateUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Assignation des rôles ----
    @GetMapping("/{id}/roles")
    public ResponseEntity<List<Long>> getRoleIds(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRoleIds(id));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<Void> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody List<Long> roleIds) {

        service.assignRoles(id, roleIds);
        return ResponseEntity.noContent().build();
    }
}