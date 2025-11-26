package ma.perenity.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvApplicationDTO {

    private Long id;

    @NotNull()
    @Positive()
    private Long environnementId;

    @NotNull()
    @Positive()
    private Long applicationId;

    private String applicationCode;
    private String applicationLibelle;

    private String protocole;
    private String host;
    private Integer port;
    private String url;
    private String username;
    private String password;
    private String passwordMasked;

    private String description;
    private Boolean actif;

    private LocalDateTime dateDerniereLivraison;
}
