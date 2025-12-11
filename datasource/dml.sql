INSERT INTO ENVIRONMENT_TYPE (code, libelle, actif) VALUES
                                                        ('EDITION', 'Édition', true),
                                                        ('INTEGRATION', 'Intégration', true),
                                                        ('CLIENT', 'Client', true);
-- ============================================
-- RÔLES SYSTÈME
-- ============================================

-- Rôles pour EDITION
INSERT INTO ROLE (code, libelle, action, scope, actif) VALUES
                                                           ('ENV_EDITION_CONSULT', 'Consultation Environnements EDITION', 'CONSULT', 'ENV_TYPE', true),
                                                           ('ENV_EDITION_CREATE', 'Création Environnements EDITION', 'CREATE', 'ENV_TYPE', true),
                                                           ('ENV_EDITION_UPDATE', 'Modification Environnements EDITION', 'UPDATE', 'ENV_TYPE', true),
                                                           ('ENV_EDITION_DELETE', 'Suppression Environnements EDITION', 'DELETE', 'ENV_TYPE', true);

-- Rôles pour INTEGRATION
INSERT INTO ROLE (code, libelle, action, scope, actif) VALUES
                                                           ('ENV_INTEGRATION_CONSULT', 'Consultation Environnements INTEGRATION', 'CONSULT', 'ENV_TYPE', true),
                                                           ('ENV_INTEGRATION_CREATE', 'Création Environnements INTEGRATION', 'CREATE', 'ENV_TYPE', true),
                                                           ('ENV_INTEGRATION_UPDATE', 'Modification Environnements INTEGRATION', 'UPDATE', 'ENV_TYPE', true),
                                                           ('ENV_INTEGRATION_DELETE', 'Suppression Environnements INTEGRATION', 'DELETE', 'ENV_TYPE', true);

-- Rôles pour CLIENT
INSERT INTO ROLE (code, libelle, action, scope, actif) VALUES
                                                           ('ENV_CLIENT_CONSULT', 'Consultation Environnements CLIENT', 'CONSULT', 'ENV_TYPE', true),
                                                           ('ENV_CLIENT_CREATE', 'Création Environnements CLIENT', 'CREATE', 'ENV_TYPE', true),
                                                           ('ENV_CLIENT_UPDATE', 'Modification Environnements CLIENT', 'UPDATE', 'ENV_TYPE', true),
                                                           ('ENV_CLIENT_DELETE', 'Suppression Environnements CLIENT', 'DELETE', 'ENV_TYPE', true);

-- ============================================
-- PROFILS
-- ============================================

-- 1. ADMIN - Tous les droits
INSERT INTO PROFIL (code, libelle, description, admin, actif) VALUES
    ('ADMIN', 'Administrateur', 'Administrateur système avec tous les droits', true, true);

-- 2. EDITION - Accès complet EDITION uniquement
INSERT INTO PROFIL (code, libelle, description, admin, actif) VALUES
    ('EDITION', 'Édition', 'Accès complet aux environnements d''édition', false, true);

-- 3. INTEGRATION - Accès complet INTEGRATION uniquement
INSERT INTO PROFIL (code, libelle, description, admin, actif) VALUES
    ('INTEGRATION', 'Intégration', 'Accès complet aux environnements d''intégration', false, true);

-- 4. TECHLEAD - Accès EDITION + INTEGRATION
INSERT INTO PROFIL (code, libelle, description, admin, actif) VALUES
    ('TECHLEAD', 'Tech Lead', 'Accès complet EDITION et INTEGRATION', false, true);

-- 5. CONSULTANT - Consultation uniquement (tous types)
INSERT INTO PROFIL (code, libelle, description, admin, actif) VALUES
    ('CONSULTANT', 'Consultant', 'Consultation de tous les environnements', false, true);

-- ============================================
-- ASSIGNATION DES RÔLES AUX PROFILS
-- ============================================

-- Profil ADMIN : TOUS les rôles
INSERT INTO PROFIL_ROLE (profil_id, role_id)
SELECT p.id, r.id
FROM PROFIL p
         CROSS JOIN ROLE r
WHERE p.code = 'ADMIN';

-- Profil EDITION : Tous les droits sur EDITION
INSERT INTO PROFIL_ROLE (profil_id, role_id)
SELECT p.id, r.id
FROM PROFIL p
         CROSS JOIN ROLE r
