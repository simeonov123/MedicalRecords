package bg.nbu.medicalrecords.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DoctorsThatHaveAppointmentsInPeriod {
    String doctorName;
    Long doctorId;
    LocalDate startDate;
    LocalDate endDate;
}
