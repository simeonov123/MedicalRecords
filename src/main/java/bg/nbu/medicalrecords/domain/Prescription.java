package bg.nbu.medicalrecords.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
@Data
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many prescriptions can belong to one treatment
    @ManyToOne
    @JoinColumn(name = "treatment_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Treatment treatment;

    // Many prescriptions can use the same medication
    @ManyToOne
    @JoinColumn(name = "medication_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Medication medication;

    // Example: "1 tablet twice a day"
    private String dosage;

    // Duration in days, weeks, etc. Could be int or String.
    private Integer duration;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
