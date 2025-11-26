package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfilCreateUpdateDTO {
    @NotBlank
    private String code;
    @NotBlank
    private String libelle;
    
    private String description;

    private Boolean admin;
    private Boolean actif;
}
