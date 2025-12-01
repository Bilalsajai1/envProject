package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;


@Data
public class UpdateProfilPermissionsRequest {

    @NotNull
    private Long profilId;

    private List<EnvTypePermissionUpdateDTO> envTypePermissions;

    private List<ProjectPermissionUpdateDTO> projectPermissions;
}