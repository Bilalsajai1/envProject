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
    public ResponseEntity<List<RoleDTO>> getAll() {
        return ResponseEntity.ok(roleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    @PostMapping
    public ResponseEntity<RoleDTO> create(@Valid @RequestBody RoleCreateUpdateDTO dto) {
        RoleDTO created = roleService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<RoleDTO>> search(@RequestBody PaginationRequest req) {
        return ResponseEntity.ok(roleService.search(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> update(@PathVariable Long id,
                                          @Valid @RequestBody RoleCreateUpdateDTO dto) {
        return ResponseEntity.ok(roleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-menu/{menuId}")
    public ResponseEntity<List<RoleDTO>> getByMenu(@PathVariable Long menuId) {
        return ResponseEntity.ok(roleService.getByMenu(menuId));
    }

    @GetMapping("/by-env/{envId}")
    public ResponseEntity<List<RoleDTO>> getByEnv(@PathVariable Long envId) {
        return ResponseEntity.ok(roleService.getByEnvironnement(envId));
    }
}
