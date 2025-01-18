package bg.nbu.medicalrecords.dto;

import lombok.Data;

@Data
public class DoctorAppointmentsCount {
    String doctorName;
    Long count;
}
