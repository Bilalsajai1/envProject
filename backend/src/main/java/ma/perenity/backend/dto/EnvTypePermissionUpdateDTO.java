package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ma.perenity.backend.entities.enums.ActionType;

import java.util.List;

@Data
public class EnvTypePermissionUpdateDTO {

    @NotBlank
    private String envTypeCode;

    @NotNull
    private List<ActionType> actions;
}
