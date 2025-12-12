package ma.perenity.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "PROJET")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 150)
    private String libelle;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean actif = true;

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "PROJET_ENV_TYPE", joinColumns = @JoinColumn(name = "projet_id"), inverseJoinColumns = @JoinColumn(name = "env_type_id")
    )
    private Set<EnvironmentTypeEntity> environmentTypes = new HashSet<>();

    @OneToMany(mappedBy = "projet")
    private List<EnvironnementEntity> environnements;
}
