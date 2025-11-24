package ma.perenity.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthContextDTO {

    private Long userId;
    private String username;
    private String email;

    private String profilCode;
    private String profilLibelle;

    private List<String> roles;
    private List<MenuDTO> menus;
}
