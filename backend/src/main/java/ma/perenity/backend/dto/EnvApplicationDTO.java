package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvApplicationDTO {

    private Long id;
    @NotBlank()
    private Long environnementId;
    @NotBlank()
    private Long applicationId;


    private String applicationCode;
    private String applicationLibelle;

    private String protocole;
    private String host;
    private Integer port;
    private String url;
    private String username;
    private String password;       // en entrée
    private String passwordMasked; // en sortie

    private String description;
    private Boolean actif;

    private LocalDateTime dateDerniereLivraison;
}
