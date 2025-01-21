package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.CreateDiagnosisDto;
import bg.nbu.medicalrecords.dto.UpdateDiagnosisDto;
import bg.nbu.medicalrecords.exception.DiagnosisNotFoundException;
import bg.nbu.medicalrecords.exception.DoctorNotAssignedException;
import bg.nbu.medicalrecords.exception.UnauthorizedAccessException;
import bg.nbu.medicalrecords.repository.DiagnosisRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final AuthenticationService authenticationService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    public DiagnosisService(DiagnosisRepository diagnosisRepository, AuthenticationService authenticationService, AppointmentService appointmentService, DoctorService doctorService) {
        this.diagnosisRepository = diagnosisRepository;
        this.authenticationService = authenticationService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
    }

    public Diagnosis createDiagnosis(Long appointmentId, CreateDiagnosisDto createDiagnosisDto) {
        User currentUser = authenticationService.getCurrentUser();
        Appointment appointment = appointmentService.findById(appointmentId);

        if (currentUser.getRole().equals("doctor")) {
            if (!appointment.getDoctor().getKeycloakUserId().equals(currentUser.getKeycloakUserId())) {
                throw new DoctorNotAssignedException("Doctor is not assigned to this appointment");
            }
        } else if ((!currentUser.getRole().equals("admin"))){
            throw new UnauthorizedAccessException("User is not authorized to create a diagnosis");
        }

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setAppointment(appointment);
        diagnosis.setStatement(createDiagnosisDto.getStatement());
        diagnosis.setDiagnosedDate(createDiagnosisDto.getDiagnosedDate());

        Diagnosis returnDiagnosis = diagnosisRepository.save(diagnosis);
        appointment.setUpdatedAt(LocalDateTime.now());

        appointmentService.save(appointment);

        return returnDiagnosis;
    }

    public Diagnosis updateDiagnosis(Long appointmentId, Long diagnosisId, UpdateDiagnosisDto updateDiagnosisDto) {
        User currentUser = authenticationService.getCurrentUser();
        Appointment appointment = appointmentService.findById(appointmentId);

        if (currentUser.getRole().equals("doctor")) {
            Doctor doctor = doctorService.findByPrincipal();

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new DoctorNotAssignedException("Doctor is not assigned to this appointment");
            }
        } else if (!currentUser.getRole().equals("admin")) {
            throw new UnauthorizedAccessException("User is not authorized to update a diagnosis");
        }

        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new DiagnosisNotFoundException("Diagnosis not found"));

        diagnosis.setDiagnosedDate(updateDiagnosisDto.getDiagnosedDate());
        diagnosis.setStatement(updateDiagnosisDto.getStatement());

        Diagnosis returnDiagnosis = diagnosisRepository.save(diagnosis);
        appointment.setUpdatedAt(LocalDateTime.now());

        appointmentService.save(appointment);

        return returnDiagnosis;
    }

    public void deleteDiagnosis(Long diagnosisId, Long appointmentId) {
        User currentUser = authenticationService.getCurrentUser();
        Appointment appointment = appointmentService.findById(appointmentId);

        if (currentUser.getRole().equals("doctor")) {
            Doctor doctor = doctorService.findByPrincipal();

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new DoctorNotAssignedException("Doctor is not assigned to this appointment");
            }
        } else if (!currentUser.getRole().equals("admin")) {
            throw new UnauthorizedAccessException("User is not authorized to delete a diagnosis");
        }

        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new DiagnosisNotFoundException("Diagnosis not found"));

        diagnosisRepository.delete(diagnosis);

        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentService.save(appointment);
    }

    public Diagnosis findById(Long diagnosisId) {
        return diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new DiagnosisNotFoundException("Diagnosis not found"));
    }

    public Diagnosis save(Diagnosis diagnosis) {
        return diagnosisRepository.save(diagnosis);
    }

    public List<String> getUniqueDiagnosis() {
        return diagnosisRepository.findDistinctStatements();
    }

    public List<Diagnosis> findByStatement(String statement) {
        return diagnosisRepository.findByStatementIgnoreCase(statement);
    }

    public long count() {
        return diagnosisRepository.count();
    }
}