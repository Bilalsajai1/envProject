package ma.perenity.backend.service;

import ma.perenity.backend.dto.UserPermissionsDTO;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.enums.ActionType;

public interface PermissionService {

    boolean isAdmin();

    boolean hasRole(String roleCode);

    boolean hasAnyRole(String... roleCodes);

    boolean canAccessEnvType(String envTypeCode, ActionType action);

    boolean canAccessEnv(EnvironnementEntity env, ActionType action);

    UserPermissionsDTO getCurrentUserPermissions();

    // ✅ NOUVELLES MÉTHODES
    boolean canAccessProject(ActionType action);

    boolean canAccessEnvironment(ActionType action);
}