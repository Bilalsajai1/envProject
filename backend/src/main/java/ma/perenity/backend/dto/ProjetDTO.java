package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjetDTO {
    private Long id;
    @NotBlank()
    private String code;
    @NotBlank()
    private String libelle;

    private String description;

    private Boolean actif;
}