package ma.perenity.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.perenity.backend.entities.enums.ActionType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilPermissionsDTO {

    private Long profilId;
    private String profilCode;
    private String profilLibelle;

    // Permissions par type d'environnement
    private List<EnvTypePermissionDTO> envTypePermissions;

    // Permissions globales pour les projets
    private List<ActionType> projectActions;

    // Permissions globales pour les environnements
    private List<ActionType> environmentActions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnvTypePermissionDTO {
        private String typeCode;        // EDITION, INTEGRATION, CLIENT
        private String typeLibelle;
        private List<ActionType> actions;
    }
}