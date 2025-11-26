package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.service.EnvApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/env-applications")
@RequiredArgsConstructor
public class EnvApplicationController {

    private final EnvApplicationService service;

    @GetMapping("/by-env/{envId}")
    public List<EnvApplicationDTO> getByEnv(@PathVariable Long envId) {
        return service.getByEnvironnement(envId);
    }

    @PostMapping
    public EnvApplicationDTO create(@Valid @RequestBody EnvApplicationDTO dto) {
        return service.create(dto);
    }
    @PostMapping("/search")
    public PaginatedResponse<EnvApplicationDTO> search(@RequestBody PaginationRequest req) {
        return service.search(req);
    }

    @PutMapping("/{id}")
    public EnvApplicationDTO update(
            @PathVariable Long id,
            @Valid @RequestBody EnvApplicationDTO dto
    ) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
