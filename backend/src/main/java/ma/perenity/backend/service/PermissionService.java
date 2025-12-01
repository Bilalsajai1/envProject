// ma/perenity/backend/service/PermissionService.java

package ma.perenity.backend.service;

import ma.perenity.backend.dto.UserPermissionsDTO;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.enums.ActionType;

public interface PermissionService {

    boolean isAdmin();

    boolean hasRole(String roleCode);

    boolean hasAnyRole(String... roleCodes);

    boolean canAccessEnvType(String envTypeCode, ActionType action);

    boolean canAccessProjectCode(String projectCode, ActionType action);

    boolean canAccessProject(ProjetEntity projet, ActionType action);

    // ✅ NOUVELLE MÉTHODE
    boolean canAccessProjectById(Long projectId, ActionType action);

    boolean canAccessEnv(EnvironnementEntity env, ActionType action);

    UserPermissionsDTO getCurrentUserPermissions();
}