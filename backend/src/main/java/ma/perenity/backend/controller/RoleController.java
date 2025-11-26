package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.RoleCreateUpdateDTO;
import ma.perenity.backend.dto.RoleDTO;
import ma.perenity.backend.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleDTO> getAll() {
        return roleService.getAll();
    }

    @GetMapping("/{id}")
    public RoleDTO getOne(@PathVariable Long id) {
        return roleService.getById(id);
    }

    @PostMapping
    public ResponseEntity<RoleDTO> create(@RequestBody @Valid RoleCreateUpdateDTO dto) {
        RoleDTO created = roleService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @PostMapping("/search")
    public PaginatedResponse<RoleDTO> search(@RequestBody PaginationRequest req) {
        return roleService.search(req);
    }


    @PutMapping("/{id}")
    public RoleDTO update(@PathVariable Long id,
                          @RequestBody @Valid RoleCreateUpdateDTO dto) {
        return roleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }

    @GetMapping("/by-menu/{menuId}")
    public List<RoleDTO> getByMenu(@PathVariable Long menuId) {
        return roleService.getByMenu(menuId);
    }

    @GetMapping("/by-env/{envId}")
    public List<RoleDTO> getByEnv(@PathVariable Long envId) {
        return roleService.getByEnvironnement(envId);
    }
}
