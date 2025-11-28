/*

package ma.perenity.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.perenity.backend.entities.*;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final ProfilRepository profilRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;
    private final ProfilRoleRepository profilRoleRepository;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final ProjetRepository projetRepository;
    private final EnvironnementRepository environnementRepository;
    private final ApplicationRepository applicationRepository;
    private final EnvApplicationRepository envApplicationRepository;

    private LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private LocalDateTime dMinus(LocalDateTime base, int days) {
        return base.minusDays(days);
    }

    @Bean
    CommandLineRunner initData() {
        return args -> {

            // Si déjà initialisé, ne rien faire
            if (profilRepository.count() > 0) {
                log.info(">>> Données déjà initialisées, on ne réinjecte pas.");
                return;
            }

            LocalDateTime now = now();

            EnvironmentTypeEntity editionType = EnvironmentTypeEntity.builder()
                    .code("EDITION")
                    .libelle("Environnement Édition")
                    .actif(true)
                    .build();

            EnvironmentTypeEntity integrationType = EnvironmentTypeEntity.builder()
                    .code("INTEGRATION")
                    .libelle("Environnement Intégration")
                    .actif(true)
                    .build();

            EnvironmentTypeEntity clientType = EnvironmentTypeEntity.builder()
                    .code("CLIENT")
                    .libelle("Environnement Client")
                    .actif(true)
                    .build();

            environmentTypeRepository.saveAll(List.of(editionType, integrationType, clientType));


            MenuEntity menuEdition = MenuEntity.builder()
                    .code("MENU_EDITION")
                    .libelle("Environnement Édition")
                    .route("/edition")
                    .icon("env_edition")
                    .ordre(1)
                    .visible(true)
                    .environmentType(editionType)
                    .build();

            MenuEntity menuIntegration = MenuEntity.builder()
                    .code("MENU_INTEGRATION")
                    .libelle("Environnement Intégration")
                    .route("/integration")
                    .icon("env_integration")
                    .ordre(2)
                    .visible(true)
                    .environmentType(integrationType)
                    .build();

            MenuEntity menuClient = MenuEntity.builder()
                    .code("MENU_CLIENT")
                    .libelle("Environnement Client")
                    .route("/client")
                    .icon("env_client")
                    .ordre(3)
                    .visible(true)
                    .environmentType(clientType)
                    .build();

            menuRepository.saveAll(List.of(menuEdition, menuIntegration, menuClient));


            ProfilEntity profilAdmin = ProfilEntity.builder()
                    .code("ADMIN")
                    .libelle("Administrateur")
                    .description("Profil administrateur")
                    .admin(true)
                    .actif(true)
                        // ✅ Initialisé explicitement
                        // ✅ Initialisé explicitement
                    
                    
                    .build();

            ProfilEntity profilDev = ProfilEntity.builder()
                    .code("DEV")
                    .libelle("Développeur")
                    .description("Développeur interne")
                    .admin(false)
                    .actif(true)

                    .build();

            ProfilEntity profilConsult = ProfilEntity.builder()
                    .code("CONSULT")
                    .libelle("Consultant")
                    .description("Consultation uniquement")
                    .admin(false)
                    .actif(true)
                    
                    
                    
                    
                    .build();

            profilRepository.saveAll(List.of(profilAdmin, profilDev, profilConsult));


            RoleEntity roleEditionConsult = RoleEntity.builder()
                    .code("ENV_EDITION_CONSULT")
                    .libelle("Consulter environnements édition")
                    .action(ActionType.CONSULT)
                    .actif(true)
                    
                    .menu(menuEdition)
                    .build();

            RoleEntity roleEditionUpdate = RoleEntity.builder()
                    .code("ENV_EDITION_UPDATE")
                    .libelle("Modifier environnements édition")
                    .action(ActionType.UPDATE)
                    .actif(true)
                    
                    .menu(menuEdition)
                    .build();

            RoleEntity roleIntegrationConsult = RoleEntity.builder()
                    .code("ENV_INTEGRATION_CONSULT")
                    .libelle("Consulter environnements intégration")
                    .action(ActionType.CONSULT)
                    .actif(true)
                    
                    .menu(menuIntegration)
                    .build();

            RoleEntity roleClientConsult = RoleEntity.builder()
                    .code("ENV_CLIENT_CONSULT")
                    .libelle("Consulter environnements client")
                    .action(ActionType.CONSULT)
                    .actif(true)
                    
                    .menu(menuClient)
                    .build();

            roleRepository.saveAll(List.of(
                    roleEditionConsult,
                    roleEditionUpdate,
                    roleIntegrationConsult,
                    roleClientConsult
            ));


            // ADMIN : a tous les rôles
            for (RoleEntity r : roleRepository.findAll()) {
                profilRoleRepository.save(
                        ProfilRoleEntity.builder()
                                .profil(profilAdmin)
                                .role(r)
                                .build()
                );
            }

            // DEV : juste édition consult + update
            profilRoleRepository.save(
                    ProfilRoleEntity.builder()
                            .profil(profilDev)
                            .role(roleEditionConsult)
                            .build()
            );
            profilRoleRepository.save(
                    ProfilRoleEntity.builder()
                            .profil(profilDev)
                            .role(roleEditionUpdate)
                            .build()
            );

            // CONSULT : seulement consult sur les trois types
            profilRoleRepository.save(
                    ProfilRoleEntity.builder()
                            .profil(profilConsult)
                            .role(roleEditionConsult)
                            .build()
            );
            profilRoleRepository.save(
                    ProfilRoleEntity.builder()
                            .profil(profilConsult)
                            .role(roleIntegrationConsult)
                            .build()
            );
            profilRoleRepository.save(
                    ProfilRoleEntity.builder()
                            .profil(profilConsult)
                            .role(roleClientConsult)
                            .build()
            );




            // Projet MANAR V8 (Edition + Intégration)
            ProjetEntity manarV8 = ProjetEntity.builder()
                    .code("MANAR_V8")
                    .libelle("Manar V8")
                    .description("Projet Manar V8")
                    .actif(true)
                    
                    .build();

            // Projet Manar Portail Client (Client)
            ProjetEntity manarPortail = ProjetEntity.builder()
                    .code("MANAR_PORTAIL_CLIENT")
                    .libelle("Manar Portail Client")
                    .description("Portail Client Manar")
                    .actif(true)
                    
                    .build();

            projetRepository.saveAll(List.of(manarV8, manarPortail));



            // ---- Type EDITION : MANAR_V8 ----
            EnvironnementEntity envDev = EnvironnementEntity.builder()
                    .code("DEV")
                    .libelle("Développement")
                    .description("Environnement de développement Manar V8")
                    .actif(true)
                    
                    .projet(manarV8)
                    .type(editionType)
                    .build();

            EnvironnementEntity envRecetteTnr = EnvironnementEntity.builder()
                    .code("REC_TNR")
                    .libelle("Recette & TNR")
                    .description("Environnement de Recette & TNR Manar V8")
                    .actif(true)
                    
                    .projet(manarV8)
                    .type(editionType)
                    .build();

            EnvironnementEntity envManarPg = EnvironnementEntity.builder()
                    .code("MANAR_POSTGRESQL")
                    .libelle("Manar PostgreSQL")
                    .description("Environnement PostgreSQL Manar V8")
                    .actif(true)
                    
                    .projet(manarV8)
                    .type(editionType)
                    .build();

            // ---- Type INTEGRATION : MANAR_V8 ----
            EnvironnementEntity envTestClient = EnvironnementEntity.builder()
                    .code("INT_TEST_CLIENT")
                    .libelle("Env Test Client")
                    .description("Environnement de test client (intégration)")
                    .actif(true)
                    
                    .projet(manarV8)
                    .type(integrationType)
                    .build();

            EnvironnementEntity envDemoProd = EnvironnementEntity.builder()
                    .code("DEMO_PROD")
                    .libelle("DEMO - PROD")
                    .description("Environnement démo côté PROD")
                    .actif(true)
                    
                    .projet(manarV8)
                    .type(integrationType)
                    .build();

            EnvironnementEntity envDemoRisk = EnvironnementEntity.builder()
                    .code("DEMO_RISK")
                    .libelle("DEMO - RISK")
                    .description("Environnement démo risque")
                    .actif(true)
                    
                    .projet(manarV8)
                    .type(integrationType)
                    .build();

            EnvironnementEntity envProdHttps = EnvironnementEntity.builder()
                    .code("PROD_HTTPS")
                    .libelle("Prod HTTPS")
                    .description("Environnement de production sécurisé HTTPS")
                    .actif(true)
                    
                    .projet(manarV8)
                    .type(integrationType)
                    .build();

            // ---- Type CLIENT : MANAR PORTAIL CLIENT ----
            EnvironnementEntity envDevClient = EnvironnementEntity.builder()
                    .code("DEV_CLIENT")
                    .libelle("Env Dev")
                    .description("Environnement Dev Portail Client")
                    .actif(true)
                    
                    .projet(manarPortail)
                    .type(clientType)
                    .build();

            EnvironnementEntity envUatUcm = EnvironnementEntity.builder()
                    .code("UAT_UCM")
                    .libelle("ENV UAT UCM")
                    .description("Environnement UAT UCM Portail Client")
                    .actif(true)
                    
                    .projet(manarPortail)
                    .type(clientType)
                    .build();

            EnvironnementEntity envProdUcm = EnvironnementEntity.builder()
                    .code("PROD_UCM")
                    .libelle("ENV PROD UCM")
                    .description("Environnement PROD UCM Portail Client")
                    .actif(true)
                    
                    .projet(manarPortail)
                    .type(clientType)
                    .build();

            environnementRepository.saveAll(List.of(
                    envDev, envRecetteTnr, envManarPg,
                    envTestClient, envDemoProd, envDemoRisk, envProdHttps,
                    envDevClient, envUatUcm, envProdUcm
            ));




            // Manar V8 "core"
            ApplicationEntity appFrontManar = ApplicationEntity.builder()
                    .code("FRONT_MANAR")
                    .libelle("Frontend Manar")
                    .description("UI Manar V8")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appBackManar = ApplicationEntity.builder()
                    .code("BACK_MANAR")
                    .libelle("Backend Manar")
                    .description("API Manar V8")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appMiddleware = ApplicationEntity.builder()
                    .code("MIDDLEWARE")
                    .libelle("Middleware")
                    .description("Middleware Manar")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appOpenApi = ApplicationEntity.builder()
                    .code("OPENAPI")
                    .libelle("OpenAPI")
                    .description("Documentation OpenAPI / Gateway")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appKeycloakManar = ApplicationEntity.builder()
                    .code("KEYCLOAK_MANAR")
                    .libelle("Keycloak Manar")
                    .description("SSO Manar")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appPortainerManar = ApplicationEntity.builder()
                    .code("PORTAINER_MANAR")
                    .libelle("Portainer Manar")
                    .description("Portainer (cluster Manar)")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appJenkins = ApplicationEntity.builder()
                    .code("JENKINS")
                    .libelle("Jenkins")
                    .description("CI/CD Jenkins")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appDbManar = ApplicationEntity.builder()
                    .code("DB_MANAR")
                    .libelle("Database Manar")
                    .description("Base de données Manar")
                    .actif(true)
                    
                    .build();

            // Spécifiques Middleware côté prod intégration
            ApplicationEntity appKeycloakMw = ApplicationEntity.builder()
                    .code("KEYCLOAK_MW")
                    .libelle("Keycloak Middleware")
                    .description("Keycloak Middleware")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appPortainerMw = ApplicationEntity.builder()
                    .code("PORTAINER_MW")
                    .libelle("Portainer Middleware")
                    .description("Portainer Middleware")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appDbMw = ApplicationEntity.builder()
                    .code("DB_MW")
                    .libelle("Database Middleware")
                    .description("Base de données Middleware")
                    .actif(true)
                    
                    .build();

            // Portail Client
            ApplicationEntity appFrontClient = ApplicationEntity.builder()
                    .code("FRONT_CLIENT")
                    .libelle("Frontend Client")
                    .description("Frontend portail client")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appFrontAdmin = ApplicationEntity.builder()
                    .code("FRONT_ADMIN")
                    .libelle("Frontend Admin")
                    .description("Frontend admin portail client")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appBackPortail = ApplicationEntity.builder()
                    .code("BACK_PORTAIL")
                    .libelle("Backend Portail")
                    .description("Backend portail client")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appKeycloakClient = ApplicationEntity.builder()
                    .code("KEYCLOAK_CLIENT")
                    .libelle("Keycloak Client")
                    .description("SSO portail client")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appPortainerFront = ApplicationEntity.builder()
                    .code("PORTAINER_FRONT")
                    .libelle("Portainer Frontend")
                    .description("Portainer pour frontend client")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appPortainerBack = ApplicationEntity.builder()
                    .code("PORTAINER_BACK")
                    .libelle("Portainer Backend")
                    .description("Portainer pour backend portail")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appDbClient = ApplicationEntity.builder()
                    .code("DB_CLIENT")
                    .libelle("Database Portail Client")
                    .description("Base de données portail client")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appMachineDesktopUcm = ApplicationEntity.builder()
                    .code("MACHINE_DESKTOP_UCM")
                    .libelle("Machine Desktop UCM")
                    .description("Machine Desktop UCM")
                    .actif(true)
                    
                    .build();

            ApplicationEntity appMachineManar = ApplicationEntity.builder()
                    .code("MACHINE_MANAR")
                    .libelle("Machine Manar")
                    .description("Machine Manar externe")
                    .actif(true)
                    
                    .build();

            applicationRepository.saveAll(List.of(
                    appFrontManar, appBackManar, appMiddleware, appOpenApi,
                    appKeycloakManar, appPortainerManar, appJenkins, appDbManar,
                    appKeycloakMw, appPortainerMw, appDbMw,
                    appFrontClient, appFrontAdmin, appBackPortail,
                    appKeycloakClient, appPortainerFront, appPortainerBack,
                    appDbClient, appMachineDesktopUcm, appMachineManar
            ));



            // ---- EDITION - DEV (8 applications) ----
            EnvApplicationEntity devFront = EnvApplicationEntity.builder()
                    .environnement(envDev)
                    .application(appFrontManar)
                    .protocole("http")
                    .host("10.0.0.11")
                    .port(4200)
                    .url("http://10.0.0.11:4200/")
                    .username("dev")
                    .password("devpwd")
                    .dateDerniereLivraison(dMinus(now, 3))
                    .description("Frontend Manar DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devBack = EnvApplicationEntity.builder()
                    .environnement(envDev)
                    .application(appBackManar)
                    .protocole("http")
                    .host("10.0.0.11")
                    .port(8080)
                    .url("http://10.0.0.11:8080/")
                    .username("dev")
                    .password("devpwd")
                    .dateDerniereLivraison(dMinus(now, 4))
                    .description("Backend Manar DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devMw = EnvApplicationEntity.builder()
                    .environnement(envDev)
                    .application(appMiddleware)
                    .protocole("http")
                    .host("10.0.0.12")
                    .port(8081)
                    .url("http://10.0.0.12:8081/")
                    .username("dev")
                    .password("devpwd")
                    .dateDerniereLivraison(dMinus(now, 5))
                    .description("Middleware DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devOpenApi = EnvApplicationEntity.builder()
                    .environnement(envDev)
                    .application(appOpenApi)
                    .protocole("http")
                    .host("10.0.0.13")
                    .port(8083)
                    .url("http://10.0.0.13:8083/")
                    .username("dev")
                    .password("devpwd")
                    .dateDerniereLivraison(dMinus(now, 2))
                    .description("OpenAPI DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devKeycloak = EnvApplicationEntity.builder()
                    .environnement(envDev)
                    .application(appKeycloakManar)
                    .protocole("http")
                    .host("10.0.0.14")
                    .port(8080)
                    .url("http://10.0.0.14:8080/")
                    .username("keycloak_dev")
                    .password("kcdev123")
                    .dateDerniereLivraison(dMinus(now, 7))
                    .description("Keycloak Manar DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devPortainer = EnvApplicationEntity.builder()
                    .environnement(envDev)
                    .application(appPortainerManar)
                    .protocole("http")
                    .host("10.0.0.15")
                    .port(9000)
                    .url("http://10.0.0.15:9000/")
                    .username("admin")
                    .password("adminpwd")
                    .dateDerniereLivraison(dMinus(now, 10))
                    .description("Portainer Manar DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devJenkins = EnvApplicationEntity.builder()
                    .environnement(envDev)
                    .application(appJenkins)
                    .protocole("http")
                    .host("10.0.0.16")
                    .port(8085)
                    .url("http://10.0.0.16:8085/")
                    .username("jenkins")
                    .password("jenkins123")
                    .dateDerniereLivraison(dMinus(now, 1))
                    .description("Jenkins CI/CD DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devDb = EnvApplicationEntity.builder()
                    .environnement(envDev)
                    .application(appDbManar)
                    .protocole("postgresql")
                    .host("10.0.0.20")
                    .port(5432)
                    .url("jdbc:postgresql://10.0.0.20:5432/manar_dev")
                    .username("manar_dev")
                    .password("dbdev123")
                    .dateDerniereLivraison(dMinus(now, 15))
                    .description("Base Données DEV Manar (PostgreSQL)")
                    .actif(true)
                    
                    .build();

            // ---- EDITION - RECETTE & TNR (7 applications, sans Jenkins) ----
            EnvApplicationEntity recFront = EnvApplicationEntity.builder()
                    .environnement(envRecetteTnr)
                    .application(appFrontManar)
                    .protocole("http")
                    .host("10.0.0.31")
                    .port(4200)
                    .url("http://10.0.0.31:4200/")
                    .username("recette")
                    .password("recette123")
                    .dateDerniereLivraison(dMinus(now, 6))
                    .description("Frontend Manar RECETTE")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity recBack = EnvApplicationEntity.builder()
                    .environnement(envRecetteTnr)
                    .application(appBackManar)
                    .protocole("http")
                    .host("10.0.0.31")
                    .port(8080)
                    .url("http://10.0.0.31:8080/")
                    .username("recette")
                    .password("recette123")
                    .dateDerniereLivraison(dMinus(now, 7))
                    .description("Backend Manar RECETTE")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity recMw = EnvApplicationEntity.builder()
                    .environnement(envRecetteTnr)
                    .application(appMiddleware)
                    .protocole("http")
                    .host("10.0.0.32")
                    .port(8081)
                    .url("http://10.0.0.32:8081/")
                    .username("recette")
                    .password("recette123")
                    .dateDerniereLivraison(dMinus(now, 8))
                    .description("Middleware RECETTE")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity recOpenApi = EnvApplicationEntity.builder()
                    .environnement(envRecetteTnr)
                    .application(appOpenApi)
                    .protocole("http")
                    .host("10.0.0.33")
                    .port(8083)
                    .url("http://10.0.0.33:8083/")
                    .username("recette")
                    .password("recette123")
                    .dateDerniereLivraison(dMinus(now, 5))
                    .description("OpenAPI RECETTE")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity recKeycloak = EnvApplicationEntity.builder()
                    .environnement(envRecetteTnr)
                    .application(appKeycloakManar)
                    .protocole("http")
                    .host("10.0.0.34")
                    .port(8080)
                    .url("http://10.0.0.34:8080/")
                    .username("kc_rec")
                    .password("kcrec123")
                    .dateDerniereLivraison(dMinus(now, 9))
                    .description("Keycloak Manar RECETTE")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity recPortainer = EnvApplicationEntity.builder()
                    .environnement(envRecetteTnr)
                    .application(appPortainerManar)
                    .protocole("http")
                    .host("10.0.0.35")
                    .port(9000)
                    .url("http://10.0.0.35:9000/")
                    .username("admin")
                    .password("adminrec")
                    .dateDerniereLivraison(dMinus(now, 12))
                    .description("Portainer Manar RECETTE")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity recDb = EnvApplicationEntity.builder()
                    .environnement(envRecetteTnr)
                    .application(appDbManar)
                    .protocole("postgresql")
                    .host("10.0.0.40")
                    .port(5432)
                    .url("jdbc:postgresql://10.0.0.40:5432/manar_recette")
                    .username("manar_rec")
                    .password("dbrec123")
                    .dateDerniereLivraison(dMinus(now, 20))
                    .description("Base Données RECETTE Manar (PostgreSQL)")
                    .actif(true)
                    
                    .build();

            // ---- EDITION - MANAR POSTGRESQL (7 applications, sans Jenkins) ----
            EnvApplicationEntity pgFront = EnvApplicationEntity.builder()
                    .environnement(envManarPg)
                    .application(appFrontManar)
                    .protocole("http")
                    .host("10.0.0.51")
                    .port(4200)
                    .url("http://10.0.0.51:4200/")
                    .username("pg")
                    .password("pgfront")
                    .dateDerniereLivraison(dMinus(now, 11))
                    .description("Frontend Manar PG")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity pgBack = EnvApplicationEntity.builder()
                    .environnement(envManarPg)
                    .application(appBackManar)
                    .protocole("http")
                    .host("10.0.0.51")
                    .port(8080)
                    .url("http://10.0.0.51:8080/")
                    .username("pg")
                    .password("pgback")
                    .dateDerniereLivraison(dMinus(now, 13))
                    .description("Backend Manar PG")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity pgMw = EnvApplicationEntity.builder()
                    .environnement(envManarPg)
                    .application(appMiddleware)
                    .protocole("http")
                    .host("10.0.0.52")
                    .port(8081)
                    .url("http://10.0.0.52:8081/")
                    .username("pg")
                    .password("pgmw")
                    .dateDerniereLivraison(dMinus(now, 14))
                    .description("Middleware Manar PG")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity pgOpenApi = EnvApplicationEntity.builder()
                    .environnement(envManarPg)
                    .application(appOpenApi)
                    .protocole("http")
                    .host("10.0.0.53")
                    .port(8083)
                    .url("http://10.0.0.53:8083/")
                    .username("pg")
                    .password("pgapi")
                    .dateDerniereLivraison(dMinus(now, 9))
                    .description("OpenAPI Manar PG")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity pgKeycloak = EnvApplicationEntity.builder()
                    .environnement(envManarPg)
                    .application(appKeycloakManar)
                    .protocole("http")
                    .host("10.0.0.54")
                    .port(8080)
                    .url("http://10.0.0.54:8080/")
                    .username("kc_pg")
                    .password("kcpg123")
                    .dateDerniereLivraison(dMinus(now, 18))
                    .description("Keycloak Manar PG")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity pgPortainer = EnvApplicationEntity.builder()
                    .environnement(envManarPg)
                    .application(appPortainerManar)
                    .protocole("http")
                    .host("10.0.0.55")
                    .port(9000)
                    .url("http://10.0.0.55:9000/")
                    .username("admin")
                    .password("adminpg")
                    .dateDerniereLivraison(dMinus(now, 16))
                    .description("Portainer Manar PG")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity pgDb = EnvApplicationEntity.builder()
                    .environnement(envManarPg)
                    .application(appDbManar)
                    .protocole("postgresql")
                    .host("10.0.0.60")
                    .port(5432)
                    .url("jdbc:postgresql://10.0.0.60:5432/manar_pg")
                    .username("manar_pg")
                    .password("dbpg123")
                    .dateDerniereLivraison(dMinus(now, 30))
                    .description("Base Données Manar PG (PostgreSQL)")
                    .actif(true)
                    
                    .build();

            // ---- INTEGRATION - Env Test Client / DEMO PROD / DEMO RISK (7 apps) ----
            EnvApplicationEntity intTestFront = EnvApplicationEntity.builder()
                    .environnement(envTestClient)
                    .application(appFrontManar)
                    .protocole("http")
                    .host("10.0.1.11")
                    .port(4200)
                    .url("http://10.0.1.11:4200/")
                    .username("int")
                    .password("intfront")
                    .dateDerniereLivraison(dMinus(now, 8))
                    .description("Frontend Manar INT TEST CLIENT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity intTestBack = EnvApplicationEntity.builder()
                    .environnement(envTestClient)
                    .application(appBackManar)
                    .protocole("http")
                    .host("10.0.1.11")
                    .port(8080)
                    .url("http://10.0.1.11:8080/")
                    .username("int")
                    .password("intback")
                    .dateDerniereLivraison(dMinus(now, 10))
                    .description("Backend Manar INT TEST CLIENT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity intTestMw = EnvApplicationEntity.builder()
                    .environnement(envTestClient)
                    .application(appMiddleware)
                    .protocole("http")
                    .host("10.0.1.12")
                    .port(8081)
                    .url("http://10.0.1.12:8081/")
                    .username("int")
                    .password("intmw")
                    .dateDerniereLivraison(dMinus(now, 11))
                    .description("Middleware INT TEST CLIENT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity intTestOpenApi = EnvApplicationEntity.builder()
                    .environnement(envTestClient)
                    .application(appOpenApi)
                    .protocole("http")
                    .host("10.0.1.13")
                    .port(8083)
                    .url("http://10.0.1.13:8083/")
                    .username("int")
                    .password("intapi")
                    .dateDerniereLivraison(dMinus(now, 9))
                    .description("OpenAPI INT TEST CLIENT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity intTestKeycloak = EnvApplicationEntity.builder()
                    .environnement(envTestClient)
                    .application(appKeycloakManar)
                    .protocole("http")
                    .host("10.0.1.14")
                    .port(8080)
                    .url("http://10.0.1.14:8080/")
                    .username("kc_int")
                    .password("kcint")
                    .dateDerniereLivraison(dMinus(now, 14))
                    .description("Keycloak Manar INT TEST CLIENT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity intTestPortainer = EnvApplicationEntity.builder()
                    .environnement(envTestClient)
                    .application(appPortainerManar)
                    .protocole("http")
                    .host("10.0.1.15")
                    .port(9000)
                    .url("http://10.0.1.15:9000/")
                    .username("admin")
                    .password("adminint")
                    .dateDerniereLivraison(dMinus(now, 12))
                    .description("Portainer Manar INT TEST CLIENT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity intTestDb = EnvApplicationEntity.builder()
                    .environnement(envTestClient)
                    .application(appDbManar)
                    .protocole("postgresql")
                    .host("10.0.1.20")
                    .port(5432)
                    .url("jdbc:postgresql://10.0.1.20:5432/manar_int_test")
                    .username("manar_int")
                    .password("dbint")
                    .dateDerniereLivraison(dMinus(now, 21))
                    .description("DB Manar INT TEST CLIENT")
                    .actif(true)
                    
                    .build();

            // DEMO PROD
            EnvApplicationEntity demoProdFront = EnvApplicationEntity.builder()
                    .environnement(envDemoProd)
                    .application(appFrontManar)
                    .protocole("http")
                    .host("10.0.1.31")
                    .port(80)
                    .url("http://10.0.1.31/")
                    .username("demo")
                    .password("demofront")
                    .dateDerniereLivraison(dMinus(now, 4))
                    .description("Frontend DEMO PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoProdBack = EnvApplicationEntity.builder()
                    .environnement(envDemoProd)
                    .application(appBackManar)
                    .protocole("http")
                    .host("10.0.1.31")
                    .port(8080)
                    .url("http://10.0.1.31:8080/")
                    .username("demo")
                    .password("demoback")
                    .dateDerniereLivraison(dMinus(now, 5))
                    .description("Backend DEMO PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoProdMw = EnvApplicationEntity.builder()
                    .environnement(envDemoProd)
                    .application(appMiddleware)
                    .protocole("http")
                    .host("10.0.1.32")
                    .port(8081)
                    .url("http://10.0.1.32:8081/")
                    .username("demo")
                    .password("demomw")
                    .dateDerniereLivraison(dMinus(now, 6))
                    .description("Middleware DEMO PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoProdOpenApi = EnvApplicationEntity.builder()
                    .environnement(envDemoProd)
                    .application(appOpenApi)
                    .protocole("http")
                    .host("10.0.1.33")
                    .port(8083)
                    .url("http://10.0.1.33:8083/")
                    .username("demo")
                    .password("demoapi")
                    .dateDerniereLivraison(dMinus(now, 3))
                    .description("OpenAPI DEMO PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoProdKeycloak = EnvApplicationEntity.builder()
                    .environnement(envDemoProd)
                    .application(appKeycloakManar)
                    .protocole("http")
                    .host("10.0.1.34")
                    .port(8080)
                    .url("http://10.0.1.34:8080/")
                    .username("kc_demo")
                    .password("kcdemo")
                    .dateDerniereLivraison(dMinus(now, 10))
                    .description("Keycloak DEMO PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoProdPortainer = EnvApplicationEntity.builder()
                    .environnement(envDemoProd)
                    .application(appPortainerManar)
                    .protocole("http")
                    .host("10.0.1.35")
                    .port(9000)
                    .url("http://10.0.1.35:9000/")
                    .username("admin")
                    .password("admindemo")
                    .dateDerniereLivraison(dMinus(now, 9))
                    .description("Portainer DEMO PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoProdDb = EnvApplicationEntity.builder()
                    .environnement(envDemoProd)
                    .application(appDbManar)
                    .protocole("postgresql")
                    .host("10.0.1.40")
                    .port(5432)
                    .url("jdbc:postgresql://10.0.1.40:5432/manar_demo_prod")
                    .username("manar_demo")
                    .password("dbdemo")
                    .dateDerniereLivraison(dMinus(now, 19))
                    .description("DB DEMO PROD")
                    .actif(true)
                    
                    .build();

            // DEMO RISK
            EnvApplicationEntity demoRiskFront = EnvApplicationEntity.builder()
                    .environnement(envDemoRisk)
                    .application(appFrontManar)
                    .protocole("http")
                    .host("10.0.1.51")
                    .port(80)
                    .url("http://10.0.1.51/")
                    .username("risk")
                    .password("riskfront")
                    .dateDerniereLivraison(dMinus(now, 8))
                    .description("Frontend DEMO RISK")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoRiskBack = EnvApplicationEntity.builder()
                    .environnement(envDemoRisk)
                    .application(appBackManar)
                    .protocole("http")
                    .host("10.0.1.51")
                    .port(8080)
                    .url("http://10.0.1.51:8080/")
                    .username("risk")
                    .password("riskback")
                    .dateDerniereLivraison(dMinus(now, 9))
                    .description("Backend DEMO RISK")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoRiskMw = EnvApplicationEntity.builder()
                    .environnement(envDemoRisk)
                    .application(appMiddleware)
                    .protocole("http")
                    .host("10.0.1.52")
                    .port(8081)
                    .url("http://10.0.1.52:8081/")
                    .username("risk")
                    .password("riskmw")
                    .dateDerniereLivraison(dMinus(now, 10))
                    .description("Middleware DEMO RISK")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoRiskOpenApi = EnvApplicationEntity.builder()
                    .environnement(envDemoRisk)
                    .application(appOpenApi)
                    .protocole("http")
                    .host("10.0.1.53")
                    .port(8083)
                    .url("http://10.0.1.53:8083/")
                    .username("risk")
                    .password("riskapi")
                    .dateDerniereLivraison(dMinus(now, 7))
                    .description("OpenAPI DEMO RISK")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoRiskKeycloak = EnvApplicationEntity.builder()
                    .environnement(envDemoRisk)
                    .application(appKeycloakManar)
                    .protocole("http")
                    .host("10.0.1.54")
                    .port(8080)
                    .url("http://10.0.1.54:8080/")
                    .username("kc_risk")
                    .password("kcrisk")
                    .dateDerniereLivraison(dMinus(now, 11))
                    .description("Keycloak DEMO RISK")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoRiskPortainer = EnvApplicationEntity.builder()
                    .environnement(envDemoRisk)
                    .application(appPortainerManar)
                    .protocole("http")
                    .host("10.0.1.55")
                    .port(9000)
                    .url("http://10.0.1.55:9000/")
                    .username("admin")
                    .password("adminrisk")
                    .dateDerniereLivraison(dMinus(now, 8))
                    .description("Portainer DEMO RISK")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity demoRiskDb = EnvApplicationEntity.builder()
                    .environnement(envDemoRisk)
                    .application(appDbManar)
                    .protocole("postgresql")
                    .host("10.0.1.60")
                    .port(5432)
                    .url("jdbc:postgresql://10.0.1.60:5432/manar_demo_risk")
                    .username("manar_risk")
                    .password("dbrisk")
                    .dateDerniereLivraison(dMinus(now, 25))
                    .description("DB DEMO RISK")
                    .actif(true)
                    
                    .build();

            // ---- INTEGRATION - PROD HTTPS (10 applications) ----
            EnvApplicationEntity prodHttpsFront = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appFrontManar)
                    .protocole("https")
                    .host("prod.manar.local")
                    .port(443)
                    .url("https://prod.manar.local/")
                    .username("prod")
                    .password("prodfront")
                    .dateDerniereLivraison(dMinus(now, 2))
                    .description("Frontend Manar PROD HTTPS")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodHttpsBack = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appBackManar)
                    .protocole("https")
                    .host("api.prod.manar.local")
                    .port(443)
                    .url("https://api.prod.manar.local/")
                    .username("prod")
                    .password("prodback")
                    .dateDerniereLivraison(dMinus(now, 3))
                    .description("Backend Manar PROD HTTPS")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodHttpsKeycloakManar = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appKeycloakManar)
                    .protocole("https")
                    .host("keycloak.prod.manar.local")
                    .port(8443)
                    .url("https://keycloak.prod.manar.local:8443/")
                    .username("kc_prod")
                    .password("kcprod")
                    .dateDerniereLivraison(dMinus(now, 5))
                    .description("Keycloak Manar PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodHttpsPortainerManar = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appPortainerManar)
                    .protocole("https")
                    .host("portainer.prod.manar.local")
                    .port(9443)
                    .url("https://portainer.prod.manar.local:9443/")
                    .username("admin")
                    .password("portprod")
                    .dateDerniereLivraison(dMinus(now, 8))
                    .description("Portainer Manar PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodHttpsMw = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appMiddleware)
                    .protocole("https")
                    .host("mw.prod.manar.local")
                    .port(8443)
                    .url("https://mw.prod.manar.local:8443/")
                    .username("prod")
                    .password("prodmw")
                    .dateDerniereLivraison(dMinus(now, 6))
                    .description("Middleware PROD HTTPS")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodHttpsKeycloakMw = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appKeycloakMw)
                    .protocole("https")
                    .host("kc-mw.prod.manar.local")
                    .port(8443)
                    .url("https://kc-mw.prod.manar.local:8443/")
                    .username("kc_mw_prod")
                    .password("kcmwprod")
                    .dateDerniereLivraison(dMinus(now, 7))
                    .description("Keycloak Middleware PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodHttpsPortainerMw = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appPortainerMw)
                    .protocole("https")
                    .host("portainer-mw.prod.manar.local")
                    .port(9443)
                    .url("https://portainer-mw.prod.manar.local:9443/")
                    .username("admin")
                    .password("portmwprod")
                    .dateDerniereLivraison(dMinus(now, 9))
                    .description("Portainer Middleware PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodHttpsOpenApi = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appOpenApi)
                    .protocole("https")
                    .host("api-docs.prod.manar.local")
                    .port(443)
                    .url("https://api-docs.prod.manar.local/")
                    .username("prod")
                    .password("prodapi")
                    .dateDerniereLivraison(dMinus(now, 4))
                    .description("OpenAPI Manar PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodHttpsDbMw = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appDbMw)
                    .protocole("postgresql")
                    .host("db-mw.prod.manar.local")
                    .port(5432)
                    .url("jdbc:postgresql://db-mw.prod.manar.local:5432/middleware_prod")
                    .username("mw_prod")
                    .password("dbmwprod")
                    .dateDerniereLivraison(dMinus(now, 20))
                    .description("DB Middleware PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodHttpsDbManar = EnvApplicationEntity.builder()
                    .environnement(envProdHttps)
                    .application(appDbManar)
                    .protocole("postgresql")
                    .host("db-manar.prod.manar.local")
                    .port(5432)
                    .url("jdbc:postgresql://db-manar.prod.manar.local:5432/manar_prod")
                    .username("manar_prod")
                    .password("dbmanarprod")
                    .dateDerniereLivraison(dMinus(now, 25))
                    .description("DB Manar PROD")
                    .actif(true)
                    
                    .build();

            // ---- CLIENT - Env Dev (6 applications) ----
            EnvApplicationEntity devClientFront = EnvApplicationEntity.builder()
                    .environnement(envDevClient)
                    .application(appFrontClient)
                    .protocole("http")
                    .host("10.0.2.11")
                    .port(4300)
                    .url("http://10.0.2.11:4300/")
                    .username("dev")
                    .password("devclient")
                    .dateDerniereLivraison(dMinus(now, 4))
                    .description("Frontend Portail Client DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devClientFrontAdmin = EnvApplicationEntity.builder()
                    .environnement(envDevClient)
                    .application(appFrontAdmin)
                    .protocole("http")
                    .host("10.0.2.11")
                    .port(4301)
                    .url("http://10.0.2.11:4301/")
                    .username("dev")
                    .password("devadmin")
                    .dateDerniereLivraison(dMinus(now, 5))
                    .description("Frontend Admin Portail Client DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devClientBack = EnvApplicationEntity.builder()
                    .environnement(envDevClient)
                    .application(appBackPortail)
                    .protocole("http")
                    .host("10.0.2.12")
                    .port(8080)
                    .url("http://10.0.2.12:8080/")
                    .username("dev")
                    .password("devback")
                    .dateDerniereLivraison(dMinus(now, 6))
                    .description("Backend Portail Client DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devClientKeycloak = EnvApplicationEntity.builder()
                    .environnement(envDevClient)
                    .application(appKeycloakClient)
                    .protocole("http")
                    .host("10.0.2.13")
                    .port(8080)
                    .url("http://10.0.2.13:8080/")
                    .username("kc_client_dev")
                    .password("kcclientdev")
                    .dateDerniereLivraison(dMinus(now, 10))
                    .description("Keycloak Client DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devClientPortainer = EnvApplicationEntity.builder()
                    .environnement(envDevClient)
                    .application(appPortainerFront)
                    .protocole("http")
                    .host("10.0.2.14")
                    .port(9000)
                    .url("http://10.0.2.14:9000/")
                    .username("admin")
                    .password("portdev")
                    .dateDerniereLivraison(dMinus(now, 12))
                    .description("Portainer Client DEV")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity devClientDb = EnvApplicationEntity.builder()
                    .environnement(envDevClient)
                    .application(appDbClient)
                    .protocole("postgresql")
                    .host("10.0.2.20")
                    .port(5432)
                    .url("jdbc:postgresql://10.0.2.20:5432/client_dev")
                    .username("client_dev")
                    .password("dbclientdev")
                    .dateDerniereLivraison(dMinus(now, 18))
                    .description("DB Portail Client DEV")
                    .actif(true)
                    
                    .build();

            // ---- CLIENT - ENV UAT UCM (9 applications) ----
            EnvApplicationEntity uatFrontClient = EnvApplicationEntity.builder()
                    .environnement(envUatUcm)
                    .application(appFrontClient)
                    .protocole("http")
                    .host("10.0.3.11")
                    .port(80)
                    .url("http://10.0.3.11/")
                    .username("uat")
                    .password("uatfront")
                    .dateDerniereLivraison(dMinus(now, 7))
                    .description("Frontend Portail Client UAT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity uatFrontAdmin = EnvApplicationEntity.builder()
                    .environnement(envUatUcm)
                    .application(appFrontAdmin)
                    .protocole("http")
                    .host("10.0.3.11")
                    .port(81)
                    .url("http://10.0.3.11:81/")
                    .username("uat")
                    .password("uatadmin")
                    .dateDerniereLivraison(dMinus(now, 7))
                    .description("Frontend Admin UAT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity uatBack = EnvApplicationEntity.builder()
                    .environnement(envUatUcm)
                    .application(appBackPortail)
                    .protocole("http")
                    .host("10.0.3.12")
                    .port(8080)
                    .url("http://10.0.3.12:8080/")
                    .username("uat")
                    .password("uatback")
                    .dateDerniereLivraison(dMinus(now, 8))
                    .description("Backend Portail UAT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity uatKeycloak = EnvApplicationEntity.builder()
                    .environnement(envUatUcm)
                    .application(appKeycloakClient)
                    .protocole("http")
                    .host("10.0.3.13")
                    .port(8080)
                    .url("http://10.0.3.13:8080/")
                    .username("kc_uat")
                    .password("kcuat")
                    .dateDerniereLivraison(dMinus(now, 10))
                    .description("Keycloak Client UAT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity uatPortFront = EnvApplicationEntity.builder()
                    .environnement(envUatUcm)
                    .application(appPortainerFront)
                    .protocole("http")
                    .host("10.0.3.14")
                    .port(9000)
                    .url("http://10.0.3.14:9000/")
                    .username("admin")
                    .password("portfrontuat")
                    .dateDerniereLivraison(dMinus(now, 9))
                    .description("Portainer Frontend UAT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity uatPortBack = EnvApplicationEntity.builder()
                    .environnement(envUatUcm)
                    .application(appPortainerBack)
                    .protocole("http")
                    .host("10.0.3.15")
                    .port(9000)
                    .url("http://10.0.3.15:9000/")
                    .username("admin")
                    .password("portbackuat")
                    .dateDerniereLivraison(dMinus(now, 9))
                    .description("Portainer Backend UAT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity uatDb = EnvApplicationEntity.builder()
                    .environnement(envUatUcm)
                    .application(appDbClient)
                    .protocole("postgresql")
                    .host("10.0.3.20")
                    .port(5432)
                    .url("jdbc:postgresql://10.0.3.20:5432/client_uat")
                    .username("client_uat")
                    .password("dbclientuat")
                    .dateDerniereLivraison(dMinus(now, 15))
                    .description("DB Portail Client UAT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity uatMachineUcm = EnvApplicationEntity.builder()
                    .environnement(envUatUcm)
                    .application(appMachineDesktopUcm)
                    .protocole("ssh")
                    .host("ucm-uat.local")
                    .port(22)
                    .url("ssh://ucm-uat.local:22")
                    .username("ucm")
                    .password("ucmuat")
                    .dateDerniereLivraison(dMinus(now, 5))
                    .description("Machine Desktop UCM UAT")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity uatMachineManar = EnvApplicationEntity.builder()
                    .environnement(envUatUcm)
                    .application(appMachineManar)
                    .protocole("ssh")
                    .host("manar-uat.local")
                    .port(22)
                    .url("ssh://manar-uat.local:22")
                    .username("manar")
                    .password("manaruat")
                    .dateDerniereLivraison(dMinus(now, 6))
                    .description("Machine Manar UAT")
                    .actif(true)
                    
                    .build();

            // ---- CLIENT - ENV PROD UCM (9 applications, même structure que UAT mais en https) ----
            EnvApplicationEntity prodUcmFrontClient = EnvApplicationEntity.builder()
                    .environnement(envProdUcm)
                    .application(appFrontClient)
                    .protocole("https")
                    .host("portal.client.prod.local")
                    .port(443)
                    .url("https://portal.client.prod.local/")
                    .username("prod")
                    .password("prodfrontclient")
                    .dateDerniereLivraison(dMinus(now, 2))
                    .description("Frontend Portail Client PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodUcmFrontAdmin = EnvApplicationEntity.builder()
                    .environnement(envProdUcm)
                    .application(appFrontAdmin)
                    .protocole("https")
                    .host("portal.admin.prod.local")
                    .port(443)
                    .url("https://portal.admin.prod.local/")
                    .username("prod")
                    .password("prodfrontadmin")
                    .dateDerniereLivraison(dMinus(now, 3))
                    .description("Frontend Admin PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodUcmBack = EnvApplicationEntity.builder()
                    .environnement(envProdUcm)
                    .application(appBackPortail)
                    .protocole("https")
                    .host("api.client.prod.local")
                    .port(443)
                    .url("https://api.client.prod.local/")
                    .username("prod")
                    .password("prodbackclient")
                    .dateDerniereLivraison(dMinus(now, 3))
                    .description("Backend Portail PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodUcmKeycloak = EnvApplicationEntity.builder()
                    .environnement(envProdUcm)
                    .application(appKeycloakClient)
                    .protocole("https")
                    .host("kc.client.prod.local")
                    .port(8443)
                    .url("https://kc.client.prod.local:8443/")
                    .username("kc_prod")
                    .password("kcprodclient")
                    .dateDerniereLivraison(dMinus(now, 4))
                    .description("Keycloak Client PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodUcmPortFront = EnvApplicationEntity.builder()
                    .environnement(envProdUcm)
                    .application(appPortainerFront)
                    .protocole("https")
                    .host("portainer-front.prod.local")
                    .port(9443)
                    .url("https://portainer-front.prod.local:9443/")
                    .username("admin")
                    .password("portfrontprod")
                    .dateDerniereLivraison(dMinus(now, 6))
                    .description("Portainer Frontend PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodUcmPortBack = EnvApplicationEntity.builder()
                    .environnement(envProdUcm)
                    .application(appPortainerBack)
                    .protocole("https")
                    .host("portainer-back.prod.local")
                    .port(9443)
                    .url("https://portainer-back.prod.local:9443/")
                    .username("admin")
                    .password("portbackprod")
                    .dateDerniereLivraison(dMinus(now, 6))
                    .description("Portainer Backend PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodUcmDb = EnvApplicationEntity.builder()
                    .environnement(envProdUcm)
                    .application(appDbClient)
                    .protocole("postgresql")
                    .host("db-client.prod.local")
                    .port(5432)
                    .url("jdbc:postgresql://db-client.prod.local:5432/client_prod")
                    .username("client_prod")
                    .password("dbclientprod")
                    .dateDerniereLivraison(dMinus(now, 12))
                    .description("DB Portail Client PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodUcmMachineUcm = EnvApplicationEntity.builder()
                    .environnement(envProdUcm)
                    .application(appMachineDesktopUcm)
                    .protocole("ssh")
                    .host("ucm-prod.local")
                    .port(22)
                    .url("ssh://ucm-prod.local:22")
                    .username("ucm")
                    .password("ucmprod")
                    .dateDerniereLivraison(dMinus(now, 5))
                    .description("Machine Desktop UCM PROD")
                    .actif(true)
                    
                    .build();

            EnvApplicationEntity prodUcmMachineManar = EnvApplicationEntity.builder()
                    .environnement(envProdUcm)
                    .application(appMachineManar)
                    .protocole("ssh")
                    .host("manar-prod.local")
                    .port(22)
                    .url("ssh://manar-prod.local:22")
                    .username("manar")
                    .password("manarprod")
                    .dateDerniereLivraison(dMinus(now, 6))
                    .description("Machine Manar PROD")
                    .actif(true)
                    
                    .build();

            // Sauvegarde de TOUTES les EnvApplication
            envApplicationRepository.saveAll(List.of(
                    devFront, devBack, devMw, devOpenApi, devKeycloak, devPortainer, devJenkins, devDb,
                    recFront, recBack, recMw, recOpenApi, recKeycloak, recPortainer, recDb,
                    pgFront, pgBack, pgMw, pgOpenApi, pgKeycloak, pgPortainer, pgDb,
                    intTestFront, intTestBack, intTestMw, intTestOpenApi, intTestKeycloak, intTestPortainer, intTestDb,
                    demoProdFront, demoProdBack, demoProdMw, demoProdOpenApi, demoProdKeycloak, demoProdPortainer, demoProdDb,
                    demoRiskFront, demoRiskBack, demoRiskMw, demoRiskOpenApi, demoRiskKeycloak, demoRiskPortainer, demoRiskDb,
                    prodHttpsFront, prodHttpsBack, prodHttpsKeycloakManar, prodHttpsPortainerManar,
                    prodHttpsMw, prodHttpsKeycloakMw, prodHttpsPortainerMw, prodHttpsOpenApi,
                    prodHttpsDbMw, prodHttpsDbManar,
                    devClientFront, devClientFrontAdmin, devClientBack, devClientKeycloak, devClientPortainer, devClientDb,
                    uatFrontClient, uatFrontAdmin, uatBack, uatKeycloak, uatPortFront, uatPortBack,
                    uatDb, uatMachineUcm, uatMachineManar,
                    prodUcmFrontClient, prodUcmFrontAdmin, prodUcmBack, prodUcmKeycloak,
                    prodUcmPortFront, prodUcmPortBack, prodUcmDb, prodUcmMachineUcm, prodUcmMachineManar
            ));



            UtilisateurEntity adminUser = UtilisateurEntity.builder()
                    .code("admin")  // doit matcher preferred_username du token
                    .firstName("Bilal")
                    .lastName("Sajai")
                    .email("bilalsajai1966@gmail.com")  // doit matcher l'email du JWT
                    .keycloakId("b2fdcd04-7266-4ad7-b48f-95cebbf19197") // sub du JWT
                    .actif(true)
                    .profil(profilAdmin)
                    .build();

            utilisateurRepository.save(adminUser);

            log.info(">>> Base initialisée avec données de test complètes.");
        };
    }
}
*/
