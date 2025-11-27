package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironnementDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.service.EnvironnementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/environnements")
@RequiredArgsConstructor
public class EnvironnementController {

    private final EnvironnementService environnementService;

    @GetMapping
    public ResponseEntity<List<EnvironnementDTO>> getEnvironmentsByProjetAndType(
            @RequestParam Long projetId,
            @RequestParam String typeCode
    ) {
        return ResponseEntity.ok(
                environnementService.getEnvironmentsByProjetAndType(projetId, typeCode)
        );
    }

    @PostMapping
    public ResponseEntity<EnvironnementDTO> create(@Valid @RequestBody EnvironnementDTO dto) {
        EnvironnementDTO created = environnementService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<EnvironnementDTO>> search(@RequestBody PaginationRequest req) {
        return ResponseEntity.ok(environnementService.search(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnvironnementDTO> update(@PathVariable Long id,
                                                   @Valid @RequestBody EnvironnementDTO dto) {
        return ResponseEntity.ok(environnementService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        environnementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
