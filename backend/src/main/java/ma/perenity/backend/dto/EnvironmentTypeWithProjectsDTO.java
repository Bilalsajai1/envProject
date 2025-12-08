package ma.perenity.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EnvironmentTypeWithProjectsDTO {
    private Long id;
    private String code;
    private String libelle;
    private Boolean actif;
    private List<ProjectWithActionsDTO> projects;
}