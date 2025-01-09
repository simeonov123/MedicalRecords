package bg.nbu.medicalrecords.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "treatments")
@Data
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many treatments can belong to one diagnosis
    @ManyToOne
    @JoinColumn(name = "diagnosis_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Diagnosis diagnosis;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDate startDate;
    private LocalDate endDate;

    private String description;

    // One treatment can have many prescriptions
    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Prescription> prescriptions = new ArrayList<>();


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
