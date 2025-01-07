// src/main/java/bg/nbu/medicalrecords/controller/UserController.java
package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import bg.nbu.medicalrecords.service.KeycloakUserService;
import bg.nbu.medicalrecords.service.LocalSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final KeycloakUserService keycloakUserService;
    private final LocalSyncService localSyncService;

    public UserController(KeycloakUserService keycloakUserService, LocalSyncService localSyncService) {
        this.keycloakUserService = keycloakUserService;
        this.localSyncService = localSyncService;
    }

    /**
     * List all Keycloak users + their roles + emailVerified
     */
    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<KeycloakUserDto>> fetchAllUsers() {
        List<KeycloakUserDto> users = keycloakUserService.fetchAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Update a user's realm role by ID
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable String userId,
            @RequestBody RoleRequest request
    ) {
        keycloakUserService.updateUserRole(userId, request.role());
        localSyncService.handleRoleChange(userId, request.role());

        return ResponseEntity.ok().build();
    }

    /**
     * Verify or un-verify user email
     */
    @PutMapping("/{userId}/verify-email")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> verifyEmail(
            @PathVariable String userId,
            @RequestParam boolean verified
    ) {
        keycloakUserService.setUserEmailVerified(userId, verified);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        keycloakUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update user details: firstName, lastName, email, username, emailVerified
     */
    @PutMapping("/{userId}/details")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> updateUserDetails(
            @PathVariable String userId,
            @RequestBody KeycloakUserDto dto
    ) {
        keycloakUserService.updateUserDetails(userId, dto);
        return ResponseEntity.ok().build();
    }

    // record for passing the "role" in request body
    public record RoleRequest(String role) {}
}
