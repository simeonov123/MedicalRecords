package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.CreatePatientDto;
import bg.nbu.medicalrecords.dto.PatientDto;
import bg.nbu.medicalrecords.dto.UpdatePatientDto;
import bg.nbu.medicalrecords.service.PatientService;
import bg.nbu.medicalrecords.service.StatisticsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;
    private final StatisticsService statisticsService;

    public PatientController(PatientService patientService, StatisticsService statisticsService) {
        this.patientService = patientService;
        this.statisticsService = statisticsService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<PatientDto> create(@Valid @RequestBody CreatePatientDto dto) {
        return ResponseEntity.ok(patientService.createPatient(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<PatientDto> update(@PathVariable @NotNull String id, @Valid @RequestBody UpdatePatientDto dto) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> delete(@PathVariable @NotNull Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<List<PatientDto>> findAll() {
        return ResponseEntity.ok(patientService.findAll());
    }


    @GetMapping("/searchByDiagnosis")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<List<PatientDto>> findAllByStatement(@RequestParam @NotNull String diagnosis) {
        return ResponseEntity.ok(statisticsService.findAllByStatement(diagnosis));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor', 'patient')")
    public ResponseEntity<PatientDto> findById(@PathVariable @NotNull Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @GetMapping("/egn/{egn}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<PatientDto> findByEgn(@PathVariable @NotNull String egn) {
        return ResponseEntity.ok(patientService.findByEgn(egn));
    }


    @PutMapping("/{id}/primary-doctor/{doctorId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> assignPrimaryDoctor(
            @PathVariable Long id,
            @PathVariable Long doctorId
    ) {
        patientService.assignPrimaryDoctor(id, doctorId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/health-insurance")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> updateHealthInsuranceStatus(@PathVariable @NotNull Long id, @RequestBody @Valid Map<String, Boolean> body) {
        Boolean healthInsurancePaid = body.get("healthInsurancePaid");
        patientService.updateHealthInsuranceStatus(id, healthInsurancePaid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/keycloak-user-id/{keycloakUserId}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor', 'patient')")
    public ResponseEntity<PatientDto> findByKeycloakUserId(@PathVariable @NotNull String keycloakUserId) {
        return ResponseEntity.ok(patientService.findByKeycloakUserId(keycloakUserId));
    }
}
