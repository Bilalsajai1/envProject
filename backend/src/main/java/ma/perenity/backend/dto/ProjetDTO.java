package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjetDTO {

    private Long id;

    @NotBlank
    private String code;

    @NotBlank
    private String libelle;

    private String description;

    private Boolean actif;

    // Optionnel : type(s) d'environnement contextuel(s) pour la création (contrôle d'accès)
    private List<String> envTypeCodes;

    // Compatibilité ascendante (un seul type envoyé côté front actuel)
    private String envTypeCode;
}
