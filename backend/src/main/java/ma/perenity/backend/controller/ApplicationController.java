package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.ApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.service.ApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService service;

    @GetMapping
    public List<ApplicationDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/actives")
    public List<ApplicationDTO> getAllActive() {
        return service.getAllActive();
    }

    @GetMapping("/{id}")
    public ApplicationDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ApplicationDTO create(@Valid @RequestBody ApplicationDTO dto) {
        return service.create(dto);
    }
    @PostMapping("/search")
    public PaginatedResponse<ApplicationDTO> search(@RequestBody PaginationRequest req) {
        return service.search(req);
    }

    @PutMapping("/{id}")
    public ApplicationDTO update(@PathVariable Long id, @Valid @RequestBody ApplicationDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
