package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByKeycloakUserId(String keycloakUserId);

    User findByEgn(String egn);
}