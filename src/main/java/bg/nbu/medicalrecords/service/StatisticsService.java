package bg.nbu.medicalrecords.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsService {

    public final DiagnosisService diagnosisService;

    public final PatientService patientService;

    public StatisticsService(DiagnosisService diagnosisService, PatientService patientService) {
        this.diagnosisService = diagnosisService;
        this.patientService = patientService;
    }


    public List<String> getUniqueDiagnosis() {
        return diagnosisService.getUniqueDiagnosis();
    }
}
