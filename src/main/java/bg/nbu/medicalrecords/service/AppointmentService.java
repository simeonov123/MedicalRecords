package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.AppointmentDto;
import bg.nbu.medicalrecords.dto.CreateAppointmentDto;
import bg.nbu.medicalrecords.exception.ResourceNotFoundException;
import bg.nbu.medicalrecords.repository.AppointmentRepository;
import bg.nbu.medicalrecords.repository.DiagnosisRepository;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import bg.nbu.medicalrecords.repository.PatientRepository;
import bg.nbu.medicalrecords.util.MappingUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AuthenticationService authenticationService;

    public AppointmentService(AppointmentRepository appointmentRepository, AuthenticationService authenticationService) {
        this.appointmentRepository = appointmentRepository;
        this.authenticationService = authenticationService;
    }



//    public List<AppointmentDto> findAllForLoggedInUser() {
//        //get the current user then get all appointments for that user and then get all related diagnoses and sick leaves and get all treatments for each diagnosis and each prescription for each treatment and each medicine for each prescription and each doctor for each appointment and each patient for each appointment and each diagnosis for each appointment and each sick leave for each appointment and map them to the dtos for each entity and return the list of appointment dtos
//
//        return null;
//    }

    public List<AppointmentDto> findAllForLoggedInUser() {
        User currentUser = authenticationService.getCurrentUser();
        System.out.println("Current user: " + currentUser);
        List<Appointment> appointments = appointmentRepository.findByPatient_KeycloakUserId(currentUser.getKeycloakUserId());

        System.out.println("Appointments: " + appointments);

        return appointments.stream().map(appointment -> MappingUtils.mapToAppointmentDto(appointment, currentUser)).collect(Collectors.toList());
    }
}