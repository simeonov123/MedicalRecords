package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Appointment;
import bg.nbu.medicalrecords.dto.AppointmentDto;
import bg.nbu.medicalrecords.dto.CreateAppointmentDto;
import bg.nbu.medicalrecords.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Appointment Controller for standard CRUD operations.
 */
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/getAppointmentsForLoggedInUser")
    @PreAuthorize("hasAnyAuthority('patient', 'admin', 'doctor')")
    public ResponseEntity<List<AppointmentDto>> findAllForLoggedInUser() {
    return ResponseEntity.ok(appointmentService.findAllForLoggedInUser());
    }



    /**
     * Create a new appointment.
     * Patients can create an appointment for themselves.
     * Admin/doctor can create for any patient if that is in the business logic.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('patient', 'admin', 'doctor')")
    public ResponseEntity<AppointmentDto> create(@RequestBody CreateAppointmentDto dto) {
        // The service will handle logic checking if patient is the same as principal or is admin
        AppointmentDto created = appointmentService.createAppointment(dto);
        return ResponseEntity.ok(created);
    }
}
    /**
     * Retrieve single appointment by ID.
     * A patient can see only their own appointments.
     * A doctor can see appointments they are assigned to, or admin sees all, etc.
     */
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('patient', 'doctor', 'admin')")
//    public ResponseEntity<Appointment> findById(@PathVariable Long id) {
//        Appointment apt = appointmentService.findById(id);
//        return ResponseEntity.ok(apt);
//    }

    /**
     * Retrieve all appointments (admin or doctor, typically).
     * Or filter so that patient sees only their own?
     */
//    @GetMapping
//    @PreAuthorize("hasAnyAuthority('doctor', 'admin')")
//    public ResponseEntity<List<Appointment>> findAll() {
//        List<Appointment> all = appointmentService.findAll();
//        return ResponseEntity.ok(all);
//    }

    /**
     * Update an existing appointment.
     * Typically allowed if user is 'admin' or the 'doctor' of the appointment.
     */
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('doctor','admin')")
//    public ResponseEntity<Appointment> update(@PathVariable Long id,
//                                              @RequestBody CreateAppointmentDto dto) {
//        Appointment updated = appointmentService.updateAppointment(id, dto);
//        return ResponseEntity.ok(updated);
//    }

    /**
     * Delete an existing appointment.
     */
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('doctor','admin')")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        appointmentService.deleteAppointment(id);
//        return ResponseEntity.noContent().build();
//    }
//}
