package bg.nbu.medicalrecords.util;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.*;

import java.util.stream.Collectors;

public class MappingUtils {

    public static AppointmentDto mapToAppointmentDto(Appointment appointment, User user) {


        return new AppointmentDto(
                appointment.getId(),
                mapToPatientDto(appointment.getPatient(), user),
                mapToDoctorDto(appointment.getDoctor()),
                appointment.getDiagnoses().stream().map(MappingUtils::mapToDiagnosisDto).collect(Collectors.toList()),
                appointment.getSickLeaves().stream().map(MappingUtils::mapToSickLeaveDto).collect(Collectors.toList()),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt(),
                appointment.getAppointmentDateTime()
        );

    }

    public static PatientDto mapToPatientDto(Patient patient, User user) {


        return new PatientDto(
                patient.getId(),
                patient.getName(),
                user.getEgn(),
                patient.isHealthInsurancePaid(),
                patient.getPrimaryDoctor() != null ? patient.getPrimaryDoctor().getId() : null,
                patient.getKeycloakUserId()
        );
    }

    public static DoctorDto mapToDoctorDto(Doctor doctor) {
        return new DoctorDto(
                doctor.getId(),
                doctor.getKeycloakUserId(),
                doctor.getName(),
                doctor.getSpecialties(),
                doctor.isPrimaryCare()
        );
    }

    public static DiagnosisDto mapToDiagnosisDto(Diagnosis diagnosis) {
        return new DiagnosisDto(
                diagnosis.getId(),
                diagnosis.getStatement(),
                diagnosis.getDiagnosedDate(),
                diagnosis.getCreatedAt(),
                diagnosis.getUpdatedAt(),
                diagnosis.getTreatments().stream().map(MappingUtils::mapToTreatmentDto).collect(Collectors.toList())
        );
    }

    public static SickLeaveDto mapToSickLeaveDto(SickLeave sickLeave) {
        return new SickLeaveDto(
                sickLeave.getId(),
                sickLeave.getReason(),
                sickLeave.getTodayDate(),
                sickLeave.getStartDate(),
                sickLeave.getEndDate(),
                sickLeave.getCreatedAt(),
                sickLeave.getUpdatedAt()
        );
    }

    public static TreatmentDto mapToTreatmentDto(Treatment treatment) {
        return new TreatmentDto(
                treatment.getId(),
                treatment.getCreatedAt(),
                treatment.getUpdatedAt(),
                treatment.getStartDate(),
                treatment.getEndDate(),
                treatment.getPrescriptions().stream().map(MappingUtils::mapToPrescriptionDto).collect(Collectors.toList())
        );
    }

    public static PrescriptionDto mapToPrescriptionDto(Prescription prescription) {
        return new PrescriptionDto(
                prescription.getId(),
                mapToMedicationDto(prescription.getMedication()),
                prescription.getDosage(),
                prescription.getDuration(),
                prescription.getCreatedAt(),
                prescription.getUpdatedAt()
        );
    }

    public static MedicationDto mapToMedicationDto(Medication medication) {
        return new MedicationDto(
                medication.getId(),
                medication.getMedicationName(),
                medication.getDosageForm(),
                medication.getStrength(),
                medication.getSideEffect(),
                medication.getCreatedAt(),
                medication.getUpdatedAt()
        );
    }
}