package ma.perenity.backend.service;

import ma.perenity.backend.dto.UserPermissionsDTO;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.enums.ActionType;

import java.util.List;

public interface PermissionService {

    boolean isAdmin();

    boolean hasRole(String roleCode);

    boolean hasAnyRole(String... roleCodes);

    boolean canAccessEnvType(String envTypeCode, ActionType action);

    boolean canAccessProjectCode(String projectCode, ActionType action);

    boolean canAccessProject(ProjetEntity projet, ActionType action);

    boolean canAccessProjectById(Long projectId, ActionType action);

    boolean canAccessEnv(EnvironnementEntity env, ActionType action);

    UserPermissionsDTO getCurrentUserPermissions();

    boolean canViewEnvironmentType(String typeCode);

    List<ActionType> getProjectActions(Long projectId);

    List<ActionType> getProjectActionsByCode(String projectCode);

    boolean canConsultProject(Long projectId);

    boolean canCreateInProject(Long projectId);

    boolean canUpdateInProject(Long projectId);

    boolean canDeleteInProject(Long projectId);
}