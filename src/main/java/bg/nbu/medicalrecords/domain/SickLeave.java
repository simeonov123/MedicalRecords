package bg.nbu.medicalrecords.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sick_leaves")
@Data
public class SickLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many sick leaves can belong to one appointment
    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Appointment appointment;

    private String reason;

    private LocalDate todayDate;

    private LocalDate startDate;
    private LocalDate endDate;

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
