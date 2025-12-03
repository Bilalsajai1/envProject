package ma.perenity.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserPermissionsDTO {

    private Long userId;
    private String code;
    private String firstName;
    private String lastName;
    private String email;

    private String profilCode;
    private String profilLibelle;
    private boolean admin;

    private Set<String> roles;
}
