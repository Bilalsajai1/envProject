package ma.perenity.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilPermissionsDTO {
    private Long profilId;
    private String profilCode;
    private String profilLibelle;
    private Boolean isAdmin;
    private List<EnvironmentTypePermissionDTO> environmentTypes;
    private List<ProjectPermissionDTO> projects;
}
