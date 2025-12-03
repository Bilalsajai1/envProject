package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCreateUpdateDTO {

    @NotBlank
    private String code;

    @NotBlank
    private String libelle;

    @NotBlank
    private String action;

    private Boolean actif;

    private Long menuId;
    private Long environnementId;

    private Long projetId;
}
