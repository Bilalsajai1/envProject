package ma.perenity.backend.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;

    private String code;
    private String firstName;
    private String lastName;
    private String email;

    private Boolean actif;

    private Long profilId;
    private String profilCode;
    private String profilLibelle;
}
