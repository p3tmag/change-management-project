package rs.diplomski.change_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * JPA entitet koji belezi jednu promenu statusa zahteva za promenu.
 * Tabela "change_status_history" cuva trag o zivotnom ciklusu i posle isteka
 * Camundinog roka za brisanje istorije (history-time-to-live = 180 dana).
 */
@Entity
@Table(name = "change_status_history", indexes = @Index(name = "idx_csh_change_id", columnList = "changeId"))
public class ChangeStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "change_status_history_seq")
    @SequenceGenerator(name = "change_status_history_seq", sequenceName = "change_status_history_seq", allocationSize = 1)
    private Long id;

    /** Identifikator promene na koju se zapis odnosi (npr. CHG-000042). */
    @Column(nullable = false, updatable = false)
    private String changeId;

    /** Status pre izmene; prazan je samo za prvi zapis (registracija). */
    @Column(updatable = false)
    private String oldStatus;

    @Column(nullable = false, updatable = false)
    private String newStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    /** Prazan konstruktor neophodan za JPA. */
    public ChangeStatusHistory() {
    }

    public ChangeStatusHistory(String changeId, String oldStatus, String newStatus) {
        this.changeId = changeId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
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

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
