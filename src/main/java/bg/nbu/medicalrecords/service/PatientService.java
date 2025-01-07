package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.CreatePatientDto;
import bg.nbu.medicalrecords.dto.PatientDto;
import bg.nbu.medicalrecords.dto.UpdatePatientDto;
import bg.nbu.medicalrecords.exception.ResourceNotFoundException;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import bg.nbu.medicalrecords.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private final UserService userService;
    public PatientService(PatientRepository patientRepository, DoctorRepository doctorRepository, UserService userService) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.userService = userService;
    }

    public Patient createPatientFromKeycloak(String kcUserId, String name) {

        User user = userService.findByKeycloakUserId(kcUserId);
        if (user == null) {
            throw new RuntimeException("User not found with keycloak id: " + kcUserId);
        }
        if (!Objects.equals(user.getKeycloakUserId(), kcUserId)) {
            throw new RuntimeException("User keycloak id mismatch");
        }

        if (!Objects.equals(user.getRole(), "patient")) {

            user.setRole("patient");
            userService.createUser(user);
        }


        Patient p = new Patient();
        p.setKeycloakUserId(kcUserId);
        p.setName(name);
        p.setHealthInsurancePaid(false);
        return patientRepository.save(p);
    }

    public boolean existsByKeycloakId(String userId) {
        return patientRepository.existsByKeycloakUserId(userId);
    }

    public void deleteByKeycloakUserId(String userId) {
        patientRepository.deleteByKeycloakUserId(userId);
    }

    public PatientDto createPatient(CreatePatientDto dto) {
        Patient p = new Patient();
        p.setName(dto.getName());
        p.setHealthInsurancePaid(dto.getHealthInsurancePaid());

        if (dto.getPrimaryDoctorId() != null) {
            Doctor d = doctorRepository.findById(dto.getPrimaryDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + dto.getPrimaryDoctorId()));
            p.setPrimaryDoctor(d);
        }

        patientRepository.save(p);
        return mapToDto(p);
    }

    public PatientDto updatePatient(Long id, UpdatePatientDto dto) {
        Patient p = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        p.setName(dto.getName());
        p.setHealthInsurancePaid(dto.getHealthInsurancePaid());

        if (dto.getPrimaryDoctorId() != null) {
            Doctor d = doctorRepository.findById(dto.getPrimaryDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + dto.getPrimaryDoctorId()));
            p.setPrimaryDoctor(d);
        } else {
            p.setPrimaryDoctor(null);
        }

        patientRepository.save(p);
        return mapToDto(p);
    }

    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
    }

    public List<PatientDto> findAll() {

        List<PatientDto> patients = patientRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());

        for (PatientDto patient : patients) {
            Optional<Patient> p = patientRepository.findById(patient.getId());
            if (p.isPresent()) {
                if (p.get().getPrimaryDoctor() != null) {
                    patient.setPrimaryDoctorId(p.get().getPrimaryDoctor().getId());
                }
                if (p.get().getKeycloakUserId() != null) {
                    patient.setKeycloakUserId(p.get().getKeycloakUserId());
                }
            }
        }

        return patients;
    }

    public PatientDto findById(Long id) {
        Patient p = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return mapToDto(p);
    }

    public PatientDto findByEgn(String egn) {
        User user = userService.findByEgn(egn);


        Patient p = patientRepository.findByKeycloakUserId(user.getKeycloakUserId());
        if (p == null) {
            throw new ResourceNotFoundException("Patient not found with EGN: " + egn);
        }
        if (!Objects.equals(user.getRole(), "patient")) {
            throw new ResourceNotFoundException("User not a patient");
        }
        return mapToDto(p);
    }

    private PatientDto mapToDto(Patient p) {
        User user = userService.findByKeycloakUserId(p.getKeycloakUserId());

        PatientDto dto = new PatientDto();
        dto.setId(p.getId());
        dto.setEgn(user.getEgn());
        dto.setName(p.getName());
        dto.setHealthInsurancePaid(p.isHealthInsurancePaid());
        dto.setPrimaryDoctorId(p.getPrimaryDoctor() != null ? p.getPrimaryDoctor().getId() : null);
        return dto;
    }

    public void assignPrimaryDoctor(Long patientId, Long doctorId) {
        Patient p = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + doctorId));
        User user = userService.findByKeycloakUserId(p.getKeycloakUserId());
        if (user == null) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        if (!Objects.equals(user.getRole(), "patient")) {
            throw new ResourceNotFoundException("User not a patient");
        }
        Doctor d = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found id=" + doctorId));

        p.setPrimaryDoctor(d);
        patientRepository.save(p);

        d.setPrimaryCare(true);
        doctorRepository.save(d);
    }

    public void updateHealthInsuranceStatus(Long id, Boolean healthInsurancePaid) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        patient.setHealthInsurancePaid(healthInsurancePaid);
        patientRepository.save(patient);
    }
}
