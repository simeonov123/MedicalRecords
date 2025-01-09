package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Appointment}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentDto implements Serializable {
    Long id;
    PatientDto patient;
    DoctorDto doctor;
    List<DiagnosisDto> diagnoses;
    List<SickLeaveDto> sickLeaves;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime appointmentDateTime;
}