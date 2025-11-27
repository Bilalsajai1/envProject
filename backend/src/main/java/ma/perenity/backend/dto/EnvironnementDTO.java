package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironnementDTO {

    private Long id;

    @NotBlank
    private String code;

    @NotBlank
    private String libelle;

    @Size(max = 255)
    private String description;

    private Boolean actif;

    private Long projetId;


    private String typeCode;
}
