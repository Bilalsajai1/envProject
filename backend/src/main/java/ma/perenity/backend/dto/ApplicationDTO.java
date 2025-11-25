package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDTO {
    private Long id;
    @NotBlank()
    private String code;
    @NotBlank()
    private String libelle;
    @Size(max = 255)
    private String description;
    private Boolean actif;
}
