package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Diagnosis;
import bg.nbu.medicalrecords.domain.SickLeave;
import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.service.AppointmentService;
import bg.nbu.medicalrecords.service.DiagnosisService;
import bg.nbu.medicalrecords.service.SickLeaveService;
import bg.nbu.medicalrecords.util.MappingUtils;
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
    private final SickLeaveService sickLeaveService;
    private final DiagnosisService diagnosisService;
    public AppointmentController(AppointmentService appointmentService, SickLeaveService sickLeaveService, DiagnosisService diagnosisService) {
        this.appointmentService = appointmentService;
        this.sickLeaveService = sickLeaveService;
        this.diagnosisService = diagnosisService;
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

    @PostMapping("/{appointmentId}/sick-leave")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<SickLeaveDto> createSickLeave(@PathVariable Long appointmentId, @RequestBody SickLeaveDto sickLeaveDto) {


        SickLeave sickLeave = sickLeaveService.createSickLeave(appointmentId, sickLeaveDto);
        return ResponseEntity.ok(MappingUtils.mapToSickLeaveDto(sickLeave));
    }


    @PutMapping("/{appointmentId}/sick-leave/{sickLeaveId}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<SickLeaveDto> updateSickLeave(@PathVariable Long appointmentId, @RequestBody UpdateSickLeaveDto sickLeaveDto, @PathVariable Long sickLeaveId) {


        SickLeave sickLeave = sickLeaveService.updateSickLeave(appointmentId, sickLeaveDto, sickLeaveId);
        return ResponseEntity.ok(MappingUtils.mapToSickLeaveDto(sickLeave));
    }

    @DeleteMapping("/{appointmentId}/sick-leave/{sickLeaveId}")
    public ResponseEntity<Void> deleteSickLeave(@PathVariable Long sickLeaveId, @PathVariable Long appointmentId) {
        sickLeaveService.deleteSickLeave(sickLeaveId, appointmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{appointmentId}/diagnosis")
    @PreAuthorize("hasAuthority('doctor')")
    public ResponseEntity<Diagnosis> createDiagnosis(@PathVariable Long appointmentId, @RequestBody CreateDiagnosisDto createDiagnosisDto) {
        Diagnosis diagnosis = diagnosisService.createDiagnosis(appointmentId, createDiagnosisDto);
        return ResponseEntity.ok(diagnosis);
    }

    @PutMapping("/{appointmentId}/diagnosis/{diagnosisId}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<DiagnosisDto> updateDiagnosis(@PathVariable Long appointmentId, @RequestBody UpdateDiagnosisDto updateDiagnosisDto, @PathVariable Long diagnosisId) {


        Diagnosis diagnosis = diagnosisService.updateDiagnosis(appointmentId, diagnosisId, updateDiagnosisDto);
        return ResponseEntity.ok(MappingUtils.mapToDiagnosisDto(diagnosis));
    }


    @DeleteMapping("/{appointmentId}/diagnosis/{diagnosisId}")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable Long diagnosisId, @PathVariable Long appointmentId) {
        diagnosisService.deleteDiagnosis(diagnosisId, appointmentId);
        return ResponseEntity.noContent().build();
    }
}