WHERE p.code = 'EDITION'
  AND r.code LIKE 'ENV_EDITION_%';

-- Profil INTEGRATION : Tous les droits sur INTEGRATION
INSERT INTO PROFIL_ROLE (profil_id, role_id)
SELECT p.id, r.id
FROM PROFIL p
         CROSS JOIN ROLE r
WHERE p.code = 'INTEGRATION'
  AND r.code LIKE 'ENV_INTEGRATION_%';

-- Profil TECHLEAD : Tous les droits sur EDITION + INTEGRATION
INSERT INTO PROFIL_ROLE (profil_id, role_id)
SELECT p.id, r.id
FROM PROFIL p
         CROSS JOIN ROLE r
WHERE p.code = 'TECHLEAD'
  AND (r.code LIKE 'ENV_EDITION_%' OR r.code LIKE 'ENV_INTEGRATION_%');

-- Profil CONSULTANT : Consultation uniquement (tous types)
INSERT INTO PROFIL_ROLE (profil_id, role_id)
SELECT p.id, r.id
FROM PROFIL p
         CROSS JOIN ROLE r
WHERE p.code = 'CONSULTANT'
  AND r.action = 'CONSULT';

-- ============================================
-- UTILISATEUR BILAL (Admin)
-- ============================================

INSERT INTO UTILISATEUR (code, first_name, last_name, email, actif, profil_id)
SELECT
    'bilal.sajai',
    'Bilal',
    'Sajai',
    'bilalsajai1966@gmail.com',
    true,
    p.id
FROM PROFIL p
WHERE p.code = 'ADMIN';

SELECT id, code, libelle, admin, keycloak_group_id FROM PROFIL;
SELECT
    p.code as profil,
    p.libelle,
    COUNT(pr.role_id) as nb_roles
FROM PROFIL p
         LEFT JOIN PROFIL_ROLE pr ON p.id = pr.profil_id
GROUP BY p.id, p.code, p.libelle
ORDER BY p.code;


SELECT
    u.id,
    u.code,
    u.first_name,
    u.last_name,
    u.email,
    u.keycloak_id,
    p.code as profil_code,
    p.libelle as profil_libelle
FROM UTILISATEUR u
         JOIN PROFIL p ON u.profil_id = p.id
WHERE u.email = 'bilalsajai1966@gmail.com';


INSERT INTO PROJET (code, libelle, description, actif) VALUES
                                                           ('MANAR_V8', 'Manar V8', 'Projet principal Manar version 8', true),
                                                           ('MANAR_PORTAIL_CLIENT', 'Manar Portail Client', 'Portail client Manar', true),
                                                           ('MANAR_LICENSE_GENERATOR', 'Manar License Generator', 'Générateur de licences Manar', true);

-- ============================================
-- 7. ENVIRONNEMENTS
-- ============================================

-- MANAR_V8 - EDITION
INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'MANAR_V8_EDITION_DEV_11',
    'Développement (11)',
    'Environnement de développement',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_V8' AND t.code = 'EDITION';

INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'MANAR_V8_EDITION_RECETTE_12',
    'Recette & TNR (12)',
    'Environnement de recette et tests',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_V8' AND t.code = 'EDITION';

INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'MANAR_V8_EDITION_POSTGRESQL',
    'Manar POSTGRESQL',
    'Environnement PostgreSQL',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_V8' AND t.code = 'EDITION';

-- MANAR_V8 - INTEGRATION
INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'MANAR_V8_INTEG_TEST_CLIENT',
    'Env test Client 10.10.10.xx',
    'Environnement test client',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_V8' AND t.code = 'INTEGRATION';

INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'MANAR_V8_INTEG_DEMO_PROD',
    'DEMO - PROD 10.10.10.180',
    'Démonstration production',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_V8' AND t.code = 'INTEGRATION';

INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'MANAR_V8_INTEG_DEMO_RISK',
    'DEMO - RISK 10.10.10.170',
    'Démonstration risque',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_V8' AND t.code = 'INTEGRATION';

INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'MANAR_V8_INTEG_PROD_HTTPS_13',
    'Prod HTTPS (13)',
    'Production HTTPS',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_V8' AND t.code = 'INTEGRATION';

-- MANAR_LICENSE_GENERATOR - EDITION
INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'LICENSE_GEN_PROD',
    'Portail Générateur licence PROD Lot-1 V8.0.X',
    'Générateur de licences production',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_LICENSE_GENERATOR' AND t.code = 'EDITION';

