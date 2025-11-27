package ma.perenity.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {

    private Long id;

    private String code;
    private String libelle;
    private String action;
    private Boolean actif;

    private Long menuId;
    private String menuCode;

    private Long environnementId;
    private String environnementCode;
}
