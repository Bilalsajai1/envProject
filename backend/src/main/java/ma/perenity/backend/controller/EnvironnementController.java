package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironnementDTO;
import ma.perenity.backend.service.EnvironnementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/environnements")
@RequiredArgsConstructor
public class EnvironnementController {

    private final EnvironnementService environnementService;

    @GetMapping
    public List<EnvironnementDTO> getEnvironmentsByProjetAndType(
            @RequestParam Long projetId,
            @RequestParam String typeCode
    ) {
        return environnementService.getEnvironmentsByProjetAndType(projetId, typeCode);
    }

    @PostMapping
    public EnvironnementDTO create(@Valid @RequestBody EnvironnementDTO dto) {
        return environnementService.create(dto);
    }

    @PutMapping("/{id}")
    public EnvironnementDTO update(
            @PathVariable Long id,
            @Valid @RequestBody EnvironnementDTO dto
    ) {
        return environnementService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        environnementService.delete(id);
    }
}
