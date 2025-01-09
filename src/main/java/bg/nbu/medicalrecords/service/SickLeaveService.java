package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Appointment;
import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.SickLeave;
import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.SickLeaveDto;
import bg.nbu.medicalrecords.repository.SickLeaveRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SickLeaveService {

    private final SickLeaveRepository sickLeaveRepository;

    private final AuthenticationService authenticationService;
    private final DoctorService doctorService;

    private final AppointmentService appointmentService;;

    public SickLeaveService(SickLeaveRepository sickLeaveRepository, AuthenticationService authenticationService, DoctorService doctorService, AppointmentService appointmentService) {
        this.sickLeaveRepository = sickLeaveRepository;
        this.authenticationService = authenticationService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    public SickLeave createSickLeave(Long appointmentId, SickLeaveDto sickLeaveDto) {

        User currentUser = authenticationService.getCurrentUser();
        Appointment appointment = appointmentService.findById(appointmentId);

        if (currentUser.getRole().equals("doctor")) {
            Doctor doctor = doctorService.findByPrincipal();

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }
        }

        SickLeave sickLeave = new SickLeave();
        sickLeave.setAppointment(appointment);
        sickLeave.setReason(sickLeaveDto.getReason());
        sickLeave.setTodayDate(sickLeaveDto.getTodayDate());
        sickLeave.setStartDate(sickLeaveDto.getStartDate());
        sickLeave.setEndDate(sickLeaveDto.getEndDate());

        return sickLeaveRepository.save(sickLeave);
    }
}
