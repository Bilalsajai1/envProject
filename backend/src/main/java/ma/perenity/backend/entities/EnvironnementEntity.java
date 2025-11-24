package ma.perenity.backend.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ENVIRONNEMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironnementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean actif = true;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJET_ID", nullable = false)
    private ProjetEntity projet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_ID", nullable = false)
    private EnvironmentTypeEntity type;

    @OneToMany(mappedBy = "environnement")
    private List<EnvApplicationEntity> envApplications;

    @OneToMany(mappedBy = "environnement")

    private List<RoleEntity> roles;
}