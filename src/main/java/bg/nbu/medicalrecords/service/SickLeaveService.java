package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.SickLeaveDto;
import bg.nbu.medicalrecords.dto.UpdateDiagnosisDto;
import bg.nbu.medicalrecords.dto.UpdateSickLeaveDto;
import bg.nbu.medicalrecords.repository.SickLeaveRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

        SickLeave returnSickLeave =  sickLeaveRepository.save(sickLeave);

        appointment.setUpdatedAt(LocalDateTime.now());


        appointmentService.save(appointment);

        return returnSickLeave;
    }

    public SickLeave updateSickLeave(Long appointmentId, UpdateSickLeaveDto sickLeaveDto, Long sickLeaveId) {
        User currentUser = authenticationService.getCurrentUser();
        Appointment appointment = appointmentService.findById(appointmentId);

        if (currentUser.getRole().equals("doctor")) {
            Doctor doctor = doctorService.findByPrincipal();

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }
        }

        SickLeave sickLeave = sickLeaveRepository.findById(sickLeaveId).orElseThrow(() -> new IllegalStateException("Sick leave not found"));


        sickLeave.setReason(sickLeaveDto.getReason());
        sickLeave.setTodayDate(sickLeaveDto.getTodayDate());
        sickLeave.setStartDate(sickLeaveDto.getStartDate());
        sickLeave.setEndDate(sickLeaveDto.getEndDate());


        SickLeave returnSickLeave =  sickLeaveRepository.save(sickLeave);

        appointment.setUpdatedAt(LocalDateTime.now());


        appointmentService.save(appointment);

        return returnSickLeave;
    }

    public void deleteSickLeave(Long sickLeaveId, Long appointmentId) {
        User currentUser = authenticationService.getCurrentUser();
        Appointment appointment = appointmentService.findById(appointmentId);

        if (currentUser.getRole().equals("doctor")) {
            Doctor doctor = doctorService.findByPrincipal();

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }
        }

        SickLeave sickLeave = sickLeaveRepository.findById(sickLeaveId).orElseThrow(() -> new IllegalStateException("Sick leave not found"));

        if (!Objects.equals(sickLeave.getAppointment().getId(), appointmentId)) {
            throw new IllegalStateException("Sick leave is not assigned to this appointment");
        }

        sickLeaveRepository.delete(sickLeave);

        appointment.setUpdatedAt(LocalDateTime.now());

        appointmentService.save(appointment);
    }


    public List<SickLeave> findAllSickLeaves() {
        return sickLeaveRepository.findAll();
    }
}
