package ma.perenity.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import ma.perenity.backend.entities.enums.ActionType;

import java.time.LocalDateTime;
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
    private String code; // ex: ENV_EDITION_CONSULT, ENV_DEV11_CREATE

    @Column(length = 150)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionType action;

    @Column(nullable = false)
    private Boolean actif = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MENU_ID")
    private MenuEntity menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENV_ID")
    private EnvironnementEntity environnement;

    @OneToMany(mappedBy = "role")
    private List<ProfilRoleEntity> profilRoles;
}