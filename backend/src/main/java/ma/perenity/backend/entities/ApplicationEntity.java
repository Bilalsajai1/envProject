package ma.perenity.backend.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "APPLICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 100)
    private String libelle;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean actif = true;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "application")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EnvApplicationEntity> envApplications;
}