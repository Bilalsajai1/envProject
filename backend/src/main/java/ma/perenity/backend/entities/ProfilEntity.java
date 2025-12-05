package ma.perenity.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "PROFIL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean admin = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean actif = true;

    @OneToMany(mappedBy = "profil")
    private List<UtilisateurEntity> utilisateurs;
    @Column(name = "keycloak_group_id", length = 100)
    private String keycloakGroupId;
    @OneToMany(mappedBy = "profil")
    private List<ProfilRoleEntity> profilRoles;
}
