package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.ProfilPermissionsDTO;
import ma.perenity.backend.dto.SaveProfilPermissionsRequest;
import ma.perenity.backend.service.ProfilPermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profils")
@RequiredArgsConstructor
public class ProfilPermissionController {

    private final ProfilPermissionService service;

    /**
     * Récupère les permissions d'un profil
     * GET /profils/{id}/permissions
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<ProfilPermissionsDTO> getPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPermissions(id));
    }

    /**
     * Sauvegarde les permissions d'un profil
     * POST /profils/{id}/permissions
     */
    @PostMapping("/{id}/permissions")
    public ResponseEntity<Void> savePermissions(
            @PathVariable Long id,
            @Valid @RequestBody SaveProfilPermissionsRequest request
    ) {
        if (!id.equals(request.getProfilId())) {
            throw new IllegalArgumentException("L'ID du profil ne correspond pas");
        }

        // 🔥 Validation supplémentaire
        if (request.getEnvTypePermissions() != null) {
            Set<String> invalidKeys = request.getEnvTypePermissions().keySet().stream()
                    .filter(key -> key.equals("size") || key.equals("page") || key.equals("sort"))
                    .collect(Collectors.toSet());

            if (!invalidKeys.isEmpty()) {
                throw new IllegalArgumentException(
                        "Clés invalides détectées dans envTypePermissions : " + invalidKeys
                );
            }
        }

        service.savePermissions(request);
        return ResponseEntity.ok().build();
    }
}