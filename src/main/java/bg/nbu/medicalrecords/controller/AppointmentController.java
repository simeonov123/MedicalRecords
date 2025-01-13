package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Diagnosis;
import bg.nbu.medicalrecords.domain.Prescription;
import bg.nbu.medicalrecords.domain.SickLeave;
import bg.nbu.medicalrecords.domain.Treatment;
import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.service.*;
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
    private final TreatmentService treatmentService;

    private final PrescriptionService prescriptionService;

    public AppointmentController(AppointmentService appointmentService, SickLeaveService sickLeaveService, DiagnosisService diagnosisService, TreatmentService treatmentService, PrescriptionService prescriptionService) {
        this.appointmentService = appointmentService;
        this.sickLeaveService = sickLeaveService;
        this.diagnosisService = diagnosisService;
        this.treatmentService = treatmentService;
        this.prescriptionService = prescriptionService;
    }

    @GetMapping("/getAppointmentsForLoggedInUser")
    @PreAuthorize("hasAnyAuthority('patient', 'admin', 'doctor')")
    public ResponseEntity<List<AppointmentDto>> findAllForLoggedInUser() {
    return ResponseEntity.ok(appointmentService.findAllForLoggedInUser());
    }


    @GetMapping("/{patientId}/appointments")
    @PreAuthorize("hasAnyAuthority( 'admin', 'doctor')")
    public ResponseEntity<List<AppointmentDto>> findAllForPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.findAllForPatient(patientId));
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


    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDto> updateAppointment(@PathVariable Long id, @RequestBody UpdateAppointmentDto updateAppointmentDto) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, updateAppointmentDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
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

    //http://localhost:8081/appointments/8/diagnosis/16/treatment
    @PostMapping("/{appointmentId}/diagnosis/{diagnosisId}/treatment")
    @PreAuthorize("hasAnyAuthority('doctor', 'admin')")
    public ResponseEntity<Treatment> createTreatment(@PathVariable Long appointmentId, @RequestBody CreateTreatmentDto createTreatmentDto, @PathVariable Long diagnosisId) {
        Treatment treatment = treatmentService.createTreatment(appointmentId, diagnosisId, createTreatmentDto);
        return ResponseEntity.ok(treatment);
    }


    //appointments/11/treatments/5/prescriptions

    @PostMapping("/{appointmentId}/treatments/{treatmentId}/prescriptions")
    @PreAuthorize("hasAnyAuthority('doctor', 'admin')")
    public ResponseEntity<PrescriptionDto> createPrescription(@PathVariable Long appointmentId, @PathVariable Long treatmentId, @RequestBody CreatePrescriptionDto createPrescriptionDto) {
        Prescription prescription = prescriptionService.createPrescription(appointmentId, treatmentId, createPrescriptionDto);
        return ResponseEntity.ok(MappingUtils.mapToPrescriptionDto(prescription));
    }


}
