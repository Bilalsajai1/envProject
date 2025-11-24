package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.ProjetDTO;
import ma.perenity.backend.service.ProjetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;


    @GetMapping("/by-environment-type/{typeCode}")
    public List<ProjetDTO> getProjectsByEnvironmentType(@PathVariable String typeCode) {
        return projetService.getProjectsByEnvironmentType(typeCode);
    }


    @GetMapping
    public List<ProjetDTO> getAll() {
        return projetService.getAll();
    }

    @GetMapping("/{id}")
    public ProjetDTO getById(@PathVariable Long id) {
        return projetService.getById(id);
    }

    @PostMapping
    public ProjetDTO create(@Valid @RequestBody ProjetDTO dto) {
        return projetService.create(dto);
    }

    @PutMapping("/{id}")
    public ProjetDTO update(@PathVariable Long id, @Valid @RequestBody ProjetDTO dto) {
        return projetService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        projetService.delete(id);
    }
}
