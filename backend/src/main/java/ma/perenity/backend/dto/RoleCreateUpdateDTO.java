package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
}
