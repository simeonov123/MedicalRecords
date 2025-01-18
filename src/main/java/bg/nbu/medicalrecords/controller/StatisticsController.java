package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.service.PatientService;
import bg.nbu.medicalrecords.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/statistics")

public class StatisticsController {

    private final StatisticsService statisticsService;
    private final PatientService patientService;


    public StatisticsController(StatisticsService statisticsService, PatientService patientService) {
        this.statisticsService = statisticsService;
        this.patientService = patientService;
    }

//    Request URL:
//    http://localhost:8081/statistics/diagnosis/unique
//    Request Method:
//    GET
    @GetMapping("/diagnoses/unique")
    public ResponseEntity<List<String>> getUniqueDiagnosis() {
        return ResponseEntity.ok(statisticsService.getUniqueDiagnosis());
    }


//    Request URL:
//    http://localhost:8081/statistics/diagnosis/leaderboard
//    Request Method:
//    GET

    @GetMapping("/diagnoses/leaderboard")
    public ResponseEntity<DiagnosisStatisticsDto> getDiagnosisLeaderboard() {
        return ResponseEntity.ok(statisticsService.getDiagnosisLeaderboard());
    }

    @GetMapping("/patients/byDoctor/{doctorId}")
    public ResponseEntity<List<PatientDto>> getPatientsByPrimaryDoctor(@PathVariable Long doctorId) {
        List<PatientDto> patients = patientService.findAllByPrimaryDoctorId(doctorId);
        return ResponseEntity.ok(patients);
    }


    //    Request URL:
//    http://localhost:8081/statistics/doctors-with-patient-count
//    Request Method:
//    GET

    @GetMapping("/doctors-with-patient-count")
    public ResponseEntity<List<DoctorPatientCountDto>> getDoctorsWithPatientCount() {
        return ResponseEntity.ok(statisticsService.getDoctorsWithPatientCount());
    }


    //    Request URL:
//    http://localhost:8081/statistics/doctors-with-appointments-count
//    Request Method:
//    GET

    @GetMapping("/doctors-with-appointments-count")
    public ResponseEntity<List<DoctorAppointmentsCount>> getDoctorsWithAppointmentsCount() {
        return ResponseEntity.ok(statisticsService.getDoctorsWithAppointmentsCount());
    }


    //Request URL:
    //http://localhost:8081/statistics/doctors-with-appointments-in-period?startDate=2025-01-05T00:00:00.000&endDate=2025-01-29T00:00:00.000
    //Request Method:
    //GET

    @GetMapping("/doctors-with-appointments-in-period")
    public ResponseEntity<List<DoctorsThatHaveAppointmentsInPeriod>> getDoctorsWithAppointmentsInPeriod(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(statisticsService.getDoctorsWithAppointmentsInPeriod(startDate, endDate));
    }

//Request URL:
//http://localhost:8081/statistics/most-sick-leaves-month-data
//Request Method:
//GET

    @GetMapping("/most-sick-leaves-month-data")
    public ResponseEntity<MostSickLeavesMonthData> getMostSickLeavesMonthData() {
        return ResponseEntity.ok(statisticsService.getMostSickLeavesMonthData());
    }


    @GetMapping("/doctors-sick-leaves-leaderboard")
    public ResponseEntity<List<DoctorsSickLeavesLeaderboardDto>> getDoctorsSickLeavesLeaderboard() {
        List<DoctorsSickLeavesLeaderboardDto> leaderboard = statisticsService.getDoctorsSickLeavesLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }
}
