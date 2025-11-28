package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.ProfilPermissionsDTO;
import ma.perenity.backend.dto.SaveProfilPermissionsRequest;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.ProfilEntity;
import ma.perenity.backend.entities.ProfilRoleEntity;
import ma.perenity.backend.entities.RoleEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.ProfilRepository;
import ma.perenity.backend.repository.ProfilRoleRepository;
import ma.perenity.backend.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfilPermissionService {

    private final ProfilRepository profilRepository;
    private final ProfilRoleRepository profilRoleRepository;
    private final RoleRepository roleRepository;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final PermissionService permissionService;

    private void checkAdmin() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Gestion des permissions réservée à l'administrateur");
        }
    }

    /**
     * Récupère les permissions actuelles d'un profil
     */
    @Transactional(readOnly = true)
    public ProfilPermissionsDTO getPermissions(Long profilId) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil introuvable"));

        // Récupérer tous les rôles du profil
        List<RoleEntity> roles = profilRoleRepository.findRolesByProfil(profilId);

        // Grouper par type
        Map<String, List<ActionType>> envTypeMap = new HashMap<>();
        Set<ActionType> projectActionsSet = new HashSet<>();
        Set<ActionType> environmentActionsSet = new HashSet<>();

        for (RoleEntity role : roles) {
            String code = role.getCode();
            ActionType action = role.getAction();

            // Format: ENV_{TYPE}_{ACTION}
            if (code.startsWith("ENV_")) {
                String[] parts = code.split("_");
                if (parts.length >= 3) {
                    String typeCode = parts[1]; // EDITION, CLIENT, INTEGRATION
                    envTypeMap.computeIfAbsent(typeCode, k -> new ArrayList<>()).add(action);
                }
            }
            // Format: PROJECT_{ACTION}
            else if (code.startsWith("PROJECT_")) {
                projectActionsSet.add(action);
            }
            // Format: ENVIRONMENT_{ACTION}
            else if (code.startsWith("ENVIRONMENT_")) {
                environmentActionsSet.add(action);
            }
        }

        // 🔥 CORRECTION : Récupérer UNIQUEMENT les types d'environnement valides
        List<EnvironmentTypeEntity> allTypes = environmentTypeRepository.findByActifTrue();

        // 🔥 Créer un Set des codes valides pour filtrage
        Set<String> validTypeCodes = allTypes.stream()
                .map(EnvironmentTypeEntity::getCode)
                .collect(Collectors.toSet());

        // Construire les DTOs pour chaque type d'environnement
        List<ProfilPermissionsDTO.EnvTypePermissionDTO> envTypePermissions = allTypes.stream()
                .map(type -> {
                    String typeCode = type.getCode();

                    // 🔥 Filtrer uniquement les actions pour ce type spécifique
                    List<ActionType> actions = envTypeMap.getOrDefault(typeCode, Collections.emptyList());

                    return ProfilPermissionsDTO.EnvTypePermissionDTO.builder()
                            .typeCode(typeCode)
                            .typeLibelle(type.getLibelle())
                            .actions(actions)
                            .build();
                })
                .collect(Collectors.toList());

        return ProfilPermissionsDTO.builder()
                .profilId(profil.getId())
                .profilCode(profil.getCode())
                .profilLibelle(profil.getLibelle())
                .envTypePermissions(envTypePermissions)
                .projectActions(new ArrayList<>(projectActionsSet))
                .environmentActions(new ArrayList<>(environmentActionsSet))
                .build();
    }

    /**
     * Sauvegarde les permissions d'un profil
     */
    @Transactional
    public void savePermissions(SaveProfilPermissionsRequest request) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(request.getProfilId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil introuvable"));

        // 🔥 Validation : Vérifier que tous les typeCodes existent
        Set<String> validTypeCodes = environmentTypeRepository.findByActifTrue()
                .stream()
                .map(EnvironmentTypeEntity::getCode)
                .collect(Collectors.toSet());

        if (request.getEnvTypePermissions() != null) {
            for (String typeCode : request.getEnvTypePermissions().keySet()) {
                if (!validTypeCodes.contains(typeCode)) {
                    throw new IllegalArgumentException(
                            "Type d'environnement invalide : " + typeCode +
                                    ". Types valides : " + validTypeCodes
                    );
                }
            }
        }

        // 1️⃣ Supprimer toutes les associations existantes
        profilRoleRepository.deleteByProfilId(profil.getId());

        // 2️⃣ Créer les nouveaux rôles
        List<RoleEntity> rolesToAssign = new ArrayList<>();

        // A) Rôles par type d'environnement
        if (request.getEnvTypePermissions() != null) {
            for (Map.Entry<String, List<ActionType>> entry : request.getEnvTypePermissions().entrySet()) {
                String typeCode = entry.getKey();
                List<ActionType> actions = entry.getValue();

                if (actions == null || actions.isEmpty()) {
                    continue; // Skip si aucune action
                }

                for (ActionType action : actions) {
                    String roleCode = "ENV_" + typeCode.toUpperCase() + "_" + action.name();
                    RoleEntity role = findOrCreateRole(roleCode, "Permission " + typeCode + " " + action.name(), action);
                    rolesToAssign.add(role);
                }
            }
        }

        // B) Rôles PROJECT
        if (request.getProjectActions() != null && !request.getProjectActions().isEmpty()) {
            for (ActionType action : request.getProjectActions()) {
                String roleCode = "PROJECT_" + action.name();
                RoleEntity role = findOrCreateRole(roleCode, "Permission Projet " + action.name(), action);
                rolesToAssign.add(role);
            }
        }

        // C) Rôles ENVIRONMENT
        if (request.getEnvironmentActions() != null && !request.getEnvironmentActions().isEmpty()) {
            for (ActionType action : request.getEnvironmentActions()) {
                String roleCode = "ENVIRONMENT_" + action.name();
                RoleEntity role = findOrCreateRole(roleCode, "Permission Environnement " + action.name(), action);
                rolesToAssign.add(role);
            }
        }

        // 3️⃣ Créer les associations ProfilRole
        for (RoleEntity role : rolesToAssign) {
            ProfilRoleEntity pr = new ProfilRoleEntity();
            pr.setProfil(profil);
            pr.setRole(role);
            profilRoleRepository.save(pr);
        }
    }

    /**
     * Trouve un rôle existant ou le crée s'il n'existe pas
     */
    private RoleEntity findOrCreateRole(String code, String libelle, ActionType action) {
        return roleRepository.findByCode(code)
                .orElseGet(() -> {
                    RoleEntity newRole = RoleEntity.builder()
                            .code(code)
                            .libelle(libelle)
                            .action(action)
                            .actif(true)
                            .build();
                    return roleRepository.save(newRole);
                });
    }
}