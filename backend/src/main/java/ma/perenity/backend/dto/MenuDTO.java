package ma.perenity.backend.dto;

import lombok.Data;

@Data
public class MenuDTO {

    private Long id;

    private String code;
    private String libelle;
    private String route;
    private String icon;
    private Integer ordre;
    private Boolean visible;

    private Long parentId;
    private String parentCode;

    private Long environmentTypeId;
    private String environmentTypeCode;
}