-- MANAR_PORTAIL_CLIENT - CLIENT
INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'PORTAIL_CLIENT_DEV',
    'Env Dev',
    'Environnement développement portail client',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_PORTAIL_CLIENT' AND t.code = 'CLIENT';

INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'PORTAIL_CLIENT_UAT',
    'ENV UAT UCM',
    'Environnement UAT UCM',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_PORTAIL_CLIENT' AND t.code = 'CLIENT';

INSERT INTO ENVIRONNEMENT (code, libelle, description, actif, projet_id, type_id)
SELECT
    'PORTAIL_CLIENT_PROD',
    'ENV PROD UCM',
    'Environnement production UCM',
    true,
    p.id,
    t.id
FROM PROJET p, ENVIRONMENT_TYPE t
WHERE p.code = 'MANAR_PORTAIL_CLIENT' AND t.code = 'CLIENT';


-- ============================================
-- 8. APPLICATIONS
-- ============================================

INSERT INTO APPLICATION (code, libelle, description, actif) VALUES
                                                                ('FRONTEND', 'Frontend', 'Application frontend web', true),
                                                                ('FRONTEND_CLIENT', 'Frontend Client', 'Frontend espace client', true),
                                                                ('FRONTEND_ADMIN', 'Frontend Admin', 'Frontend espace admin', true),
                                                                ('BACKEND', 'Backend', 'API Backend', true),
                                                                ('MIDDLEWARE', 'Middleware', 'Couche middleware', true),
                                                                ('OPENAPI', 'OpenAPI', 'Documentation OpenAPI', true),
                                                                ('KEYCLOAK', 'Keycloak', 'Serveur d''authentification', true),
                                                                ('KEYCLOAK_MANAR', 'Keycloak Manar', 'Keycloak instance Manar', true),
                                                                ('KEYCLOAK_MIDDLEWARE', 'Keycloak Middleware', 'Keycloak instance Middleware', true),
                                                                ('PORTAINER', 'Portainer', 'Gestion des conteneurs Docker', true),
                                                                ('PORTAINER_MANAR', 'Portainer Manar', 'Portainer instance Manar', true),
                                                                ('PORTAINER_MIDDLEWARE', 'Portainer Middleware', 'Portainer instance Middleware', true),
                                                                ('PORTAINER_FRONTEND', 'Portainer Frontend', 'Portainer instance Frontend', true),
                                                                ('PORTAINER_BACKEND', 'Portainer Backend', 'Portainer instance Backend', true),
                                                                ('JENKINS', 'Jenkins', 'Serveur CI/CD', true),
                                                                ('DATABASE', 'Database', 'Base de données', true),
                                                                ('DATABASE_MIDDLEWARE', 'Database Middleware', 'Base de données Middleware', true),
                                                                ('DATABASE_MANAR', 'Database Manar', 'Base de données Manar', true),
                                                                ('MACHINE_DESKTOP_UCM', 'Machine Desktop UCM', 'Machine desktop UCM', true),
                                                                ('MACHINE_MANAR', 'Machine Manar', 'Machine serveur Manar', true);

-- ============================================
-- 9. ENV_APPLICATION - Configuration par environnement
-- ============================================

-- ========================================
-- ENVIRONNEMENT : Développement (11)
-- MANAR_V8 - EDITION
-- ========================================

-- Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.20.20.11',
    22,
    'frontend_user',
    'frontend_pass',
    'http://10.20.20.11/#/auth/login',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_DEV_11' AND a.code = 'FRONTEND';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.20.20.11',
    8080,
    'backend_user',
    'backend_pass',
    'http://10.20.20.11:8080/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_DEV_11' AND a.code = 'BACKEND';

-- Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.20.20.11',
    8081,
    'middleware_user',
    'middleware_pass',
    'http://10.20.20.11:8081',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_DEV_11' AND a.code = 'MIDDLEWARE';

-- OpenAPI
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.11',
    443,
    'api_user',
    'api_pass',
    'http://10.20.20.11/swagger-ui.html',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_DEV_11' AND a.code = 'OPENAPI';

-- Keycloak
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.11',
    8443,
    'admin',
    'keycloak_admin_pass',
    'http://10.20.20.11:8443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_DEV_11' AND a.code = 'KEYCLOAK';

