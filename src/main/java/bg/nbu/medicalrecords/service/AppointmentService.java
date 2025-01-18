package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.dto.UpdateAppointmentDto;
import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.AppointmentDto;
import bg.nbu.medicalrecords.dto.CreateAppointmentDto;
import bg.nbu.medicalrecords.repository.AppointmentRepository;
import bg.nbu.medicalrecords.repository.PatientRepository;
import bg.nbu.medicalrecords.util.MappingUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AuthenticationService authenticationService;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final UserService userService;


    public AppointmentService(AppointmentRepository appointmentRepository, AuthenticationService authenticationService,
                              PatientRepository patientRepository, PatientService patientService, DoctorService doctorService, UserService userService) {
        this.appointmentRepository = appointmentRepository;
        this.authenticationService = authenticationService;
        this.patientRepository = patientRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.userService = userService;
    }



//    public List<AppointmentDto> findAllForLoggedInUser() {
//        //get the current user then get all appointments for that user and then get all related diagnoses and sick leaves and get all treatments for each diagnosis and each prescription for each treatment and each medicine for each prescription and each doctor for each appointment and each patient for each appointment and each diagnosis for each appointment and each sick leave for each appointment and map them to the dtos for each entity and return the list of appointment dtos
//
//        return null;
//    }

    public List<AppointmentDto> findAllForLoggedInUser() {
        User currentUser = authenticationService.getCurrentUser();
        System.out.println("Current user: " + currentUser);
        List<Appointment> appointments;
        if (currentUser.getRole().contains("doctor")) {
            appointments = appointmentRepository.findByDoctor_KeycloakUserId(currentUser.getKeycloakUserId());
        } else {
            appointments = appointmentRepository.findByPatient_KeycloakUserId(currentUser.getKeycloakUserId());
        }

        System.out.println("Appointments: " + appointments);

        return appointments.stream().map(appointment -> MappingUtils.mapToAppointmentDto(appointment, currentUser)).collect(Collectors.toList());
    }

    public AppointmentDto createAppointment(CreateAppointmentDto dto) {

        User currentUser = authenticationService.getCurrentUser();
        System.out.println("Current user: " + currentUser);
        Appointment appointment = new Appointment();

        Patient patient = patientService.findPatientById(dto.getPatientId());

        appointment.setPatient(patient);

        Doctor doctor = doctorService.findById(dto.getDoctorId());
        appointment.setDoctor(doctor);

        appointment.setAppointmentDateTime(dto.getDate());
        return MappingUtils.mapToAppointmentDto(appointmentRepository.save(appointment), currentUser);
    }

    public Appointment findById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    public void save(Appointment appointment) {
        appointmentRepository.save(appointment);
    }

    public AppointmentDto updateAppointment(Long appointmentId, UpdateAppointmentDto updateAppointmentDto) {
        User currentUser = authenticationService.getCurrentUser();
        Appointment appointment = findById(appointmentId);

        if (currentUser.getRole().equals("doctor")) {
            Doctor doctor = doctorService.findByPrincipal();

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }


        }else if(currentUser.getRole().equals("admin") & updateAppointmentDto.getDoctorId() != null) {
            appointment.setDoctor(doctorService.findById(updateAppointmentDto.getDoctorId()));
        }

        appointment.setAppointmentDateTime(updateAppointmentDto.getAppointmentDateTime());

        return MappingUtils.mapToAppointmentDto(appointmentRepository.save(appointment), currentUser);
    }

    public void deleteAppointment(Long appointmentId) {
        User currentUser = authenticationService.getCurrentUser();

        if (currentUser.getRole().equals("doctor")) {
            Doctor doctor = doctorService.findByPrincipal();
            Appointment appointment = findById(appointmentId);

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new IllegalStateException("Doctor is not assigned to this appointment");
            }
            appointmentRepository.delete(appointment);
        }
    }

    public List<AppointmentDto> findAllForPatient(Long patientId) {
        User currentUser = authenticationService.getCurrentUser();

        if (!currentUser.getRole().contains("admin") && !currentUser.getRole().contains("doctor")) {
            throw new IllegalStateException("You are not allowed to view this patient's appointments");
        }

        List<Appointment> appointments = appointmentRepository.findByPatient_Id(patientId);

        Optional<Patient> patient = patientRepository.findById(patientId);

        String patientKeycloakUserId = patient.map(Patient::getKeycloakUserId).orElse(null);

        if (patientKeycloakUserId == null) {
            throw new IllegalStateException("Patient not found");
        }

        User patientUser = userService.findByKeycloakUserId(patientKeycloakUserId);


        return appointments.stream().map(appointment -> MappingUtils.mapToAppointmentDto(appointment, patientUser)).collect(Collectors.toList());
    }

    public List<Appointment> findAllByDoctorId(Long id) {
        return appointmentRepository.findByDoctor_Id(id);
    }

    public List<AppointmentDto> getAppointmentsForDoctorInPeriod(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Appointment> appointments = appointmentRepository.findByDoctor_Id(doctorId);

        return appointments.stream()
                .filter(appointment -> appointment.getAppointmentDateTime().isAfter(startDate) && appointment.getAppointmentDateTime().isBefore(endDate))
                .map(appointment -> MappingUtils.mapToAppointmentDto(appointment, userService.findByKeycloakUserId(appointment.getPatient().getKeycloakUserId())))
                .collect(Collectors.toList());
    }
}