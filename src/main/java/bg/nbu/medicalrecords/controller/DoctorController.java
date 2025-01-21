package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.service.DoctorService;
import bg.nbu.medicalrecords.service.PatientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin', 'doctor', 'patient')")
    public ResponseEntity<List<Doctor>> findAll() {
        return ResponseEntity.ok(doctorService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Doctor> create(@RequestBody @Valid Doctor doctor) {
        return ResponseEntity.ok(doctorService.createDoctor(doctor));
    }

    @PutMapping("/{keycloakUserId}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<Doctor> updateByKeycloakUserId(@PathVariable String keycloakUserId, @RequestBody @Valid Doctor updated) {
        return ResponseEntity.ok(doctorService.updateDoctorByKeycloakUserId(keycloakUserId, updated));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> delete(@PathVariable @NotNull Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<Doctor> findById(@PathVariable @NotNull Long id) {
        return ResponseEntity.ok(doctorService.findById(id));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<Doctor> findByPrincipal() {
        return ResponseEntity.ok(doctorService.findByPrincipal());
    }
}

