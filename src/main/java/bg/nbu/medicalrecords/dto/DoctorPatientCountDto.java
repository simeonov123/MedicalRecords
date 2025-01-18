package bg.nbu.medicalrecords.dto;

import lombok.Data;

@Data
public class DoctorPatientCountDto {
    String doctorName;
    Long count;
}
