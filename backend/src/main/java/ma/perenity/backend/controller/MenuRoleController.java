package ma.perenity.backend.controller;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.entities.RoleEntity;
import ma.perenity.backend.service.MenuRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuRoleController {

    private final MenuRoleService menuRoleService;

    @GetMapping("/{menuId}/roles")
    public ResponseEntity<List<RoleEntity>> getRoles(@PathVariable Long menuId) {
        return ResponseEntity.ok(menuRoleService.getRolesForMenu(menuId));
    }

    @PostMapping("/{menuId}/roles/{roleId}")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long menuId,
            @PathVariable Long roleId) {
        menuRoleService.assignRoleToMenu(menuId, roleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{menuId}/roles/{roleId}")
    public ResponseEntity<Void> removeRole(
            @PathVariable Long menuId,
            @PathVariable Long roleId) {
        menuRoleService.removeRoleFromMenu(menuId, roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles/unassigned")
    public ResponseEntity<List<RoleEntity>> unassignedRoles() {
        return ResponseEntity.ok(menuRoleService.getUnassignedRoles());
    }
}

