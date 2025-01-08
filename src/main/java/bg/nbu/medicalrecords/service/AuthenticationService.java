package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Jwt jwt) {

            // Extract data from the JWT claims
            String keycloakUserId = jwt.getClaim("sub"); // Keycloak User ID

            // Query your UserRepository or create a User object
            return userRepository.findByKeycloakUserId(keycloakUserId);
        } else {
            throw new IllegalStateException("Principal is not a JWT instance");
        }
    }
}