-- Portainer
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.11',
    9443,
    'admin',
    'portainer_pass',
    'https://10.20.20.11:9443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_DEV_11' AND a.code = 'PORTAINER';

-- Jenkins
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.11',
    8082,
    'jenkins_admin',
    'jenkins_pass',
    'http://10.20.20.11:8082',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_DEV_11' AND a.code = 'JENKINS';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.20.20.11',
    5432,
    'manar_db_user',
    'db_pass',
    'jdbc:postgresql://10.20.20.11:5432/manar_dev',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_DEV_11' AND a.code = 'DATABASE';

-- ========================================
-- ENVIRONNEMENT : Recette & TNR (12)
-- MANAR_V8 - EDITION
-- ========================================

-- Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.20.20.12',
    22,
    'frontend_user',
    'frontend_pass',
    'http://10.20.20.12/#/auth/login',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_RECETTE_12' AND a.code = 'FRONTEND';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.20.20.12',
    8080,
    'backend_user',
    'backend_pass',
    'http://10.20.20.12:8080/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_RECETTE_12' AND a.code = 'BACKEND';

-- Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.20.20.12',
    8081,
    'middleware_user',
    'middleware_pass',
    'http://10.20.20.12:8081',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_RECETTE_12' AND a.code = 'MIDDLEWARE';

-- OpenAPI
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.12',
    443,
    'api_user',
    'api_pass',
    'http://10.20.20.12/swagger-ui.html',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_RECETTE_12' AND a.code = 'OPENAPI';

-- Keycloak
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.12',
    8443,
    'admin',
    'keycloak_admin_pass',
    'http://10.20.20.12:8443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_RECETTE_12' AND a.code = 'KEYCLOAK';

-- Portainer
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.12',
    9443,
    'admin',
    'portainer_pass',
    'https://10.20.20.12:9443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_RECETTE_12' AND a.code = 'PORTAINER';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.20.20.12',
    5432,
    'manar_db_user',
    'db_pass',
    'jdbc:postgresql://10.20.20.12:5432/manar_recette',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_RECETTE_12' AND a.code = 'DATABASE';

-- ========================================
-- ENVIRONNEMENT : Manar POSTGRESQL
-- MANAR_V8 - EDITION
-- ========================================

-- Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.20.20.13',
    22,
    'frontend_user',
    'frontend_pass',
    'http://10.20.20.13/#/auth/login',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_POSTGRESQL' AND a.code = 'FRONTEND';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.20.20.13',
    8080,
    'backend_user',
    'backend_pass',
    'http://10.20.20.13:8080/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_POSTGRESQL' AND a.code = 'BACKEND';

-- Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.20.20.13',
    8081,
    'middleware_user',
    'middleware_pass',
    'http://10.20.20.13:8081',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_POSTGRESQL' AND a.code = 'MIDDLEWARE';

-- OpenAPI
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    443,
    'api_user',
    'api_pass',
    'http://10.20.20.13/swagger-ui.html',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_POSTGRESQL' AND a.code = 'OPENAPI';

-- Keycloak
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    8443,
    'admin',
    'keycloak_admin_pass',
    'http://10.20.20.13:8443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_POSTGRESQL' AND a.code = 'KEYCLOAK';

-- Portainer
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    9443,
    'admin',
    'portainer_pass',
    'https://10.20.20.13:9443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_POSTGRESQL' AND a.code = 'PORTAINER';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.20.20.13',
    5432,
    'postgres',
    'postgres_pass',
    'jdbc:postgresql://10.20.20.13:5432/manar_postgresql',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_EDITION_POSTGRESQL' AND a.code = 'DATABASE';

-- ========================================
-- ENVIRONNEMENT : Env test Client 10.10.10.xx
-- MANAR_V8 - INTEGRATION
-- ========================================

-- Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.10.10.100',
    22,
    'frontend_user',
    'frontend_pass',
    'http://10.10.10.100/#/auth/login',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_TEST_CLIENT' AND a.code = 'FRONTEND';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.10.10.100',
    8080,
    'backend_user',
    'backend_pass',
    'http://10.10.10.100:8080/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_TEST_CLIENT' AND a.code = 'BACKEND';

-- Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.10.10.100',
    8081,
    'middleware_user',
    'middleware_pass',
    'http://10.10.10.100:8081',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_TEST_CLIENT' AND a.code = 'MIDDLEWARE';

-- OpenAPI
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.10.10.100',
    443,
    'api_user',
    'api_pass',
    'http://10.10.10.100/swagger-ui.html',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_TEST_CLIENT' AND a.code = 'OPENAPI';

