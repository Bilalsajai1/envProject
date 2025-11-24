package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentTypeDTO {

    private Long id;
    @NotBlank()
    private String code;
    @NotBlank()
    private String libelle;
    private Boolean actif;
}
