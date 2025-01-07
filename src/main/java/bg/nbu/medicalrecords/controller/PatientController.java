package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.CreatePatientDto;
import bg.nbu.medicalrecords.dto.PatientDto;
import bg.nbu.medicalrecords.dto.UpdatePatientDto;
import bg.nbu.medicalrecords.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<PatientDto> create(@Valid @RequestBody CreatePatientDto dto) {
        return ResponseEntity.ok(patientService.createPatient(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<PatientDto> update(@PathVariable Long id, @Valid @RequestBody UpdatePatientDto dto) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<List<PatientDto>> findAll() {
        return ResponseEntity.ok(patientService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor', 'patient')")
    public ResponseEntity<PatientDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @GetMapping("/egn/{egn}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<PatientDto> findByEgn(@PathVariable String egn) {
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
    public ResponseEntity<Void> updateHealthInsuranceStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean healthInsurancePaid = body.get("healthInsurancePaid");
        patientService.updateHealthInsuranceStatus(id, healthInsurancePaid);
        return ResponseEntity.ok().build();
    }
}
