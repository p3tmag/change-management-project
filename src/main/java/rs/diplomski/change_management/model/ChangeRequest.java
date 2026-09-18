package rs.diplomski.change_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * JPA entitet koji predstavlja jedan zahtev za promenu u sistemu.
 * Cuva se u tabeli "change_request" i prati zivotni ciklus promene.
 */
@Entity
@Table(name = "change_request")
public class ChangeRequest {

    /** Vrednosti statusa koji prate zivotni ciklus zahteva za promenu. */
    public static final String STATUS_REGISTERED  = "REGISTERED";
    public static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String STATUS_PRE_CCB_REVIEW = "PRE_CCB_REVIEW";
    public static final String STATUS_CCB_REVIEW   = "CCB_REVIEW";
    public static final String STATUS_ON_HOLD      = "ON_HOLD";
    public static final String STATUS_APPROVED    = "APPROVED";
    public static final String STATUS_REJECTED    = "REJECTED";
    public static final String STATUS_IMPLEMENTED = "IMPLEMENTED";
    public static final String STATUS_COMPLETED   = "COMPLETED";
    public static final String STATUS_BACKED_OUT  = "BACKED_OUT";

    private static final Set<String> KNOWN_STATUSES = Set.of(
            STATUS_REGISTERED, STATUS_UNDER_REVIEW, STATUS_PRE_CCB_REVIEW, STATUS_CCB_REVIEW,
            STATUS_ON_HOLD, STATUS_APPROVED, STATUS_REJECTED, STATUS_IMPLEMENTED,
            STATUS_COMPLETED, STATUS_BACKED_OUT);

    /** Otkriva tipfelere u statusima koji se u BPMN-u zadaju kao obican tekst. */
    public static boolean isKnownStatus(String status) {
        return KNOWN_STATUSES.contains(status);
    }

    private static final String CHANGE_ID_FORMAT = "CHG-%06d";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "change_request_seq")
    @SequenceGenerator(name = "change_request_seq", sequenceName = "change_request_seq", allocationSize = 1)
    private Long id;

    /** Redni identifikator promene izveden iz sekvence (npr. CHG-000042). */
    @Column(unique = true, nullable = false)
    private String changeId;

    @Column
    private String title;

    @Column(length = 2000)
    private String description;

    @Column
    private String priority;

    /** Tekuci status zahteva za promenu. */
    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    /** Prazan konstruktor neophodan za JPA. */
    public ChangeRequest() {
    }

    /** Postavlja vrednosti vremenskih oznaka pre prvog cuvanja. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        // Sekvenca dodeljuje id pre ovog poziva, pa je redni broj vec poznat.
        if (this.changeId == null) {
            this.changeId = String.format(CHANGE_ID_FORMAT, this.id);
        }
    }

    /** Azurira vremensku oznaku poslednje izmene. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getteri i seteri ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChangeId() {
        return changeId;
    }

    public void setChangeId(String changeId) {
        this.changeId = changeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
