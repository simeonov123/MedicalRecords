package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.CreatePrescriptionDto;
import bg.nbu.medicalrecords.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final AuthenticationService authenticationService;
    private final PrescriptionRepository prescriptionRepository;

    private final AppointmentService appointmentService;

    private final TreatmentService treatmentService;

    private final MedicationService medicationService;
    private final DiagnosisService diagnosisService;


    public PrescriptionService(AuthenticationService authenticationService, PrescriptionRepository prescriptionRepository, AppointmentService appointmentService, TreatmentService treatmentService, MedicationService medicationService, DiagnosisService diagnosisService) {
        this.authenticationService = authenticationService;
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentService = appointmentService;
        this.treatmentService = treatmentService;
        this.medicationService = medicationService;
        this.diagnosisService = diagnosisService;
    }

    public Prescription createPrescription(Long appointmentId, Long treatmentId, CreatePrescriptionDto createPrescriptionDto) {
        // Check if the appointment exists
        Appointment appointment = appointmentService.findById(appointmentId);

        // Check if the treatment exists
        Treatment treatment = treatmentService.findById(treatmentId);

        // Check if the medication exists
        Medication medication = medicationService.findById(createPrescriptionDto.getMedicationId());

        // Check if the user trying to create the prescription has the right to do so
        User currentUser = authenticationService.getCurrentUser();

        // Check if the user is a doctor and if they are the doctor that created the appointment
        if (currentUser.getRole().equals("doctor")) {
            if (!appointment.getDoctor().getKeycloakUserId().equals(currentUser.getKeycloakUserId())) {
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }
        }

        Prescription prescription = new Prescription();
        prescription.setTreatment(treatment);
        prescription.setMedication(medication);
        prescription.setDosage(createPrescriptionDto.getDosage());
        prescription.setDuration(createPrescriptionDto.getDuration());

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        treatment.getPrescriptions().add(savedPrescription);
        treatment = treatmentService.save(treatment);

        Diagnosis diagnosis = treatment.getDiagnosis();
        diagnosis.getTreatments().add(treatment);
        diagnosisService.save(diagnosis);

        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentService.save(appointment);

        return savedPrescription;
    }
}
