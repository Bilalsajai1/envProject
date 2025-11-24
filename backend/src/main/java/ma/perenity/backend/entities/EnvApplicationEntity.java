package ma.perenity.backend.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "ENV_APPLICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENV_ID", nullable = false)
    private EnvironnementEntity environnement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_ID", nullable = false)
    @ToString.Exclude
    private ApplicationEntity application;

    private String protocole;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String url;
    private LocalDateTime dateDerniereLivraison;
    private String description;

    @Column(nullable = false)
    private Boolean actif = true;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}