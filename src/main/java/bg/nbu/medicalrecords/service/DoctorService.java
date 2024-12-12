package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        Optional<Doctor> existing = doctorRepository.findById(id);
        if (existing.isPresent()) {
            Doctor doc = existing.get();
            doc.setName(updatedDoctor.getName());
            doc.setUniqueIdentifier(updatedDoctor.getUniqueIdentifier());
            doc.setSpecialties(updatedDoctor.getSpecialties());
            doc.setPrimaryCare(updatedDoctor.isPrimaryCare());
            return doctorRepository.save(doc);
        }
        throw new RuntimeException("Doctor not found with id: " + id);
    }

    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public Doctor findById(Long id) {
        return doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }
}
