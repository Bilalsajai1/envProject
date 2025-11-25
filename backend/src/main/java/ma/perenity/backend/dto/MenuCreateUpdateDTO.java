package ma.perenity.backend.dto;

import lombok.Data;

@Data
public class MenuCreateUpdateDTO {

    private String code;
    private String libelle;
    private String route;
    private String icon;
    private Integer ordre;
    private Boolean visible;

    private Long parentId;
    private Long environmentTypeId;
}
