package ma.perenity.backend.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.config.KeycloakProperties;
import ma.perenity.backend.dto.ProfilKeycloakDTO;
import ma.perenity.backend.dto.RoleKeycloakDTO;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final KeycloakProperties keycloakProperties;
    private Keycloak keycloak;

    private Keycloak getKeycloak() {
        if (keycloak == null) {
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakProperties.getServerUrl())
                    .realm(keycloakProperties.getRealm())
                    .clientId(keycloakProperties.getClientAdminId())
                    .clientSecret(keycloakProperties.getClientSecret())
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .build();
        }
        return keycloak;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Récupérer un groupe par son nom
     */
    public String findGroupIdByName(String groupName) {
        List<GroupRepresentation> groups = getKeycloak()
                .realm(keycloakProperties.getRealm())
                .groups()
                .groups(groupName, 0, 1); // Recherche exacte

        if (groups != null && !groups.isEmpty()) {
            // Vérifier que le nom correspond exactement (case-insensitive)
            for (GroupRepresentation group : groups) {
                if (group.getName().equalsIgnoreCase(groupName)) {
                    return group.getId();
                }
            }
        }
        return null;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Créer ou récupérer un groupe existant
     */
    public String getOrCreateGroup(ProfilKeycloakDTO profilDTO) {
        // 1️⃣ Vérifier si le groupe existe déjà
        String existingGroupId = findGroupIdByName(profilDTO.getLibelle());

        if (existingGroupId != null) {
            System.out.println("✅ Groupe Keycloak déjà existant: " + profilDTO.getLibelle() + " (ID: " + existingGroupId + ")");

            // Mettre à jour les rôles du groupe existant
            if (profilDTO.getRoles() != null && !profilDTO.getRoles().isEmpty()) {
                createMissingRoles(profilDTO.getRoles());
                replaceGroupRoles(existingGroupId, profilDTO.getRoles());
            }

            return existingGroupId;
        }

        // 2️⃣ Sinon, créer le groupe
        System.out.println("🆕 Création du groupe Keycloak: " + profilDTO.getLibelle());
        return createGroup(profilDTO);
    }

    public String createUser(String username, String firstName, String lastName,
                             String email, String password, boolean enabled, String groupId) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(enabled);

        UsersResource usersResource = getKeycloak().realm(keycloakProperties.getRealm()).users();
        Response response = usersResource.create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus());
        }

        String userId = usersResource.search(username).get(0).getId();

        setPassword(userId, password);
        assignUserToGroup(userId, groupId);

        return userId;
    }

    public void updateUser(String keycloakId, String username, String firstName,
                           String lastName, String email, boolean enabled, String groupId) {

        UsersResource usersResource = getKeycloak()
                .realm(keycloakProperties.getRealm())
                .users();

        UserRepresentation user = usersResource.get(keycloakId).toRepresentation();

        if (user == null) {
            throw new RuntimeException("Keycloak user not found: " + keycloakId);
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setEnabled(enabled);

        usersResource.get(keycloakId).update(user);

        assignUserToGroup(keycloakId, groupId);
    }

    public void deleteUser(String userId) {
        getKeycloak().realm(keycloakProperties.getRealm())
                .users()
                .get(userId)
                .remove();
    }

    /**
     * ✅ MODIFIÉ: Ne plus lever d'exception si le groupe existe déjà
     */
    public String createGroup(ProfilKeycloakDTO profilDTO) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(profilDTO.getLibelle());

        Response response = getKeycloak().realm(keycloakProperties.getRealm()).groups().add(group);

        if (response.getStatus() == 201) {
            String location = response.getHeaderString("Location");
            if (location != null && location.contains("/groups/")) {
                String groupId = location.substring(location.lastIndexOf("/") + 1);

                if (profilDTO.getRoles() != null && !profilDTO.getRoles().isEmpty()) {
                    createMissingRoles(profilDTO.getRoles());
                    replaceGroupRoles(groupId, profilDTO.getRoles());
                }

                return groupId;
            }
        } else if (response.getStatus() == 409) {
            // ✅ Si le groupe existe déjà, le récupérer au lieu de lever une exception
            System.out.println("⚠️ Groupe déjà existant, récupération de l'ID...");
            String existingGroupId = findGroupIdByName(profilDTO.getLibelle());
            if (existingGroupId != null) {
                return existingGroupId;
            }
            throw new RuntimeException("Group already exists but could not retrieve ID: " + profilDTO.getLibelle());
        }

        throw new RuntimeException("Failed to create group. HTTP Status: " + response.getStatus());
    }

    public void updateGroup(String groupId, List<RoleKeycloakDTO> roles) {
        if (roles != null && !roles.isEmpty()) {
            createMissingRoles(roles);
            replaceGroupRoles(groupId, roles);
        }
    }

    public void deleteGroup(String groupId) {
        getKeycloak().realm(keycloakProperties.getRealm())
                .groups()
                .group(groupId)
                .remove();
    }

    public void setPassword(String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        getKeycloak().realm(keycloakProperties.getRealm())
                .users()
                .get(userId)
                .resetPassword(credential);
    }

    public boolean verifyPassword(String username, String password) {
        try {
            Keycloak testKeycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakProperties.getServerUrl())
                    .realm(keycloakProperties.getRealm())
                    .clientId(keycloakProperties.getClientId())
                    .grantType(OAuth2Constants.PASSWORD)
                    .username(username)
                    .password(password)
                    .build();

            return testKeycloak.tokenManager().getAccessToken() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void assignUserToGroup(String userId, String groupId) {
        UsersResource users = getKeycloak().realm(keycloakProperties.getRealm()).users();

        users.get(userId).groups().forEach(g -> users.get(userId).leaveGroup(g.getId()));
        users.get(userId).joinGroup(groupId);
    }

    private void createMissingRoles(List<RoleKeycloakDTO> roles) {
        RolesResource keycloakRoles = getKeycloak().realm(keycloakProperties.getRealm()).roles();

        roles.forEach(roleDTO -> {
            boolean exists = keycloakRoles.list()
                    .stream()
                    .anyMatch(kr -> kr.getName().equals(roleDTO.getCode()));

            if (!exists) {
                RoleRepresentation newRole = new RoleRepresentation();
                newRole.setName(roleDTO.getCode());
                newRole.setDescription(roleDTO.getLibelle());
                keycloakRoles.create(newRole);
            }
        });
    }

    private void replaceGroupRoles(String groupId, List<RoleKeycloakDTO> roles) {
        List<RoleRepresentation> currentRoles = getKeycloak().realm(keycloakProperties.getRealm())
                .groups()
                .group(groupId)
                .roles()
                .realmLevel()
                .listAll();

        if (!currentRoles.isEmpty()) {
            getKeycloak().realm(keycloakProperties.getRealm())
                    .groups()
                    .group(groupId)
                    .roles()
                    .realmLevel()
                    .remove(currentRoles);
        }

        List<RoleRepresentation> allRoles = getKeycloak().realm(keycloakProperties.getRealm())
                .roles()
                .list();

        List<String> roleNames = roles.stream()
                .map(RoleKeycloakDTO::getCode)
                .toList();

        List<RoleRepresentation> rolesToAssign = allRoles.stream()
                .filter(role -> roleNames.contains(role.getName()))
                .toList();

        if (!rolesToAssign.isEmpty()) {
            getKeycloak().realm(keycloakProperties.getRealm())
                    .groups()
                    .group(groupId)
                    .roles()
                    .realmLevel()
                    .add(rolesToAssign);
        }
    }
}