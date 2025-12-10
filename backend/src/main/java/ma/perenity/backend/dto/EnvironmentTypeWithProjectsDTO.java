package ma.perenity.backend.dto;

import lombok.Builder;
import lombok.Data;
import ma.perenity.backend.entities.enums.ActionType;

import java.util.List;

@Data
@Builder
public class EnvironmentTypeWithProjectsDTO {
    private Long id;
    private String code;
    private String libelle;
    private Boolean actif;
    private List<ActionType> allowedActions;
    private List<ProjectWithActionsDTO> projects;
}
