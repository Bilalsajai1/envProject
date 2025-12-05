package ma.perenity.backend.service;

import jakarta.ws.rs.NotFoundException;
import ma.perenity.backend.config.KeycloakProperties;

import ma.perenity.backend.dto.ProfilKeycloakDTO;
import ma.perenity.backend.dto.RoleKeycloakDTO;
import ma.perenity.backend.dto.UtilisateurKeycloakDTO;
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

import jakarta.ws.rs.core.Response;
import java.util.List;

@Service
public class KeycloakService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;

    public KeycloakService(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(keycloakProperties.getRealm())
                .clientId(keycloakProperties.getClientAdminId())
                .clientSecret(keycloakProperties.getClientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    private UserRepresentation createKeycloakUser(UtilisateurKeycloakDTO userDto) {
        var user = new UserRepresentation();
        user.setUsername(userDto.getCode());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstname());
        user.setLastName(userDto.getLastname());
        user.setEnabled(userDto.getEnabled());
        return user;
    }

    /**
     * Create a new user
     */
    public String createUser(UtilisateurKeycloakDTO userDto, String groupId) {
        var user = createKeycloakUser(userDto);

        UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();
        Response response = usersResource.create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus());
        }

        // Find the user ID
        String userId = usersResource.search(userDto.getCode()).get(0).getId();

        // Set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userDto.getPassword());
        credential.setTemporary(false);
        usersResource.get(userId).resetPassword(credential);

        assignUserToGroup(userId, groupId);

        return userId;
    }

    /**
     * Update user
     */
    public void updateUser(UtilisateurKeycloakDTO userDto, String groupId) {
        try {
            var updatedUser = createKeycloakUser(userDto);
            keycloak.realm(keycloakProperties.getRealm())
                    .users()
                    .get(userDto.getKeycloakId())
                    .update(updatedUser);
            assignUserToGroup(userDto.getKeycloakId(), groupId);
        } catch (NotFoundException e) {
            throw new RuntimeException("User not found with ID: " + userDto.getKeycloakId(), e);
        }
    }

    /**
     * Delete a user by ID
     */
    public void deleteUser(String userId) {
        try {
            keycloak.realm(keycloakProperties.getRealm())
                    .users()
                    .get(userId)
                    .remove();
        } catch (NotFoundException e) {
            throw new RuntimeException("User not found with ID: " + userId, e);
        }
    }

    /**
     * Create a new group
     */
    public String createGroup(ProfilKeycloakDTO profilDTO) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(profilDTO.getLibelle());

        Response response = keycloak.realm(keycloakProperties.getRealm()).groups().add(group);

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
            throw new RuntimeException("Group already exists: " + profilDTO.getLibelle());
        }

        throw new RuntimeException("Failed to create group. HTTP Status: " + response.getStatus());
    }

    /**
     * Update group roles
     */
    public void updateGroup(String groupId, List<RoleKeycloakDTO> roles) {
        if (roles != null && !roles.isEmpty()) {
            createMissingRoles(roles);
            replaceGroupRoles(groupId, roles);
        }
    }

    /**
     * Assign user to group
     */
    public void assignUserToGroup(String userId, String groupId) {
        UsersResource users = keycloak.realm(keycloakProperties.getRealm()).users();

        // Remove from all groups first
        users.get(userId).groups().forEach(g -> {
            users.get(userId).leaveGroup(g.getId());
        });

        // Join new group
        users.get(userId).joinGroup(groupId);
    }

    /**
     * Create missing roles
     */
    private void createMissingRoles(List<RoleKeycloakDTO> roles) {
        var keycloakRoles = getRoles();

        roles.forEach(roleDTO -> {
            var existingRole = keycloakRoles.list()
                    .stream()
                    .filter(kr -> kr.getName().equals(roleDTO.getCode()))
                    .findFirst();

            if (existingRole.isEmpty()) {
                RoleRepresentation newRole = new RoleRepresentation();
                newRole.setName(roleDTO.getCode());
                newRole.setDescription(roleDTO.getLibelle());
                keycloakRoles.create(newRole);
            }
        });
    }

    /**
     * Get roles resource
     */
    public RolesResource getRoles() {
        return keycloak.realm(keycloakProperties.getRealm()).roles();
    }

    /**
     * Replace group roles
     */
    private void replaceGroupRoles(String groupId, List<RoleKeycloakDTO> roles) {
        // 1. Get current roles
        List<RoleRepresentation> currentRoles = keycloak.realm(keycloakProperties.getRealm())
                .groups()
                .group(groupId)
                .roles()
                .realmLevel()
                .listAll();

        // 2. Remove current roles
        if (!currentRoles.isEmpty()) {
            keycloak.realm(keycloakProperties.getRealm())
                    .groups()
                    .group(groupId)
                    .roles()
                    .realmLevel()
                    .remove(currentRoles);
        }

        // 3. Get all available roles
        List<RoleRepresentation> allRoles = keycloak.realm(keycloakProperties.getRealm())
                .roles()
                .list();

        // 4. Filter roles to assign
        List<String> roleNames = roles.stream()
                .map(RoleKeycloakDTO::getCode)
                .toList();

        List<RoleRepresentation> rolesToAssign = allRoles.stream()
                .filter(role -> roleNames.contains(role.getName()))
                .toList();

        // 5. Add new roles
        if (!rolesToAssign.isEmpty()) {
            keycloak.realm(keycloakProperties.getRealm())
                    .groups()
                    .group(groupId)
                    .roles()
                    .realmLevel()
                    .add(rolesToAssign);
        }
    }

    /**
     * Delete group
     */
    public void deleteGroup(String groupId) {
        try {
            keycloak.realm(keycloakProperties.getRealm())
                    .groups()
                    .group(groupId)
                    .remove();
        } catch (NotFoundException e) {
            throw new RuntimeException("Group not found with ID: " + groupId, e);
        }
    }

    /**
     * Update password
     */
    public void updatePassword(String userId, String newPassword) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();
            usersResource.get(userId).resetPassword(credential);
        } catch (NotFoundException e) {
            throw new RuntimeException("User not found with ID: " + userId, e);
        }
    }

    /**
     * Verify password
     */
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
}