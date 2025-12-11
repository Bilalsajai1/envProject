package ma.perenity.backend.dto;

import lombok.Data;

@Data
public class ProfilDTO {

    private Long id;

    private String code;
    private String libelle;
    private String description;

    private Boolean admin;
    private Boolean actif;

    private Integer nbUsers;
    private Integer nbActiveUsers;
}
