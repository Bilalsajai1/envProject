package ma.perenity.backend.dto;

import lombok.Data;

@Data
public class ProfilCreateUpdateDTO {

    private String code;
    private String libelle;
    private String description;

    private Boolean admin;
    private Boolean actif;
}
