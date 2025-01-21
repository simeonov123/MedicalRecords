package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Diagnosis;
import bg.nbu.medicalrecords.domain.Prescription;
import bg.nbu.medicalrecords.domain.SickLeave;
import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.service.*;
import bg.nbu.medicalrecords.util.MappingUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<List<AppointmentDto>> findAllForPatient(@PathVariable @NotNull Long patientId) {
        return ResponseEntity.ok(appointmentService.findAllForPatient(patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('patient', 'admin', 'doctor')")
    public ResponseEntity<AppointmentDto> create(@RequestBody @Valid CreateAppointmentDto dto) {
        AppointmentDto created = appointmentService.createAppointment(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<AppointmentDto> updateAppointment(@PathVariable @NotNull Long id, @RequestBody @Valid UpdateAppointmentDto updateAppointmentDto) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, updateAppointmentDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<Void> deleteAppointment(@PathVariable @NotNull Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{appointmentId}/sick-leave")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<SickLeaveDto> createSickLeave(@PathVariable @NotNull Long appointmentId, @RequestBody @Valid SickLeaveDto sickLeaveDto) {
        SickLeave sickLeave = sickLeaveService.createSickLeave(appointmentId, sickLeaveDto);
        return ResponseEntity.ok(MappingUtils.mapToSickLeaveDto(sickLeave));
    }

    @PutMapping("/{appointmentId}/sick-leave/{sickLeaveId}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<SickLeaveDto> updateSickLeave(@PathVariable @NotNull Long appointmentId, @RequestBody @Valid UpdateSickLeaveDto sickLeaveDto, @PathVariable @NotNull Long sickLeaveId) {
        SickLeave sickLeave = sickLeaveService.updateSickLeave(appointmentId, sickLeaveDto, sickLeaveId);
        return ResponseEntity.ok(MappingUtils.mapToSickLeaveDto(sickLeave));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    @DeleteMapping("/{appointmentId}/sick-leave/{sickLeaveId}")
    public ResponseEntity<Void> deleteSickLeave(@PathVariable @NotNull Long appointmentId, @PathVariable @NotNull Long sickLeaveId) {
        sickLeaveService.deleteSickLeave(sickLeaveId, appointmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{appointmentId}/diagnosis")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<Diagnosis> createDiagnosis(@PathVariable @NotNull Long appointmentId, @RequestBody @Valid CreateDiagnosisDto createDiagnosisDto) {
        Diagnosis diagnosis = diagnosisService.createDiagnosis(appointmentId, createDiagnosisDto);
        return ResponseEntity.ok(diagnosis);
    }

    @PutMapping("/{appointmentId}/diagnosis/{diagnosisId}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<DiagnosisDto> updateDiagnosis(@PathVariable @NotNull Long appointmentId, @RequestBody @Valid UpdateDiagnosisDto updateDiagnosisDto, @PathVariable @NotNull Long diagnosisId) {
        Diagnosis diagnosis = diagnosisService.updateDiagnosis(appointmentId, diagnosisId, updateDiagnosisDto);
        return ResponseEntity.ok(MappingUtils.mapToDiagnosisDto(diagnosis));
    }

    @DeleteMapping("/{appointmentId}/diagnosis/{diagnosisId}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable @NotNull Long appointmentId, @PathVariable @NotNull Long diagnosisId) {
        diagnosisService.deleteDiagnosis(diagnosisId, appointmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{appointmentId}/diagnosis/{diagnosisId}/treatment")
    @PreAuthorize("hasAnyAuthority('doctor', 'admin')")
    public ResponseEntity<TreatmentDto> createTreatment(@PathVariable @NotNull Long appointmentId, @PathVariable @NotNull Long diagnosisId, @RequestBody @Valid CreateTreatmentDto createTreatmentDto) {
        TreatmentDto treatment = treatmentService.createTreatment(appointmentId, diagnosisId, createTreatmentDto);
        return ResponseEntity.ok(treatment);
    }


    @PostMapping("/{appointmentId}/treatments/{treatmentId}/prescriptions")
    @PreAuthorize("hasAnyAuthority('doctor', 'admin')")
    public ResponseEntity<PrescriptionDto> createPrescription(@PathVariable Long appointmentId, @PathVariable Long treatmentId, @RequestBody @Valid CreatePrescriptionDto createPrescriptionDto) {
        Prescription prescription = prescriptionService.createPrescription(appointmentId, treatmentId, createPrescriptionDto);
        return ResponseEntity.ok(MappingUtils.mapToPrescriptionDto(prescription));
    }


    @PutMapping("/{appointmentId}/treatments/{treatmentId}/prescriptions/{prescriptionId}")
    @PreAuthorize("hasAnyAuthority('doctor', 'admin')")
    public ResponseEntity<PrescriptionDto> updatePrescription(@PathVariable Long appointmentId, @PathVariable Long treatmentId, @PathVariable Long prescriptionId, @RequestBody @Valid UpdatePrescriptionDto updatePrescriptionDto) {
        Prescription prescription = prescriptionService.updatePrescription(appointmentId, treatmentId, prescriptionId, updatePrescriptionDto);
        return ResponseEntity.ok(MappingUtils.mapToPrescriptionDto(prescription));
    }


    @DeleteMapping("/{appointmentId}/treatments/{treatmentId}/prescriptions/{prescriptionId}")
    @PreAuthorize("hasAnyAuthority('doctor', 'admin')")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long appointmentId, @PathVariable Long treatmentId, @PathVariable Long prescriptionId) {
        prescriptionService.deletePrescription(appointmentId, treatmentId, prescriptionId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{appointmentId}/treatments/{treatmentId}")
    @PreAuthorize("hasAnyAuthority('doctor', 'admin')")
    public ResponseEntity<TreatmentDto> updateTreatment(@PathVariable @NotNull Long appointmentId, @PathVariable @NotNull Long treatmentId, @RequestBody @Valid UpdateTreatmentDto updateTreatmentDto) {
        TreatmentDto treatment = treatmentService.updateTreatment(appointmentId, treatmentId, updateTreatmentDto);
        return ResponseEntity.ok(treatment);
    }

    @DeleteMapping("/{appointmentId}/treatments/{treatmentId}")
    @PreAuthorize("hasAnyAuthority('doctor', 'admin')")
    public ResponseEntity<Void> deleteTreatment(@PathVariable Long appointmentId, @PathVariable Long treatmentId) {
        treatmentService.deleteTreatment(appointmentId, treatmentId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsForDoctorInPeriod(@PathVariable @NotNull Long doctorId,
                                                                                 @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                                                 @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctorInPeriod(doctorId, startDate, endDate));
    }
}
