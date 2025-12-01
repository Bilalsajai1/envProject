/*
package ma.perenity.backend.config;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.entities.*;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProfilRepository profilRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final ProjetRepository projetRepository;
    private final EnvironnementRepository environnementRepository;
    private final ApplicationRepository applicationRepository;
    private final EnvApplicationRepository envApplicationRepository;
    private final RoleRepository roleRepository;
    private final ProfilRoleRepository profilRoleRepository;

    @Bean
    CommandLineRunner initData() {
        return args -> {

            // Si déjà initialisé, on ne refait rien
            if (profilRepository.count() > 0) {
                return;
            }

            // =========================
            // 1. TYPES D’ENVIRONNEMENT
            // =========================
            EnvironmentTypeEntity edition = environmentTypeRepository.save(
                    EnvironmentTypeEntity.builder()
                            .code("EDITION")
                            .libelle("Environnement Édition")
                            .actif(true)
                            .build()
            );

            EnvironmentTypeEntity integration = environmentTypeRepository.save(
                    EnvironmentTypeEntity.builder()
                            .code("INTEGRATION")
                            .libelle("Environnement Intégration")
                            .actif(true)
                            .build()
            );

            EnvironmentTypeEntity client = environmentTypeRepository.save(
                    EnvironmentTypeEntity.builder()
                            .code("CLIENT")
                            .libelle("Environnement Client")
                            .actif(true)
                            .build()
            );

            // =========================
            // 2. PROJETS
            // =========================
            ProjetEntity manarV8 = projetRepository.save(
                    ProjetEntity.builder()
                            .code("MANAR_V8")
                            .libelle("Manar v8")
                            .description("Projet Manar v8 (Edition & Intégration)")
                            .actif(true)
                            .build()
            );

            ProjetEntity manarPortailClient = projetRepository.save(
                    ProjetEntity.builder()
                            .code("MANAR_PORTAIL_CLIENT")
                            .libelle("Manar Portail Client")
                            .description("Portail client - Environnement CLIENT")
                            .actif(true)
                            .build()
            );

            ProjetEntity manarLicenseGenerator = projetRepository.save(
                    ProjetEntity.builder()
                            .code("MANAR_LICENSE_GENERATOR")
                            .libelle("Manar License Generator")
                            .description("Portail Générateur de licences (Edition)")
                            .actif(true)
                            .build()
            );

            // =========================
            // 3. ENVIRONNEMENTS
            // =========================
            // --- Projet MANAR_V8 - TYPE EDITION ---
            EnvironnementEntity edDev = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("ED_DEV")
                            .libelle("Développement")
                            .description("Développement (11)")
                            .actif(true)
                            .projet(manarV8)
                            .type(edition)
                            .build()
            );

            EnvironnementEntity edRecTnr = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("ED_REC_TNR")
                            .libelle("Recette & TNR")
                            .description("Recette & TNR (12)")
                            .actif(true)
                            .projet(manarV8)
                            .type(edition)
                            .build()
            );

            EnvironnementEntity edManarPgsql = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("ED_MANAR_PGSQL")
                            .libelle("Manar POSTGRESQL")
                            .description("Environnement Manar POSTGRESQL")
                            .actif(true)
                            .projet(manarV8)
                            .type(edition)
                            .build()
            );

            // --- Projet MANAR_V8 - TYPE INTEGRATION ---
            EnvironnementEntity intEnvTestClient = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("INT_ENV_TEST_CLIENT")
                            .libelle("Env test Client")
                            .description("Env test Client (10.10.10.xx)")
                            .actif(true)
                            .projet(manarV8)
                            .type(integration)
                            .build()
            );

            EnvironnementEntity intDemoProd = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("INT_DEMO_PROD")
                            .libelle("DEMO - PROD")
                            .description("DEMO - PROD (10.10.10.180)")
                            .actif(true)
                            .projet(manarV8)
                            .type(integration)
                            .build()
            );

            EnvironnementEntity intDemoRisk = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("INT_DEMO_RISK")
                            .libelle("DEMO - RISK")
                            .description("DEMO - RISK (10.10.10.170)")
                            .actif(true)
                            .projet(manarV8)
                            .type(integration)
                            .build()
            );

            EnvironnementEntity intProdHttps = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("INT_PROD_HTTPS")
                            .libelle("Prod HTTPS")
                            .description("Prod HTTPS (13)")
                            .actif(true)
                            .projet(manarV8)
                            .type(integration)
                            .build()
            );

            // --- Projet MANAR_PORTAIL_CLIENT - TYPE CLIENT ---
            EnvironnementEntity cliDev = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("CLI_DEV")
                            .libelle("Env Dev")
                            .description("Environnement Dev Portail Client")
                            .actif(true)
                            .projet(manarPortailClient)
                            .type(client)
                            .build()
            );

            EnvironnementEntity cliUatUcm = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("CLI_UAT_UCM")
                            .libelle("ENV UAT UCM")
                            .description("Environnement UAT UCM Portail Client")
                            .actif(true)
                            .projet(manarPortailClient)
                            .type(client)
                            .build()
            );

            EnvironnementEntity cliProdUcm = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("CLI_PROD_UCM")
                            .libelle("ENV PROD UCM")
                            .description("Environnement PROD UCM Portail Client")
                            .actif(true)
                            .projet(manarPortailClient)
                            .type(client)
                            .build()
            );

            // --- Projet MANAR_LICENSE_GENERATOR - TYPE EDITION ---
            EnvironnementEntity licProd = environnementRepository.save(
                    EnvironnementEntity.builder()
                            .code("LIC_PROD")
                            .libelle("Portail Générateur licence")
                            .description("PROD Lot-1 V8.0.X")
                            .actif(true)
                            .projet(manarLicenseGenerator)
                            .type(edition)
                            .build()
            );

            // =========================
            // 4. APPLICATIONS
            // =========================
            ApplicationEntity appManarBackend = applicationRepository.save(
                    ApplicationEntity.builder()
                            .code("MANAR_BACKEND")
                            .libelle("Manar Backend")
                            .description("Backend Manar (API)")
                            .actif(true)
                            .build()
            );

            ApplicationEntity appManarFront = applicationRepository.save(
                    ApplicationEntity.builder()
                            .code("MANAR_FRONT")
                            .libelle("Manar Front")
                            .description("Interface Web Manar")
                            .actif(true)
                            .build()
            );

            ApplicationEntity appPortailClient = applicationRepository.save(
                    ApplicationEntity.builder()
                            .code("PORTAIL_CLIENT")
                            .libelle("Portail Client")
                            .description("Application Portail Client")
                            .actif(true)
                            .build()
            );

            ApplicationEntity appLicenseGenerator = applicationRepository.save(
                    ApplicationEntity.builder()
                            .code("LICENSE_GENERATOR")
                            .libelle("Générateur de licences")
                            .description("Portail Générateur de licences")
                            .actif(true)
                            .build()
            );

            // =========================
            // 5. ENV_APPLICATIONS (exemples)
            // =========================
            envApplicationRepository.save(
                    EnvApplicationEntity.builder()
                            .environnement(edDev)
                            .application(appManarBackend)
                            .protocole("http")
                            .host("10.10.10.11")
                            .port(8080)
                            .url("http://10.10.10.11:8080/")
                            .username("svc-manar")
                            .password("pwd")
                            .description("Instance Manar Backend - Dev")
                            .dateDerniereLivraison(LocalDateTime.now().minusDays(3))
                            .actif(true)
                            .build()
            );

            envApplicationRepository.save(
                    EnvApplicationEntity.builder()
                            .environnement(intDemoProd)
                            .application(appManarFront)
                            .protocole("http")
                            .host("10.10.10.180")
                            .port(8080)
                            .url("http://10.10.10.180:8080/")
                            .username("svc-manar")
                            .password("pwd")
                            .description("Manar Front - DEMO PROD")
                            .dateDerniereLivraison(LocalDateTime.now().minusDays(1))
                            .actif(true)
                            .build()
            );

            envApplicationRepository.save(
                    EnvApplicationEntity.builder()
                            .environnement(cliProdUcm)
                            .application(appPortailClient)
                            .protocole("https")
                            .host("ucm.prod.local")
                            .port(443)
                            .url("https://ucm.prod.local/")
                            .username("svc-portail")
                            .password("pwd")
                            .description("Portail Client - PROD UCM")
                            .dateDerniereLivraison(LocalDateTime.now().minusWeeks(1))
                            .actif(true)
                            .build()
            );

            envApplicationRepository.save(
                    EnvApplicationEntity.builder()
                            .environnement(licProd)
                            .application(appLicenseGenerator)
                            .protocole("https")
                            .host("license.prod.local")
                            .port(443)
                            .url("https://license.prod.local/")
                            .username("svc-license")
                            .password("pwd")
                            .description("Générateur de licences - PROD")
                            .dateDerniereLivraison(LocalDateTime.now().minusDays(5))
                            .actif(true)
                            .build()
            );

            // =========================
            // 6. ROLES ENV_* (par type d'env)
            // =========================
            ActionType[] actions = ActionType.values();

            for (EnvironmentTypeEntity t : List.of(edition, integration, client)) {
                for (ActionType action : actions) {
                    String code = "ENV_" + t.getCode().toUpperCase() + "_" + action.name(); // ex: ENV_EDITION_CONSULT

                    if (roleRepository.existsByCode(code)) {
                        continue;
                    }

                    RoleEntity role = RoleEntity.builder()
                            .code(code)
                            .libelle("Droit " + action.name() + " sur type " + t.getCode())
                            .action(action)
                            .actif(true)
                            .build();

                    roleRepository.save(role);
                }
            }

            // =========================
            // 7. ROLES PROJ_* (par projet)
            // =========================
            List<ProjetEntity> allProjects = projetRepository.findAll();

            for (ProjetEntity p : allProjects) {
                for (ActionType action : actions) {
                    String code = "PROJ_" + p.getCode().toUpperCase() + "_" + action.name(); // ex: PROJ_MANAR_V8_CONSULT

                    if (roleRepository.existsByCode(code)) {
                        continue;
                    }

                    RoleEntity role = RoleEntity.builder()
                            .code(code)
                            .libelle("Droit " + action.name() + " sur projet " + p.getCode())
                            .action(action)
                            .actif(true)
                            .projet(p)
                            .build();

                    roleRepository.save(role);
                }
            }

            // =========================
            // 8. PROFIL ADMIN
            // =========================
            ProfilEntity adminProfil = profilRepository.save(
                    ProfilEntity.builder()
                            .code("ADMIN")
                            .libelle("Administrateur")
                            .description("Profil administrateur global")
                            .admin(true)
                            .actif(true)
                            .build()
            );

            // =========================
            // 9. ASSIGNER TOUS LES ROLES AU PROFIL ADMIN
            // =========================
            List<RoleEntity> allRoles = roleRepository.findAll();
            for (RoleEntity role : allRoles) {
                ProfilRoleEntity pr = ProfilRoleEntity.builder()
                        .profil(adminProfil)
                        .role(role)
                        .build();
                profilRoleRepository.save(pr);
            }

            // =========================
            // 🔟 UTILISATEUR ADMIN LOCAL
            // =========================
            // ⚠️ Met ici exactement le même email que l'utilisateur Keycloak
            // (voir explication plus bas)
            UtilisateurEntity adminUser = UtilisateurEntity.builder()
                    .code("ADMIN")
                    .firstName("bilal")
                    .lastName("sajai")
                    .email("bilalsajai1966@gmail.com")  // <-- A ALIGNER AVEC KEYCLOAK
                    .actif(true)
                    .profil(adminProfil)
                    .build();

            utilisateurRepository.save(adminUser);
        };
    }
}
*/
