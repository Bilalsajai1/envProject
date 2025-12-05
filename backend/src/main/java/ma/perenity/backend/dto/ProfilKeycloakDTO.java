package ma.perenity.backend.dto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfilKeycloakDTO {
    private String code;
    private String libelle;
    private List<RoleKeycloakDTO> roles;
}