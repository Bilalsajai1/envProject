package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuCreateUpdateDTO {

    @NotBlank
    private String code;

    @NotBlank
    private String libelle;

    private String route;
    private String icon;
    private Integer ordre;
    private Boolean visible;

    private Long parentId;
    private Long environmentTypeId;
}
