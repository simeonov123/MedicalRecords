package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.dto.CreatePatientDto;
import bg.nbu.medicalrecords.dto.PatientDto;
import bg.nbu.medicalrecords.dto.UpdatePatientDto;
import bg.nbu.medicalrecords.exception.ResourceNotFoundException;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import bg.nbu.medicalrecords.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public PatientDto createPatient(CreatePatientDto dto) {
        Patient p = new Patient();
        p.setName(dto.getName());
        p.setEgn(dto.getEgn());
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
        p.setEgn(dto.getEgn());
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
        return patientRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public PatientDto findById(Long id) {
        Patient p = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return mapToDto(p);
    }

    public PatientDto findByEgn(String egn) {
        Patient p = patientRepository.findByEgn(egn);
        if (p == null) {
            throw new ResourceNotFoundException("Patient not found with EGN: " + egn);
        }
        return mapToDto(p);
    }

    private PatientDto mapToDto(Patient p) {
        PatientDto dto = new PatientDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setEgn(p.getEgn());
        dto.setHealthInsurancePaid(p.isHealthInsurancePaid());
        dto.setPrimaryDoctorId(p.getPrimaryDoctor() != null ? p.getPrimaryDoctor().getId() : null);
        return dto;
    }
}
