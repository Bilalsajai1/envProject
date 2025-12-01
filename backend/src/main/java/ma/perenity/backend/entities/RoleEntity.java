package ma.perenity.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import ma.perenity.backend.entities.enums.ActionType;

import java.util.List;

@Entity
@Table(name = "ROLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 150)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionType action;

    @Builder.Default
    @Column(nullable = false)
    private Boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MENU_ID")
    private MenuEntity menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENV_ID")
    private EnvironnementEntity environnement;

    /**
     * Nouveau : rôle lié à un projet spécifique (ex: PROJ_MANAR_V8_CONSULT).
     * Peut être null pour les rôles qui ne sont pas portés par projet.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJET_ID")
    private ProjetEntity projet;

    @OneToMany(mappedBy = "role")
    private List<ProfilRoleEntity> profilRoles;
}
