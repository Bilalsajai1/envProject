package ma.perenity.backend.dto;

import lombok.Data;
import ma.perenity.backend.entities.enums.ActionType;

import java.util.List;

@Data
public class EnvTypePermissionUpdateDTO {
    private String envTypeCode;
    private List<ActionType> actions;
}
