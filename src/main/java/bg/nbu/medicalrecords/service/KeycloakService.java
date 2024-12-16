package bg.nbu.medicalrecords.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String realmName;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret:}")
    private String clientSecret; // If needed

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Obtain an admin access token to interact with the Keycloak Admin API.
     */
    public String getAdminAccessToken() {
        String tokenUrl = keycloakAuthServerUrl + "/realms/" + realmName + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String body = "grant_type=password"
                + "&client_id=" + clientId
                + "&username=" + adminUsername
                + "&password=" + adminPassword;
        // If your realm requires client_secret, append: + "&client_secret=" + clientSecret

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("Failed to obtain admin access token from Keycloak");
        }
    }

    /**
     * Create a user in Keycloak with the specified username, email, and password.
     * Returns the Keycloak User ID of the newly created user.
     */
    public String createUser(String username, String email, String password) {
        String adminToken = getAdminAccessToken();

        String createUserUrl = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Keycloak user creation payload
        Map<String, Object> userPayload = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "credentials", Collections.singletonList(
                        Map.of("type", "password", "value", password, "temporary", false)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userPayload, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(createUserUrl, entity, Void.class);
        if (response.getStatusCode() == HttpStatus.CREATED
                || response.getStatusCode() == HttpStatus.NO_CONTENT) {
            // User created successfully
            // Next: retrieve the userId from Keycloak
            // Keycloak doesn't return the userId in the response body. We must fetch it from the location header or by searching.

            // For simplicity, search by username:
            return getUserIdByUsername(username, adminToken);
        } else {
            throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatusCode());
        }
    }

    /**
     * Retrieve the Keycloak user ID by username.
     */
    public String getUserIdByUsername(String username, String adminToken) {
        String searchUrl = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/users?username=" + username;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Object[]> response = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, Object[].class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().length > 0) {
            Map userData = (Map) response.getBody()[0];
            return (String) userData.get("id");
        }
        throw new RuntimeException("User not found in Keycloak after creation");
    }

    /**
     * Assign a realm role (e.g., "patient", "doctor", "user") to a user in Keycloak.
     */
    public void assignRole(String userId, String roleName) {
        String adminToken = getAdminAccessToken();

        // First fetch the role representation
        String roleUrl = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/roles/" + roleName;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<Map> roleResponse = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        if (roleResponse.getStatusCode() != HttpStatus.OK || roleResponse.getBody() == null) {
            throw new RuntimeException("Role '" + roleName + "' not found in Keycloak");
        }
        Map<String, Object> roleRepresentation = roleResponse.getBody();

        // Assign the role to the user
        String assignUrl = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders assignHeaders = new HttpHeaders();
        assignHeaders.setBearerAuth(adminToken);
        assignHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Map<String, Object>>> assignEntity = new HttpEntity<>(List.of(roleRepresentation), assignHeaders);

        ResponseEntity<Void> assignResponse = restTemplate.exchange(assignUrl, HttpMethod.POST, assignEntity, Void.class);
        if (assignResponse.getStatusCode() != HttpStatus.NO_CONTENT
                && assignResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to assign role '" + roleName + "' to user " + userId);
        }
    }

    public String createUser(String username, String email, String password, String firstName, String lastName) {
        String adminToken = getAdminAccessToken();

        String createUserUrl = keycloakAuthServerUrl + "/admin/realms/" + realmName + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Keycloak user creation payload with firstName/lastName
        Map<String, Object> userPayload = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "firstName", firstName,
                "lastName",  lastName,
                "credentials", Collections.singletonList(
                        Map.of("type", "password", "value", password, "temporary", false)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userPayload, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(createUserUrl, entity, Void.class);
        if (response.getStatusCode() == HttpStatus.CREATED
                || response.getStatusCode() == HttpStatus.NO_CONTENT) {
            return getUserIdByUsername(username, adminToken);
        } else {
            throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatusCode());
        }
    }

}
