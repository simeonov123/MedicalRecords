package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.exception.DoctorNotFoundException;
import bg.nbu.medicalrecords.exception.UserNotFoundException;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public DoctorService(DoctorRepository doctorRepository, UserService userService, AuthenticationService authenticationService) {
        this.doctorRepository = doctorRepository;
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    public void createDoctorFromKeycloak(String kcUserId, String name, String uniqueIdentifier) {
        User user = userService.findByKeycloakUserId(kcUserId);
        if (user == null) {
            throw new UserNotFoundException("User not found with keycloak id: " + kcUserId);
        }
        if (!Objects.equals(user.getKeycloakUserId(), kcUserId)) {
            throw new UserNotFoundException("User keycloak id mismatch");
        }

        user.setRole("doctor");
        userService.createUser(user);

        Doctor doc = new Doctor();
        doc.setName(name);
        doc.setKeycloakUserId(uniqueIdentifier);
        doc.setPrimaryCare(false);
        doc.setSpecialties("N/A");
        doctorRepository.save(doc);
    }

    public boolean existsByKeycloakId(String userId) {
        return doctorRepository.existsByKeycloakUserId(userId);
    }

    public void deleteByKeycloakUserId(String userId) {
        doctorRepository.deleteByKeycloakUserId(userId);
    }

    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        Optional<Doctor> existing = doctorRepository.findById(id);
        if (existing.isPresent()) {
            Doctor doc = existing.get();
            doc.setName(updatedDoctor.getName());
            doc.setKeycloakUserId(updatedDoctor.getKeycloakUserId());
            doc.setSpecialties(updatedDoctor.getSpecialties());
            doc.setPrimaryCare(updatedDoctor.isPrimaryCare());
            return doctorRepository.save(doc);
        }
        throw new DoctorNotFoundException("Doctor not found with id: " + id);
    }

    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public Doctor findById(Long id) {
        return doctorRepository.findById(id).orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
    }

    public Doctor findByPrincipal() {
        User user = authenticationService.getCurrentUser();
        return doctorRepository.findByKeycloakUserId(user.getKeycloakUserId());
    }

    public Doctor updateDoctorByKeycloakUserId(String keycloakUserId, Doctor updated) {
        User user = userService.findByKeycloakUserId(keycloakUserId);
        if (user == null) {
            throw new UserNotFoundException("User not found with keycloakUserId: " + keycloakUserId);
        }

        Doctor doc = doctorRepository.findByKeycloakUserId(keycloakUserId);
        if (doc == null) {
            throw new DoctorNotFoundException("Doctor not found with keycloakUserId: " + keycloakUserId);
        }

        doc.setName(user.getFirstName() + " " + user.getLastName());
        doc.setPrimaryCare(updated.isPrimaryCare());
        doc.setSpecialties(updated.getSpecialties().isEmpty() ? "N/A" : updated.getSpecialties());
        return doctorRepository.save(doc);
    }
}