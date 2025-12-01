package ma.perenity.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.perenity.backend.entities.enums.ActionType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPermissionDTO {
    private Long id;
    private String code;
    private String libelle;
    private Boolean actif;

    private List<ActionType> allowedActions;
}
