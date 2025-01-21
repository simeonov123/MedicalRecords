package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.dto.UpdatePrescriptionDto;
import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.CreatePrescriptionDto;
import bg.nbu.medicalrecords.exception.*;
import bg.nbu.medicalrecords.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
        Appointment appointment = appointmentService.findById(appointmentId);
        Treatment treatment = treatmentService.findById(treatmentId);
        Medication medication = medicationService.findById(createPrescriptionDto.getMedicationId());

        User currentUser = authenticationService.getCurrentUser();
        if (currentUser.getRole().equals("doctor")) {
            if (!appointment.getDoctor().getKeycloakUserId().equals(currentUser.getKeycloakUserId())) {
                throw new DoctorNotAssignedException("Doctor is not assigned to this appointment");
            }
        } else if (!currentUser.getRole().equals("admin")) {
            throw new UnauthorizedAccessException("User is not authorized to create a prescription");
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

    public Prescription updatePrescription(Long appointmentId, Long treatmentId, Long prescriptionId, UpdatePrescriptionDto updatePrescriptionDto) {
        Appointment appointment = appointmentService.findById(appointmentId);
        Treatment treatment = treatmentService.findById(treatmentId);
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found"));

        User currentUser = authenticationService.getCurrentUser();
        if (currentUser.getRole().equals("doctor")) {
            if (!appointment.getDoctor().getKeycloakUserId().equals(currentUser.getKeycloakUserId())) {
                throw new DoctorNotAssignedException("Doctor is not assigned to this appointment");
            }
        } else if (!currentUser.getRole().equals("admin")) {
            throw new UnauthorizedAccessException("User is not authorized to update a prescription");
        }

        prescription.setDosage(updatePrescriptionDto.getDosage());
        prescription.setDuration(updatePrescriptionDto.getDuration());

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentService.save(appointment);

        return savedPrescription;
    }

    public void deletePrescription(Long appointmentId, Long treatmentId, Long prescriptionId) {
        Appointment appointment = appointmentService.findById(appointmentId);
        Treatment treatment = treatmentService.findById(treatmentId);
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found"));

        User currentUser = authenticationService.getCurrentUser();
        if (currentUser.getRole().equals("doctor")) {
            if (!appointment.getDoctor().getKeycloakUserId().equals(currentUser.getKeycloakUserId())) {
                throw new DoctorNotAssignedException("Doctor is not assigned to this appointment");
            }
        } else if (!currentUser.getRole().equals("admin")) {
            throw new UnauthorizedAccessException("User is not authorized to delete a prescription");
        }

        treatment.getPrescriptions().remove(prescription);
        treatmentService.save(treatment);

        prescriptionRepository.delete(prescription);

        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentService.save(appointment);
    }
}