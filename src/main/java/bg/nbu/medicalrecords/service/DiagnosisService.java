package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Appointment;
import bg.nbu.medicalrecords.domain.Diagnosis;
import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.CreateDiagnosisDto;
import bg.nbu.medicalrecords.repository.DiagnosisRepository;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final AuthenticationService authenticationService;
    private final AppointmentService appointmentService;

    public DiagnosisService(DiagnosisRepository diagnosisRepository, AuthenticationService authenticationService, AppointmentService appointmentService) {
        this.diagnosisRepository = diagnosisRepository;
        this.authenticationService = authenticationService;
        this.appointmentService = appointmentService;
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

        return diagnosisRepository.save(diagnosis);
    }
}