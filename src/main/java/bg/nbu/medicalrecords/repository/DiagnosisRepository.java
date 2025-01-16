package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    @Query("SELECT DISTINCT d.statement FROM Diagnosis d")
    List<String> findDistinctStatements();

    List<Diagnosis> findByStatementIgnoreCase(String statement);
}