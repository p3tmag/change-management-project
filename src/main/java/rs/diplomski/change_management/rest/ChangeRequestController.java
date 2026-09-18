package rs.diplomski.change_management.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rs.diplomski.change_management.model.ChangeRequest;
import rs.diplomski.change_management.model.ChangeStatusHistory;
import rs.diplomski.change_management.service.ChangeRequestService;

/**
 * REST kontroler za pregled zahteva za promenu.
 * Putanje su pod /api/changes, sto ne koliduje sa Camunda REST-om (/engine-rest).
 */
@RestController
@RequestMapping("/api/changes")
public class ChangeRequestController {

    private final ChangeRequestService service;

    public ChangeRequestController(ChangeRequestService service) {
        this.service = service;
    }

    /**
     * Vraca listu svih zahteva za promenu.
     * GET /api/changes
     *
     * @return lista zahteva u JSON formatu
     */
    @GetMapping
    public List<ChangeRequest> getAllChanges() {
        return service.findAll();
    }

    /**
     * Vraca jedan zahtev za promenu na osnovu changeId-a.
     * GET /api/changes/{changeId}
     *
     * @param changeId identifikator promene (npr. CHG-000042)
     * @return zahtev ili HTTP 404 ako ne postoji
     */
    @GetMapping("/{changeId}")
    public ResponseEntity<ChangeRequest> getChange(@PathVariable String changeId) {
        return service.findByChangeId(changeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Vraca hronolosku istoriju statusa jedne promene.
     * GET /api/changes/{changeId}/history
     *
     * @param changeId identifikator promene (npr. CHG-000042)
     * @return zapisi o prelazima statusa ili HTTP 404 ako zahtev ne postoji
     */
    @GetMapping("/{changeId}/history")
    public ResponseEntity<List<ChangeStatusHistory>> getChangeHistory(@PathVariable String changeId) {
        if (service.findByChangeId(changeId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.findStatusHistory(changeId));
    }
}
