package rs.diplomski.change_management.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.diplomski.change_management.model.ChangeRequest;
import rs.diplomski.change_management.model.ChangeStatusHistory;
import rs.diplomski.change_management.repository.ChangeRequestRepository;
import rs.diplomski.change_management.repository.ChangeStatusHistoryRepository;

/**
 * Servisni sloj za upravljanje zahtevima za promenu.
 * Posreduje izmedju REST kontrolera odnosno Camunda delegata i repozitorijuma.
 */
@Service
public class ChangeRequestService {

    private final ChangeRequestRepository repository;
    private final ChangeStatusHistoryRepository historyRepository;

    public ChangeRequestService(ChangeRequestRepository repository,
            ChangeStatusHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    /**
     * Registruje novi zahtev za promenu sa rednim identifikatorom
     * i pocetnim statusom REGISTERED.
     *
     * @param title       naslov promene
     * @param description opis promene
     * @param priority    prioritet promene
     * @return sacuvani entitet sa dodeljenim changeId-em
     */
    @Transactional
    public ChangeRequest register(String title, String description, String priority) {
        ChangeRequest cr = new ChangeRequest();
        cr.setTitle(title);
        cr.setDescription(description);
        cr.setPriority(priority);
        cr.setStatus(ChangeRequest.STATUS_REGISTERED);
        ChangeRequest sacuvan = repository.save(cr);
        historyRepository.save(new ChangeStatusHistory(
                sacuvan.getChangeId(), null, ChangeRequest.STATUS_REGISTERED));
        return sacuvan;
    }

    /**
     * Azurira naslov, opis i prioritet postojeceg zahteva za promenu.
     * Poziva se posle dorade zahteva, kada podnosilac izmeni procesne
     * promenljive pa se stanje u bazi mora uskladiti sa njima.
     *
     * @param changeId    identifikator promene (npr. CHG-000042)
     * @param title       novi naslov promene
     * @param description novi opis promene
     * @param priority    novi prioritet promene
     * @return azurirani entitet
     * @throws IllegalArgumentException ako changeId nije prosledjen
     * @throws IllegalStateException    ako zahtev ne postoji u bazi
     */
    @Transactional
    public ChangeRequest updateDetails(String changeId, String title, String description, String priority) {
        ChangeRequest cr = pronadjiIliPukni(changeId);
        cr.setTitle(title);
        cr.setDescription(description);
        cr.setPriority(priority);
        return repository.save(cr);
    }

    /**
     * Azurira status postojeceg zahteva za promenu.
     *
     * @param changeId  identifikator promene (npr. CHG-000042)
     * @param newStatus novi status, mora biti jedna od poznatih vrednosti
     * @return azurirani entitet
     * @throws IllegalArgumentException ako su ulazni podaci neispravni
     * @throws IllegalStateException    ako zahtev ne postoji u bazi
     */
    @Transactional
    public ChangeRequest updateStatus(String changeId, String newStatus) {
        if (!ChangeRequest.isKnownStatus(newStatus)) {
            throw new IllegalArgumentException("Nepoznat status zahteva za promenu: " + newStatus);
        }

        ChangeRequest cr = pronadjiIliPukni(changeId);
        String stariStatus = cr.getStatus();
        cr.setStatus(newStatus);
        ChangeRequest sacuvan = repository.save(cr);

        if (!newStatus.equals(stariStatus)) {
            historyRepository.save(new ChangeStatusHistory(changeId, stariStatus, newStatus));
        }
        return sacuvan;
    }

    /**
     * Vraca listu svih zahteva za promenu.
     *
     * @return lista svih zahteva
     */
    @Transactional(readOnly = true)
    public List<ChangeRequest> findAll() {
        return repository.findAll();
    }

    /**
     * Trazi zahtev za promenu po Camunda identifikatoru.
     *
     * @param changeId identifikator promene (npr. CHG-000042)
     * @return Optional koji sadrzi entitet ako postoji
     */
    @Transactional(readOnly = true)
    public Optional<ChangeRequest> findByChangeId(String changeId) {
        return repository.findByChangeId(changeId);
    }

    /**
     * Vraca hronolosku istoriju statusa jedne promene. Za razliku od Camundine
     * istorije, ovi zapisi ostaju i posle isteka roka od 180 dana.
     *
     * @param changeId identifikator promene (npr. CHG-000042)
     * @return zapisi od najstarijeg ka najnovijem
     */
    @Transactional(readOnly = true)
    public List<ChangeStatusHistory> findStatusHistory(String changeId) {
        return historyRepository.findByChangeIdOrderByChangedAtAscIdAsc(changeId);
    }

    private ChangeRequest pronadjiIliPukni(String changeId) {
        if (changeId == null || changeId.isBlank()) {
            throw new IllegalArgumentException("Identifikator promene (changeId) nije prosledjen.");
        }
        return repository.findByChangeId(changeId)
                .orElseThrow(() -> new IllegalStateException(
                        "Zahtev za promenu " + changeId + " ne postoji u bazi."));
    }
}
