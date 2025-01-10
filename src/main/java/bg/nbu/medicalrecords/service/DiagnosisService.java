package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Appointment;
import bg.nbu.medicalrecords.domain.Diagnosis;
import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.CreateDiagnosisDto;
import bg.nbu.medicalrecords.dto.UpdateDiagnosisDto;
import bg.nbu.medicalrecords.repository.DiagnosisRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }
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
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }
        }

        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId).orElseThrow(() -> new IllegalStateException("Sick leave not found"));


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
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }
        }

        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId).orElseThrow(() -> new IllegalStateException("Diagnosis not found"));

        diagnosisRepository.delete(diagnosis);

        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentService.save(appointment);
    }
}