-- Keycloak
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.10.10.100',
    8443,
    'admin',
    'keycloak_admin_pass',
    'http://10.10.10.100:8443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_TEST_CLIENT' AND a.code = 'KEYCLOAK';

-- Portainer
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.10.10.100',
    9443,
    'admin',
    'portainer_pass',
    'https://10.10.10.100:9443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_TEST_CLIENT' AND a.code = 'PORTAINER';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.10.10.100',
    5432,
    'manar_db_user',
    'db_pass',
    'jdbc:postgresql://10.10.10.100:5432/manar_test',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_TEST_CLIENT' AND a.code = 'DATABASE';

-- ========================================
-- ENVIRONNEMENT : DEMO - PROD 10.10.10.180
-- MANAR_V8 - INTEGRATION
-- ========================================

-- Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.10.10.180',
    22,
    'frontend_user',
    'frontend_pass',
    'http://10.10.10.180/#/auth/login',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_PROD' AND a.code = 'FRONTEND';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.10.10.180',
    8080,
    'backend_user',
    'backend_pass',
    'http://10.10.10.180:8080/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_PROD' AND a.code = 'BACKEND';

-- Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.10.10.180',
    8081,
    'middleware_user',
    'middleware_pass',
    'http://10.10.10.180:8081',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_PROD' AND a.code = 'MIDDLEWARE';

-- OpenAPI
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.10.10.180',
    443,
    'api_user',
    'api_pass',
    'http://10.10.10.180/swagger-ui.html',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_PROD' AND a.code = 'OPENAPI';

-- Keycloak
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.10.10.180',
    8443,
    'admin',
    'keycloak_admin_pass',
    'http://10.10.10.180:8443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_PROD' AND a.code = 'KEYCLOAK';

-- Portainer
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.10.10.180',
    9443,
    'admin',
    'portainer_pass',
    'https://10.10.10.180:9443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_PROD' AND a.code = 'PORTAINER';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.10.10.180',
    5432,
    'manar_db_user',
    'db_pass',
    'jdbc:postgresql://10.10.10.180:5432/manar_demo',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_PROD' AND a.code = 'DATABASE';

-- ========================================
-- ENVIRONNEMENT : DEMO - RISK 10.10.10.170
-- MANAR_V8 - INTEGRATION
-- ========================================

-- Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.10.10.170',
    22,
    'frontend_user',
    'frontend_pass',
    'http://10.10.10.170/#/auth/login',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_RISK' AND a.code = 'FRONTEND';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.10.10.170',
    8080,
    'backend_user',
    'backend_pass',
    'http://10.10.10.170:8080/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_RISK' AND a.code = 'BACKEND';

-- Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.10.10.170',
    8081,
    'middleware_user',
    'middleware_pass',
    'http://10.10.10.170:8081',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_RISK' AND a.code = 'MIDDLEWARE';

-- OpenAPI
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.10.10.170',
    443,
    'api_user',
    'api_pass',
    'http://10.10.10.170/swagger-ui.html',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_RISK' AND a.code = 'OPENAPI';

-- Keycloak
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.10.10.170',
    8443,
    'admin',
    'keycloak_admin_pass',
    'http://10.10.10.170:8443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_RISK' AND a.code = 'KEYCLOAK';

-- Portainer
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.10.10.170',
    9443,
    'admin',
    'portainer_pass',
    'https://10.10.10.170:9443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_RISK' AND a.code = 'PORTAINER';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.10.10.170',
    5432,
    'manar_db_user',
    'db_pass',
    'jdbc:postgresql://10.10.10.170:5432/manar_risk',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_DEMO_RISK' AND a.code = 'DATABASE';

-- ========================================
-- ENVIRONNEMENT : Prod HTTPS (13)
-- MANAR_V8 - INTEGRATION
-- ========================================

-- Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    443,
    'frontend_user',
    'frontend_pass',
    'https://10.20.20.13/#/auth/login',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'FRONTEND';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    8443,
    'backend_user',
    'backend_pass',
    'https://10.20.20.13:8443/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'BACKEND';

-- Keycloak Manar
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    8543,
    'admin',
    'keycloak_manar_pass',
    'https://10.20.20.13:8543',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'KEYCLOAK_MANAR';

-- Portainer Manar
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    9543,
    'admin',
    'portainer_manar_pass',
    'https://10.20.20.13:9543',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'PORTAINER_MANAR';

-- Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    8143,
    'middleware_user',
    'middleware_pass',
    'https://10.20.20.13:8143',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'MIDDLEWARE';

-- Keycloak Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    8643,
    'admin',
    'keycloak_middleware_pass',
    'https://10.20.20.13:8643',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'KEYCLOAK_MIDDLEWARE';

-- Portainer Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    9643,
    'admin',
    'portainer_middleware_pass',
    'https://10.20.20.13:9643',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'PORTAINER_MIDDLEWARE';

-- OpenAPI
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.20.20.13',
    443,
    'api_user',
    'api_pass',
    'https://10.20.20.13/swagger-ui.html',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'OPENAPI';

-- Database Middleware
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.20.20.13',
    5433,
    'middleware_db_user',
    'middleware_db_pass',
    'jdbc:postgresql://10.20.20.13:5433/middleware_prod',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'DATABASE_MIDDLEWARE';

-- Database Manar
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.20.20.13',
    5432,
    'manar_db_user',
    'manar_db_pass',
    'jdbc:postgresql://10.20.20.13:5432/manar_prod',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'MANAR_V8_INTEG_PROD_HTTPS_13' AND a.code = 'DATABASE_MANAR';

-- ========================================
-- ENVIRONNEMENT : Env Dev (Portail Client)
-- MANAR_PORTAIL_CLIENT - CLIENT
-- ========================================

-- Frontend Client
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.30.30.10',
    22,
    'frontend_client_user',
    'frontend_client_pass',
    'http://10.30.30.10/client',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_DEV' AND a.code = 'FRONTEND_CLIENT';

-- Frontend Admin
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.30.30.10',
    22,
    'frontend_admin_user',
    'frontend_admin_pass',
    'http://10.30.30.10/admin',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_DEV' AND a.code = 'FRONTEND_ADMIN';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.30.30.10',
    8080,
    'backend_user',
    'backend_pass',
    'http://10.30.30.10:8080/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_DEV' AND a.code = 'BACKEND';

-- Keycloak
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.10',
    8443,
    'admin',
    'keycloak_pass',
    'http://10.30.30.10:8443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_DEV' AND a.code = 'KEYCLOAK';

-- Portainer
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.10',
    9443,
    'admin',
    'portainer_pass',
    'https://10.30.30.10:9443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_DEV' AND a.code = 'PORTAINER';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.30.30.10',
    5432,
    'portail_db_user',
    'portail_db_pass',
    'jdbc:postgresql://10.30.30.10:5432/portail_dev',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_DEV' AND a.code = 'DATABASE';

-- ========================================
-- ENVIRONNEMENT : ENV UAT UCM (Portail Client)
-- MANAR_PORTAIL_CLIENT - CLIENT
-- ========================================

-- Frontend Client
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.20',
    443,
    'frontend_client_user',
    'frontend_client_pass',
    'https://10.30.30.20/client',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_UAT' AND a.code = 'FRONTEND_CLIENT';

-- Frontend Admin
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.20',
    443,
    'frontend_admin_user',
    'frontend_admin_pass',
    'https://10.30.30.20/admin',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_UAT' AND a.code = 'FRONTEND_ADMIN';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.20',
    8443,
    'backend_user',
    'backend_pass',
    'https://10.30.30.20:8443/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_UAT' AND a.code = 'BACKEND';

-- Keycloak
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.20',
    8543,
    'admin',
    'keycloak_pass',
    'https://10.30.30.20:8543',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_UAT' AND a.code = 'KEYCLOAK';

-- Portainer Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.20',
    9543,
    'admin',
    'portainer_frontend_pass',
    'https://10.30.30.20:9543',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_UAT' AND a.code = 'PORTAINER_FRONTEND';

-- Portainer Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.20',
    9643,
    'admin',
    'portainer_backend_pass',
    'https://10.30.30.20:9643',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_UAT' AND a.code = 'PORTAINER_BACKEND';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.30.30.20',
    5432,
    'portail_db_user',
    'portail_db_pass',
    'jdbc:postgresql://10.30.30.20:5432/portail_uat',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_UAT' AND a.code = 'DATABASE';

-- Machine Desktop UCM
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'RDP',
    '10.30.30.21',
    3389,
    'ucm_user',
    'ucm_pass',
    'rdp://10.30.30.21',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_UAT' AND a.code = 'MACHINE_DESKTOP_UCM';

