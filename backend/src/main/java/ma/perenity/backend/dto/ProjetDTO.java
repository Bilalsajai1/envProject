package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjetDTO {

    private Long id;

    @NotBlank
    private String code;

    @NotBlank
    private String libelle;

    private String description;

    private Boolean actif;

    private List<String> envTypeCodes;

    private String envTypeCode;
}
