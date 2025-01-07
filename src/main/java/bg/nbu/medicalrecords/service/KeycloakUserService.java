// src/main/java/bg/nbu/medicalrecords/service/KeycloakUserService.java

package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakUserService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String realmName;

    private final KeycloakService keycloakService;

    public KeycloakUserService(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    /**
     * Fetch all users from Keycloak, then for each user fetch realm roles + emailVerified.
     */
    public List<KeycloakUserDto> fetchAllUsers() {
        String adminToken = keycloakService.getAdminAccessToken();
        String url = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // Potentially add "?max=100" if you have many users
        ResponseEntity<List> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), List.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> usersList = (List<Map<String, Object>>) response.getBody();
            // Convert each user record to KeycloakUserDto
            return usersList.stream().map(this::mapKeycloakUser).collect(Collectors.toList());
        } else {
            throw new RuntimeException("Failed to fetch users from Keycloak");
        }
    }

    /**
     * Update the role for a user by removing all existing realm roles and assigning the new one.
     */
    public void updateUserRole(String userId, String newRole) {
        String adminToken = keycloakService.getAdminAccessToken();

        String getRolesUrl = keycloakAuthServerUrl + "/admin/realms/" + realmName
                + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // 1. Fetch current roles
        ResponseEntity<List> rolesResp = restTemplate.exchange(
                getRolesUrl, HttpMethod.GET, new HttpEntity<>(headers), List.class
        );
        if (rolesResp.getStatusCode() == HttpStatus.OK && rolesResp.getBody() != null) {
            // 2. Remove all current realm roles
            List<Map<String, Object>> currentRoles = (List<Map<String, Object>>) rolesResp.getBody();
            if (!currentRoles.isEmpty()) {
                HttpEntity<List<Map<String, Object>>> removeEntity = new HttpEntity<>(currentRoles, headers);
                restTemplate.exchange(getRolesUrl, HttpMethod.DELETE, removeEntity, Void.class);
            }
        }

        // 3. Assign the new role
        keycloakService.assignRole(userId, newRole);
    }

    /**
     * Mark user as email verified in Keycloak.
     */
    public void setUserEmailVerified(String userId, boolean isVerified) {
        String adminToken = keycloakService.getAdminAccessToken();
        String url = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("emailVerified", isVerified);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update emailVerified in Keycloak");
        }
    }

    /**
     * Delete user by Keycloak user ID
     */
    public void deleteUser(String userId) {
        String adminToken = keycloakService.getAdminAccessToken();
        String url = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to delete user in Keycloak");
        }
    }

    /**
     * Update user details: email, firstName, lastName, username, etc.
     */
    public void updateUserDetails(String userId, KeycloakUserDto dto) {
        String adminToken = keycloakService.getAdminAccessToken();
        String url = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", dto.getEmail());
        payload.put("firstName", dto.getFirstName());
        payload.put("lastName", dto.getLastName());
        payload.put("username", dto.getUsername());
        payload.put("emailVerified", dto.isEmailVerified());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update user details in Keycloak");
        }
    }

    /**
     * Helper: convert Keycloak user JSON to KeycloakUserDto (including realm roles + emailVerified).
     */
    private KeycloakUserDto mapKeycloakUser(Map<String, Object> userMap) {
        KeycloakUserDto dto = new KeycloakUserDto();
        String userId = (String) userMap.get("id");
        dto.setId(userId);
        dto.setUsername((String) userMap.get("username"));
        dto.setEmail((String) userMap.get("email"));
        dto.setFirstName((String) userMap.get("firstName"));
        dto.setLastName((String) userMap.get("lastName"));

        // emailVerified is part of the user representation
        Object ev = userMap.get("emailVerified");
        dto.setEmailVerified(ev instanceof Boolean ? (Boolean) ev : false);

        // fetch roles for each user
        List<String> realmRoles = fetchRealmRoles(userId);

        // Filter out Keycloak's default composite role
        realmRoles.remove("default-roles-" + realmName);

        if (!realmRoles.isEmpty()) {
            // For simplicity: pick the first role as "dto.role"
            dto.setRole(realmRoles.get(0));
        } else {
            dto.setRole("user"); // fallback if no realm roles or only default-roles were found
        }

        return dto;
    }

    /**
     * Return a list of realm-level role names assigned to the user.
     */
    private List<String> fetchRealmRoles(String userId) {
        String adminToken = keycloakService.getAdminAccessToken();
        String rolesUrl = keycloakAuthServerUrl + "/admin/realms/" + realmName
                + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<List> resp = restTemplate.exchange(
                rolesUrl, HttpMethod.GET, new HttpEntity<>(headers), List.class
        );
        if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
            List<Map<String, Object>> roleMaps = (List<Map<String, Object>>) resp.getBody();
            List<String> roleNames = new ArrayList<>();
            for (Map<String, Object> r : roleMaps) {
                // "name" is typically the role name
                roleNames.add((String) r.get("name"));
            }
            return roleNames;
        }
        return Collections.emptyList();
    }

    public KeycloakUserDto findUserById(String userId) {
        String adminToken = keycloakService.getAdminAccessToken();
        String userUrl = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userUrl, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> userData = response.getBody();
            KeycloakUserDto userDto = new KeycloakUserDto();
            userDto.setId((String) userData.get("id"));
            userDto.setUsername((String) userData.get("username"));
            userDto.setEmail((String) userData.get("email"));
            userDto.setFirstName((String) userData.get("firstName"));
            userDto.setLastName((String) userData.get("lastName"));
            userDto.setEmailVerified((Boolean) userData.get("emailVerified"));
            // Assuming role is part of the user data, otherwise fetch roles separately
            List<Map<String, String>> realmRoles = (List<Map<String, String>>) userData.get("realmRoles");
            if (realmRoles != null && !realmRoles.isEmpty()) {
                userDto.setRole(realmRoles.get(0).get("name"));
            }
            return userDto;
        } else {
            throw new RuntimeException("Failed to fetch user details from Keycloak");
        }
    }
}
