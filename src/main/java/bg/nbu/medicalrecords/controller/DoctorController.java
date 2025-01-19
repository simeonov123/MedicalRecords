package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.service.DoctorService;
import bg.nbu.medicalrecords.service.PatientService;
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
    public ResponseEntity<Doctor> create(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.createDoctor(doctor));
    }

//    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('admin')")
//    public ResponseEntity<Doctor> update(@PathVariable Long id, @RequestBody Doctor updated) {
//        return ResponseEntity.ok(doctorService.updateDoctor(id, updated));
//    }

    // // New method: Update doctor by Keycloak User ID
    //    final response = await _apiService.put(
    //      '/doctors/${doc.keycloakUserId}',
    //      body: {
    //        "primaryCare": doc.primaryCare,
    //        "specialties": doc.specialties.isEmpty ? "N/A" : doc.specialties,
    //      },
    //    );

    @PutMapping("/{keycloakUserId}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<Doctor> updateByKeycloakUserId(@PathVariable String keycloakUserId, @RequestBody Doctor updated) {
        return ResponseEntity.ok(doctorService.updateDoctorByKeycloakUserId(keycloakUserId, updated));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<Doctor> findById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.findById(id));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAnyAuthority('admin', 'doctor')")
    public ResponseEntity<Doctor> findByPrincipal() {
        return ResponseEntity.ok(doctorService.findByPrincipal());
    }
}

