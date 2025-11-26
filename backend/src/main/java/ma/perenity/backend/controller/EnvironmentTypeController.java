package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironmentTypeDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.service.EnvironmentTypeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/environment-types")
@RequiredArgsConstructor
public class EnvironmentTypeController {

    private final EnvironmentTypeService service;

    @GetMapping
    public List<EnvironmentTypeDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/actives")
    public List<EnvironmentTypeDTO> getAllActive() {
        return service.getAllActive();
    }

    @GetMapping("/{id}")
    public EnvironmentTypeDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public EnvironmentTypeDTO create(@Valid @RequestBody EnvironmentTypeDTO dto) {
        return service.create(dto);
    }
    @PostMapping("/search")
    public PaginatedResponse<EnvironmentTypeDTO> search(@RequestBody PaginationRequest req) {
        return service.search(req);
    }

    @PutMapping("/{id}")
    public EnvironmentTypeDTO update(
            @PathVariable Long id,
            @Valid @RequestBody EnvironmentTypeDTO dto
    ) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
