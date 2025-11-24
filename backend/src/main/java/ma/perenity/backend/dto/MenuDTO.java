package ma.perenity.backend.dto;

import lombok.*;
import ma.perenity.backend.entities.MenuEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuDTO {

    private Long id;
    private String code;
    private String libelle;
    private String route;
    private String icon;
    private Integer ordre;

    public static MenuDTO from(MenuEntity m) {
        return MenuDTO.builder()
                .id(m.getId())
                .code(m.getCode())
                .libelle(m.getLibelle())
                .route(m.getRoute())
                .icon(m.getIcon())
                .ordre(m.getOrdre())
                .build();
    }
}