package ma.perenity.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UtilisateurKeycloakDTO {

    private Long id;
    private String code;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Boolean enabled;
    private String keycloakId;
    private Long profilId;
}