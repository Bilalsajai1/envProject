package ma.perenity.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleKeycloakDTO {
    private String code;
    private String libelle;
}