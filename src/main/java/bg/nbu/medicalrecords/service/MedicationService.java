package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Medication;
import bg.nbu.medicalrecords.repository.MedicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<Medication> getAll() {

        return medicationRepository.findAll();
    }

    public Medication findById(Long medicationId) {
        return medicationRepository.findById(medicationId).orElseThrow(() -> new IllegalArgumentException("Medication not found"));
    }
}
