package ma.perenity.backend.controller;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.MenuDTO;
import ma.perenity.backend.dto.MenuCreateUpdateDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.service.MenuService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuDTO>> all() {
        return ResponseEntity.ok(menuService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.findById(id));
    }

    @GetMapping("/by-environment-type/{code}")
    public ResponseEntity<List<MenuDTO>> getByEnvironmentType(@PathVariable String code) {
        return ResponseEntity.ok(menuService.findByEnvironmentTypeCode(code));
    }

    @PostMapping
    public ResponseEntity<MenuDTO> create(@Valid @RequestBody MenuCreateUpdateDTO dto) {
        MenuDTO created = menuService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<MenuDTO>> search(@RequestBody PaginationRequest req) {
        return ResponseEntity.ok(menuService.search(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuDTO> update(@PathVariable Long id,
                                          @Valid @RequestBody MenuCreateUpdateDTO dto) {
        return ResponseEntity.ok(menuService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
