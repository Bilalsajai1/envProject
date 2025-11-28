package ma.perenity.backend.dto;

import lombok.Data;
import ma.perenity.backend.entities.enums.ActionType;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class SaveProfilPermissionsRequest {

    @NotNull
    private Long profilId;

    // Map: typeCode -> liste d'actions
    // Ex: {"EDITION": ["CONSULT", "CREATE"], "CLIENT": ["CONSULT"]}
    private Map<String, List<ActionType>> envTypePermissions;

    // Actions globales pour PROJECT
    private List<ActionType> projectActions;

    // Actions globales pour ENVIRONMENT
    private List<ActionType> environmentActions;
}