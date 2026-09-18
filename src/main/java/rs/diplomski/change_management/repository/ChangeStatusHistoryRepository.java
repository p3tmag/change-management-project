package rs.diplomski.change_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.diplomski.change_management.model.ChangeStatusHistory;

/**
 * Repozitorijum za pristup istoriji statusa zahteva za promenu.
 */
public interface ChangeStatusHistoryRepository extends JpaRepository<ChangeStatusHistory, Long> {

    /**
     * Vraca hronoloski poredjanu istoriju statusa jedne promene.
     *
     * @param changeId identifikator promene (npr. CHG-000042)
     * @return zapisi od najstarijeg ka najnovijem
     */
    List<ChangeStatusHistory> findByChangeIdOrderByChangedAtAscIdAsc(String changeId);
}