-- Machine Manar
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.30.30.22',
    22,
    'manar_user',
    'manar_pass',
    'ssh://10.30.30.22',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_UAT' AND a.code = 'MACHINE_MANAR';

-- ========================================
-- ENVIRONNEMENT : ENV PROD UCM (Portail Client)
-- MANAR_PORTAIL_CLIENT - CLIENT
-- ========================================

-- Frontend Client
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.30',
    443,
    'frontend_client_user',
    'frontend_client_pass',
    'https://10.30.30.30/client',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_PROD' AND a.code = 'FRONTEND_CLIENT';

-- Frontend Admin
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.30',
    443,
    'frontend_admin_user',
    'frontend_admin_pass',
    'https://10.30.30.30/admin',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_PROD' AND a.code = 'FRONTEND_ADMIN';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.30',
    8443,
    'backend_user',
    'backend_pass',
    'https://10.30.30.30:8443/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_PROD' AND a.code = 'BACKEND';

-- Keycloak
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.30',
    8543,
    'admin',
    'keycloak_pass',
    'https://10.30.30.30:8543',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_PROD' AND a.code = 'KEYCLOAK';

-- Portainer Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.30',
    9543,
    'admin',
    'portainer_frontend_pass',
    'https://10.30.30.30:9543',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_PROD' AND a.code = 'PORTAINER_FRONTEND';

-- Portainer Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.30.30.30',
    9643,
    'admin',
    'portainer_backend_pass',
    'https://10.30.30.30:9643',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_PROD' AND a.code = 'PORTAINER_BACKEND';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.30.30.30',
    5432,
    'portail_db_user',
    'portail_db_pass',
    'jdbc:postgresql://10.30.30.30:5432/portail_prod',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_PROD' AND a.code = 'DATABASE';

-- Machine Desktop UCM
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'RDP',
    '10.30.30.31',
    3389,
    'ucm_user',
    'ucm_pass',
    'rdp://10.30.30.31',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_PROD' AND a.code = 'MACHINE_DESKTOP_UCM';

-- Machine Manar
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'SSH',
    '10.30.30.32',
    22,
    'manar_user',
    'manar_pass',
    'ssh://10.30.30.32',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'PORTAIL_CLIENT_PROD' AND a.code = 'MACHINE_MANAR';

-- ========================================
-- ENVIRONNEMENT : Portail Générateur Licence
-- MANAR_LICENSE_GENERATOR - EDITION
-- ========================================

-- Frontend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.40.40.10',
    443,
    'frontend_user',
    'frontend_pass',
    'https://10.40.40.10/license-generator',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'LICENSE_GEN_PROD' AND a.code = 'FRONTEND';

-- Backend
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.40.40.10',
    8443,
    'backend_user',
    'backend_pass',
    'https://10.40.40.10:8443/api',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'LICENSE_GEN_PROD' AND a.code = 'BACKEND';

-- Portainer
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'HTTPS',
    '10.40.40.10',
    9443,
    'admin',
    'portainer_pass',
    'https://10.40.40.10:9443',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'LICENSE_GEN_PROD' AND a.code = 'PORTAINER';

-- Database
INSERT INTO ENV_APPLICATION (env_id, app_id, protocole, host, port, username, password, url, actif)
SELECT
    e.id,
    a.id,
    'TCP',
    '10.40.40.10',
    5432,
    'license_db_user',
    'license_db_pass',
    'jdbc:postgresql://10.40.40.10:5432/license_generator',
    true
FROM ENVIRONNEMENT e, APPLICATION a
WHERE e.code = 'LICENSE_GEN_PROD' AND a.code = 'DATABASE';

-- Afficher les statistiques
SELECT 'Types d''environnement' as table_name, COUNT(*) as count FROM ENVIRONMENT_TYPE
UNION ALL
SELECT 'Rôles', COUNT(*) FROM ROLE
UNION ALL
SELECT 'Profils', COUNT(*) FROM PROFIL
UNION ALL
SELECT 'Utilisateurs', COUNT(*) FROM UTILISATEUR
UNION ALL
SELECT 'Projets', COUNT(*) FROM PROJET
UNION ALL
SELECT 'Environnements', COUNT(*) FROM ENVIRONNEMENT
UNION ALL
SELECT 'Applications', COUNT(*) FROM APPLICATION
UNION ALL
SELECT 'Env-Applications', COUNT(*) FROM ENV_APPLICATION;