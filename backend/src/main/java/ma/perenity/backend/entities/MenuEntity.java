package ma.perenity.backend.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "MENU")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 100)
    private String libelle;

    @Column(length = 200)
    private String route;

    @Column(length = 100)
    private String icon;

    private Integer ordre;

    @Column(nullable = false)
    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")

    private MenuEntity parent;

    @OneToMany(mappedBy = "parent")

    @EqualsAndHashCode.Exclude
    private List<MenuEntity> children;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENV_TYPE_ID")

    private EnvironmentTypeEntity environmentType;

    @OneToMany(mappedBy = "menu")

    @EqualsAndHashCode.Exclude
    private List<RoleEntity> roles;
}