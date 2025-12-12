package ma.perenity.backend.excepion;

public enum ErrorMessage {
    USER_NOT_FOUND("Utilisateur introuvable"),
    USER_NOT_FOUND_WITH_EMAIL("Utilisateur introuvable : %s"),
    USER_INACTIVE_OR_DELETED("Utilisateur inactif ou supprime"),
    USER_NOT_AUTHENTICATED("Utilisateur non authentifie"),
    USER_EMAIL_ALREADY_EXISTS("Un utilisateur avec cet email existe deja"),
    USER_CODE_ALREADY_EXISTS("Un utilisateur avec ce code existe deja"),
    USER_ADMIN_REQUIRED("Administration des utilisateurs reservee a l'administrateur"),

    PROFILE_NOT_FOUND("Profil introuvable"),
    PROFILE_CODE_ALREADY_EXISTS("Le code profil existe deja : %s"),
    PROFILE_ADMIN_REQUIRED("Administration des profils reservee a l'administrateur"),
    PROFILE_HAS_ACTIVE_USERS("Impossible de supprimer ce profil : des utilisateurs actifs y sont associes"),
    PROFILE_ADMIN_PERMISSIONS_IMMUTABLE("Impossible de modifier les permissions d'un profil administrateur. Les administrateurs ont automatiquement tous les droits"),

    ROLE_NOT_FOUND("Role introuvable"),
    ROLE_CODE_ALREADY_EXISTS("Code de role deja utilise"),
    INVALID_ACTION("Action invalide : %s"),

    ENVIRONMENT_TYPE_NOT_FOUND("Type d'environnement introuvable avec code : %s"),

    PROJECT_NOT_FOUND("Projet introuvable avec code : %s"),

    NO_PERMISSION("Vous n'avez pas la permission necessaire"),
    NO_CREATE_PERMISSION("Vous n'avez pas la permission de creer"),
    NO_UPDATE_PERMISSION("Vous n'avez pas la permission de modifier"),
    NO_DELETE_PERMISSION("Vous n'avez pas la permission de supprimer"),
    NO_READ_PERMISSION("Vous n'avez pas la permission de consulter cet enregistrement"),
    NO_UPDATE_PERMISSION_FOR_RECORD("Vous n'avez pas la permission de modifier cet enregistrement"),
    NO_DELETE_PERMISSION_FOR_RECORD("Vous n'avez pas la permission de supprimer cet enregistrement"),

    CURRENT_PASSWORD_REQUIRED("Le mot de passe actuel est requis"),
    CURRENT_PASSWORD_INCORRECT("Mot de passe actuel incorrect"),
    PASSWORD_MIN_LENGTH("Le mot de passe doit contenir au moins 8 caracteres"),
    PASSWORD_CHANGE_FAILED_MISSING_KEYCLOAK("Impossible de changer le mot de passe : compte Keycloak manquant"),

    EMAIL_NOT_FOUND_IN_TOKEN("Email introuvable dans le token"),
    AUTHENTICATION_ERROR("Erreur d'authentification Keycloak"),

    ENVIRONMENT_ID_REQUIRED("EnvironnementId est obligatoire"),
    APPLICATION_ID_REQUIRED("ApplicationId est obligatoire"),
    APPLICATION_ALREADY_EXISTS_IN_ENV("Cette application existe deja dans cet environnement."),

    KEYCLOAK_USER_CREATION_FAILED("Failed to create user in Keycloak. Status: %s"),
    KEYCLOAK_USER_NOT_FOUND("Keycloak user not found: %s"),
    KEYCLOAK_USER_DELETION_FAILED("Echec de la suppression de l'utilisateur dans Keycloak"),
    KEYCLOAK_GROUP_CREATION_FAILED("Failed to create group. HTTP Status: %s"),
    KEYCLOAK_GROUP_EXISTS_BUT_NO_ID("Group already exists but could not retrieve ID: %s");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}
