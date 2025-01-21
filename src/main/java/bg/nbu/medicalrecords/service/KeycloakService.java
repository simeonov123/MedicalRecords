// KeycloakService.java

package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * KeycloakService handles interactions with the Keycloak Admin REST API, including:
 * - obtaining an admin access token,
 * - creating a user in Keycloak,
 * - assigning roles,
 * - periodic user synchronization (Keycloak -> local DB).
 */
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
    private String clientSecret; // If needed for confidential clients

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserService userService;

    private final DoctorService doctorService;

    private final PatientService patientService;



    public KeycloakService(UserService userService, DoctorService doctorService, PatientService patientService) {
        this.userService = userService;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * Obtain an admin access token to interact with the Keycloak Admin API.
     */
    public String getAdminAccessToken() {
        String tokenUrl = keycloakAuthServerUrl
                + "/realms/" + realmName
                + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // If your realm requires client_secret, append: + "&client_secret=" + clientSecret
        String body = "grant_type=password"
                + "&client_id=" + clientId
                + "&username=" + adminUsername
                + "&password=" + adminPassword;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("Failed to obtain admin access token from Keycloak");
        }
    }

    /**
     * Create a user in Keycloak with the specified username, email, and password (no first/last name).
     * Returns the Keycloak User ID of the newly created user.
     */
    public String createUser(String username, String email, String password) {
        String adminToken = getAdminAccessToken();

        String createUserUrl = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Keycloak user creation payload (no first/last name)
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

            // Keycloak doesn't return the userId directly; we search by username to find its ID
            return getUserIdByUsername(username, adminToken);
        } else {
            throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatusCode());
        }
    }

    /**
     * Retrieve the Keycloak user ID by username.
     */
    public String getUserIdByUsername(String username, String adminToken) {
        String searchUrl = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users?username=" + username;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Object[]> response = restTemplate.exchange(
                searchUrl, HttpMethod.GET, entity, Object[].class
        );

        if (response.getStatusCode() == HttpStatus.OK
                && response.getBody() != null
                && response.getBody().length > 0) {

            Map userData = (Map) response.getBody()[0];
            return (String) userData.get("id");
        }
        throw new RuntimeException("User not found in Keycloak after creation");
    }

    /**
     * Assign a realm role (e.g., "patient", "doctor", "user", "admin") to a user in Keycloak.
     */
    public void assignRole(String userId, String roleName) {
        String adminToken = getAdminAccessToken();

        // 1) Fetch the role representation
        String roleUrl = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/roles/" + roleName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<Map> roleResponse = restTemplate.exchange(
                roleUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class
        );
        if (roleResponse.getStatusCode() != HttpStatus.OK
                || roleResponse.getBody() == null) {
            throw new RuntimeException("Role '" + roleName + "' not found in Keycloak");
        }

        Map<String, Object> roleRepresentation = roleResponse.getBody();

        // 2) Assign the role to the user
        String assignUrl = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users/" + userId
                + "/role-mappings/realm";

        HttpHeaders assignHeaders = new HttpHeaders();
        assignHeaders.setBearerAuth(adminToken);
        assignHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Map<String, Object>>> assignEntity =
                new HttpEntity<>(List.of(roleRepresentation), assignHeaders);

        ResponseEntity<Void> assignResponse = restTemplate.exchange(
                assignUrl, HttpMethod.POST, assignEntity, Void.class
        );
        if (assignResponse.getStatusCode() != HttpStatus.NO_CONTENT
                && assignResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException(
                    "Failed to assign role '" + roleName + "' to user " + userId
            );
        }
    }

    /**
     * Create a user in Keycloak with the specified username, email, password,
     * plus firstName/lastName. Returns the Keycloak user ID of the newly created user.
     */
    public String createUser(
            String username,
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        String adminToken = getAdminAccessToken();

        String createUserUrl = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Keycloak user creation payload with firstName/lastName
        Map<String, Object> userPayload = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "firstName", firstName,
                "lastName", lastName,
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

    /**
     * Trigger a sync of Keycloak users with the local database on a schedule (and can be called manually).
     * This is an example: fetch all Keycloak users, compare with local DB, create or delete as needed.
     */
    // e.g., every 1 minute
    @Scheduled(fixedRate = 60_000)
    public void syncUsers() {
        // 1) fetch all users from Keycloak
        List<KeycloakUserDto> keycloakUsers = fetchAllUsers();
        // 2) fetch all users from the local database
        List<User> localUsers = userService.findAll();

        // 3) For each Keycloak user, ensure a local user record
        for (KeycloakUserDto keycloakUser : keycloakUsers) {
            // see if it exists locally
            Optional<User> localUserOpt = localUsers.stream()
                    .filter(user -> user.getKeycloakUserId().equals(keycloakUser.getId()))
                    .findFirst();

            // if not found locally, create it
            if (localUserOpt.isEmpty()) {
                User newUser = new User();
                newUser.setKeycloakUserId(keycloakUser.getId());
                newUser.setUsername(keycloakUser.getUsername());
                newUser.setEmail(keycloakUser.getEmail());
                newUser.setFirstName(keycloakUser.getFirstName());
                newUser.setLastName(keycloakUser.getLastName());
                newUser.setRole(keycloakUser.getRole()); // e.g., 'patient', 'doctor', etc.
                newUser.setEgn(keycloakUser.getId()); // or other fields
                userService.createUser(newUser);
            }
        }

        // 4) For each local user, ensure they still exist in Keycloak
        for (User localUser : localUsers) {
            boolean foundInKeycloak = keycloakUsers.stream()
                    .anyMatch(kc -> kc.getId().equals(localUser.getKeycloakUserId()));
            // if the user no longer exists in Keycloak, remove it locally
            if (!foundInKeycloak) {
                userService.deleteUser(localUser.getId());
            }
        }
    }

    /**
     * Fetch all Keycloak users, including their realm roles and emailVerified status.
     */
    public List<KeycloakUserDto> fetchAllUsers() {
        String adminToken = getAdminAccessToken();
        String url = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users?max=1000"; // Increase max if you have more than 1000 users

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<List> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), List.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> usersList = (List<Map<String, Object>>) response.getBody();
            // Convert each user record to KeycloakUserDto
            List<KeycloakUserDto> returnDto = usersList.stream()
                    .map(this::mapKeycloakUser)
                    .collect(Collectors.toList());

            //for each keycloak user dto find the user in the local db and get their egn and pass it in the dto
            for (KeycloakUserDto keycloakUser : returnDto) {
                User user = userService.findByKeycloakUserId(keycloakUser.getId());
                if (user != null) {
                    keycloakUser.setEgn(user.getEgn());
                }
            }

            return returnDto;
        } else {
            throw new RuntimeException("Failed to fetch users from Keycloak");
        }
    }

    /**
     * Update the role for a user by removing all existing realm roles and assigning the new one.
     */
    public void updateUserRole(String userId, String newRole) {
        String adminToken = getAdminAccessToken();

        // 1) Fetch current roles
        String getRolesUrl = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<List> rolesResp = restTemplate.exchange(
                getRolesUrl, HttpMethod.GET, new HttpEntity<>(headers), List.class
        );
        if (rolesResp.getStatusCode() == HttpStatus.OK && rolesResp.getBody() != null) {
            // 2) Remove all current realm roles
            List<Map<String, Object>> currentRoles = (List<Map<String, Object>>) rolesResp.getBody();
            if (!currentRoles.isEmpty()) {
                HttpEntity<List<Map<String, Object>>> removeEntity =
                        new HttpEntity<>(currentRoles, headers);
                restTemplate.exchange(getRolesUrl, HttpMethod.DELETE, removeEntity, Void.class);
            }
        }

        // 3) Now assign the new role
        assignRole(userId, newRole);
    }

    /**
     * Mark user as email verified in Keycloak.
     */
    public void setUserEmailVerified(String userId, boolean isVerified) {
        String adminToken = getAdminAccessToken();
        String url = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users/" + userId;

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
     * Delete user by Keycloak user ID (including local DB records).
     */
    @Transactional
    public void deleteUser(String userId) {
        // 1) delete associated data from local DB
        deleteAssociatedData(userId);

        // 2) remove user from Keycloak
        String adminToken = getAdminAccessToken();
        String url = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to delete user in Keycloak");
        }
    }

    /**
     * Delete associated data for a user by Keycloak user ID (doctor, patient, local user).
     */
    @Transactional
    public void deleteAssociatedData(String userId) {
        // remove from local DB
        if (userService.existsByKeycloakId(userId)) {
            userService.deleteByKeycloakUserId(userId);
        }
        if (doctorService.existsByKeycloakId(userId)) {
            doctorService.deleteByKeycloakUserId(userId);
        }
        if (patientService.existsByKeycloakId(userId)) {
            patientService.deleteByKeycloakUserId(userId);
        }
    }

    /**
     * Update user details: email, firstName, lastName, username, etc. in Keycloak.
     */
    public void updateUserDetails(String userId, KeycloakUserDto dto) {
        String adminToken = getAdminAccessToken();
        String url = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", dto.getEmail());
        payload.put("firstName", dto.getFirstName());
        payload.put("lastName", dto.getLastName());
        payload.put("username", dto.getUsername());
        payload.put("emailVerified", dto.isEmailVerified());
        payload.put("enabled", true);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update user details in Keycloak");
        }

        User user = userService.findByKeycloakUserId(userId);
        if (user != null) {
            if (dto.getEgn() != null && !dto.getEgn().isEmpty()) {
                user.setEgn(dto.getEgn());
            }
            user.setEmail(dto.getEmail());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            userService.updateUser(user);
        }
    }

    /**
     * Return a single Keycloak user by ID, including realm roles if possible.
     */
    public KeycloakUserDto findUserById(String userId) {
        String adminToken = getAdminAccessToken();
        String userUrl = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response =
                restTemplate.exchange(userUrl, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> userData = response.getBody();
            KeycloakUserDto userDto = new KeycloakUserDto();

            userDto.setId((String) userData.get("id"));
            userDto.setUsername((String) userData.get("username"));
            userDto.setEmail((String) userData.get("email"));
            userDto.setFirstName((String) userData.get("firstName"));
            userDto.setLastName((String) userData.get("lastName"));

            // Check if emailVerified is set
            Object emailVerified = userData.get("emailVerified");
            userDto.setEmailVerified(emailVerified instanceof Boolean && (Boolean) emailVerified);

            // We fetch the realm roles separately to get "role"
            List<String> realmRoles = fetchRealmRoles(userDto.getId());
            // remove default roles if present
            realmRoles.remove("default-roles-" + realmName);

            if (!realmRoles.isEmpty()) {
                // pick the first as main role
                userDto.setRole(realmRoles.get(0));
            } else {
                userDto.setRole("user"); // fallback if no assigned roles
            }

            return userDto;
        } else {
            throw new RuntimeException("Failed to fetch user details from Keycloak");
        }
    }

    /**
     * Helper: fetch realm-level roles for a user (Keycloak).
     */
    private List<String> fetchRealmRoles(String userId) {
        String adminToken = getAdminAccessToken();
        String rolesUrl = keycloakAuthServerUrl
                + "/admin/realms/" + realmName
                + "/users/" + userId
                + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<List> resp = restTemplate.exchange(
                rolesUrl, HttpMethod.GET, new HttpEntity<>(headers), List.class
        );

        if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
            List<Map<String, Object>> roleMaps = (List<Map<String, Object>>) resp.getBody();
            List<String> roleNames = new ArrayList<>();
            for (Map<String, Object> rm : roleMaps) {
                String roleName = (String) rm.get("name");
                roleNames.add(roleName);
            }
            return roleNames;
        }
        return Collections.emptyList();
    }

    /**
     * Helper: transform a Keycloak user JSON record into KeycloakUserDto (including realm roles).
     */
    private KeycloakUserDto mapKeycloakUser(Map<String, Object> userMap) {
        KeycloakUserDto dto = new KeycloakUserDto();

        String userId = (String) userMap.get("id");
        dto.setId(userId);
        dto.setUsername((String) userMap.get("username"));
        dto.setEmail((String) userMap.get("email"));
        dto.setFirstName((String) userMap.get("firstName"));
        dto.setLastName((String) userMap.get("lastName"));

        // emailVerified
        Object ev = userMap.get("emailVerified");
        dto.setEmailVerified(ev instanceof Boolean && (Boolean) ev);

        // fetch roles
        List<String> realmRoles = fetchRealmRoles(userId);
        realmRoles.remove("default-roles-" + realmName);

        if (!realmRoles.isEmpty()) {
            // pick the first as the main role
            dto.setRole(realmRoles.get(0));
        } else {
            dto.setRole("user");
        }

        return dto;
    }
}
