package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.DiagnosisStatisticsDto;
import bg.nbu.medicalrecords.dto.PatientDto;
import bg.nbu.medicalrecords.service.PatientService;
import bg.nbu.medicalrecords.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
