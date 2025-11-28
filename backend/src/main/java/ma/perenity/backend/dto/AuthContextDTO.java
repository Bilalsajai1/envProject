package ma.perenity.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthContextDTO {

    private UserPermissionsDTO user;

    private List<EnvironmentTypePermissionDTO> environmentTypes;
}
