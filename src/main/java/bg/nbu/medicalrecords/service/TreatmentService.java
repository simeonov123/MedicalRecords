package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Appointment;
import bg.nbu.medicalrecords.domain.Diagnosis;
import bg.nbu.medicalrecords.domain.Treatment;
import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.CreateTreatmentDto;
import bg.nbu.medicalrecords.repository.TreatmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class TreatmentService {

    private final AuthenticationService authenticationService;

    private final AppointmentService appointmentService;

    private final DiagnosisService diagnosisService;
    private final TreatmentRepository treatmentRepository;

    public TreatmentService(AuthenticationService authenticationService, AppointmentService appointmentService, DiagnosisService diagnosisService, TreatmentRepository treatmentRepository) {
        this.authenticationService = authenticationService;
        this.appointmentService = appointmentService;
        this.diagnosisService = diagnosisService;
        this.treatmentRepository = treatmentRepository;
    }

    public Treatment createTreatment(Long appointmentId, Long diagnosisId, CreateTreatmentDto createTreatmentDto) {
        User currentUser = authenticationService.getCurrentUser();
        Appointment appointment = appointmentService.findById(appointmentId);

        if (currentUser.getRole().equals("doctor")) {
            if (!appointment.getDoctor().getKeycloakUserId().equals(currentUser.getKeycloakUserId())) {
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }
        }

        Diagnosis diagnosis = diagnosisService.findById(diagnosisId);

        Treatment treatment = new Treatment();
        treatment.setDescription(createTreatmentDto.getDescription());
        treatment.setStartDate(createTreatmentDto.getStartDate());
        treatment.setEndDate(createTreatmentDto.getEndDate());
        treatment.setDiagnosis(diagnosis);
        treatment.setPrescriptions(new ArrayList<>());

        treatment = treatmentRepository.save(treatment);

        diagnosis.getTreatments().add(treatment);
        diagnosis = diagnosisService.save(diagnosis);


        Diagnosis updatedDiagnosis = diagnosisService.save(diagnosis);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment.getDiagnoses().get(appointment.getDiagnoses().indexOf(diagnosis)).getTreatments().addAll(updatedDiagnosis.getTreatments());

        appointmentService.save(appointment);


        return treatment;
    }

    public Treatment findById(Long treatmentId) {
        return treatmentRepository.findById(treatmentId).orElseThrow(() -> new IllegalStateException("Treatment not found"));
    }

    public Treatment save(Treatment treatment) {
        treatment.setUpdatedAt(LocalDateTime.now());
        return treatmentRepository.save(treatment);
    }
}
