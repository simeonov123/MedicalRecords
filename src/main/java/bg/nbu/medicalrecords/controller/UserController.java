// src/main/java/bg/nbu/medicalrecords/controller/UserController.java
package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import bg.nbu.medicalrecords.service.KeycloakService;
import bg.nbu.medicalrecords.service.LocalSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final KeycloakService keycloakService;
    private final LocalSyncService localSyncService;

    public UserController(KeycloakService keycloakService, LocalSyncService localSyncService) {
        this.keycloakService = keycloakService;
        this.localSyncService = localSyncService;
    }


    /**
     * List all Keycloak users + their roles + emailVerified
     */
    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<KeycloakUserDto>> fetchAllUsers() {
        List<KeycloakUserDto> users = keycloakService.fetchAllUsers();
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
        keycloakService.updateUserRole(userId, request.role());
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
        keycloakService.setUserEmailVerified(userId, verified);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        keycloakService.deleteUser(userId);
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
        keycloakService.updateUserDetails(userId, dto);
        return ResponseEntity.ok().build();
    }

    // record for passing the "role" in request body
    public record RoleRequest(String role) {}
}
