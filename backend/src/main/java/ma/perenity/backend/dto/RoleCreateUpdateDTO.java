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

    private String libelle;

    /**
     * Valeur de l'enum ActionType (ex: READ, WRITE, DELETE, ...)
     */
    @NotBlank
    private String action;

    private Boolean actif;

    private Long menuId;           // optionnel
    private Long environnementId;  // optionnel
}
