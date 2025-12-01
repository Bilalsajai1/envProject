package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.*;
import ma.perenity.backend.service.ProfilService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profils")
@RequiredArgsConstructor
public class ProfilController {

    private final ProfilService service;

    @GetMapping
    public ResponseEntity<List<ProfilDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfilDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProfilDTO> create(@Valid @RequestBody ProfilCreateUpdateDTO dto) {
        ProfilDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<ProfilDTO>> search(@RequestBody PaginationRequest req) {
        return ResponseEntity.ok(service.search(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfilDTO> update(@PathVariable Long id,
                                            @Valid @RequestBody ProfilCreateUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<List<Long>> getRoleIds(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRoleIds(id));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<Void> assignRoles(@PathVariable Long id,
                                            @Valid @RequestBody List<Long> roleIds) {
        service.assignRoles(id, roleIds);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ENDPOINTS DE PERMISSIONS
    // ============================================================

    /**
     * Récupère toutes les permissions d'un profil
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<ProfilPermissionsDTO> getPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPermissions(id));
    }

    /**
     * ✅ NOUVEAU : Met à jour TOUTES les permissions en une seule requête
     * C'est l'endpoint principal que vous devez utiliser
     */
    @PutMapping("/{id}/permissions")
    public ResponseEntity<Void> updateAllPermissions(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfilPermissionsRequest request) {
        // S'assurer que l'ID correspond
        request.setProfilId(id);
        service.updateAllPermissions(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Met à jour uniquement les permissions par type d'environnement
     */
    @PutMapping("/{id}/permissions/env-types")
    public ResponseEntity<Void> updateEnvTypePermissions(
            @PathVariable Long id,
            @Valid @RequestBody List<EnvTypePermissionUpdateDTO> envPermissions) {
        service.updateEnvTypePermissions(id, envPermissions);
        return ResponseEntity.noContent().build();
    }

    /**
     * Met à jour uniquement les permissions par projet
     */
    @PutMapping("/{id}/permissions/projects")
    public ResponseEntity<Void> updateProjectPermissions(
            @PathVariable Long id,
            @Valid @RequestBody List<ProjectPermissionUpdateDTO> projPermissions) {
        service.updateProjectPermissions(id, projPermissions);
        return ResponseEntity.noContent().build();
    }
}