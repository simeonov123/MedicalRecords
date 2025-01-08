package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.SickLeave}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class SickLeaveDto implements Serializable {
    Long id;
    String reason;
    LocalDate todayDate;
    LocalDate startDate;
    LocalDate endDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}