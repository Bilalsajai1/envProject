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

    /**
     * Codes des rôles Keycloak / BD :
     * Exemple : ENV_EDITION_CONSULT, ENV_CLIENT_CREATE, ...
     */
    private Set<String> roles;
}
