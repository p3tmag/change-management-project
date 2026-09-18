package rs.diplomski.change_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.diplomski.change_management.model.ChangeRequest;

/**
 * Repozitorijum za pristup podacima zahteva za promenu u bazi.
 */
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {

    /**
     * Pronalazi zahtev za promenu po jedinstvenom identifikatoru (changeId).
     *
     * @param changeId identifikator promene (npr. CHG-000042)
     * @return Optional koji sadrzi entitet ako postoji
     */
    Optional<ChangeRequest> findByChangeId(String changeId);
}
