package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Appointment;
import bg.nbu.medicalrecords.dto.PatientDto;
import bg.nbu.medicalrecords.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/patients-by-diagnosis/{diagnosisId}")
    public ResponseEntity<List<PatientDto>> patientsByDiagnosis(@PathVariable Long diagnosisId) {
        return ResponseEntity.ok(reportService.findPatientsByDiagnosis(diagnosisId));
    }

    @GetMapping("/most-common-diagnoses")
    public ResponseEntity<List<String>> mostCommonDiagnoses() {
        return ResponseEntity.ok(reportService.findMostCommonDiagnoses());
    }

    @GetMapping("/patients-by-primary-doctor/{doctorId}")
    public ResponseEntity<List<PatientDto>> patientsByPrimaryDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reportService.findPatientsByPrimaryDoctor(doctorId));
    }

    @GetMapping("/count-patients-per-doctor")
    public ResponseEntity<Map<Long, Long>> countPatientsPerDoctor() {
        return ResponseEntity.ok(reportService.countPatientsPerPrimaryDoctor());
    }

    @GetMapping("/count-visits-per-doctor")
    public ResponseEntity<Map<Long, Long>> countVisitsPerDoctor() {
        return ResponseEntity.ok(reportService.countVisitsPerDoctor());
    }

    @GetMapping("/count-visits-per-patient")
    public ResponseEntity<Map<Long, Long>> countVisitsPerPatient() {
        return ResponseEntity.ok(reportService.countVisitsPerPatient());
    }

    @GetMapping("/visits-in-period")
    public ResponseEntity<List<Appointment>> visitsInPeriod(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(reportService.findVisitsInPeriod(start, end));
    }

    @GetMapping("/visits-for-doctor-in-period")
    public ResponseEntity<List<Appointment>> visitsForDoctorInPeriod(
            @RequestParam("doctorId") Long doctorId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(reportService.findVisitsForDoctorInPeriod(doctorId, start, end));
    }

    @GetMapping("/month-with-most-sick-leaves")
    public ResponseEntity<Month> monthWithMostSickLeaves() {
        return ResponseEntity.ok(reportService.findMonthWithMostSickLeaves());
    }

    @GetMapping("/doctors-with-most-sick-leaves")
    public ResponseEntity<List<Long>> doctorsWithMostSickLeaves() {
        return ResponseEntity.ok(reportService.findDoctorsWhoIssuedMostSickLeaves());
    }
}
