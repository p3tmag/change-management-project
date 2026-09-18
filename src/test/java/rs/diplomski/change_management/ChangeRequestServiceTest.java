package rs.diplomski.change_management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import rs.diplomski.change_management.model.ChangeRequest;
import rs.diplomski.change_management.model.ChangeStatusHistory;
import rs.diplomski.change_management.service.ChangeRequestService;

@SpringBootTest
class ChangeRequestServiceTest {

    @Autowired
    private ChangeRequestService service;

    @Test
    void dodeljujeRedneIdentifikatore() {
        ChangeRequest prvi = service.register("Prva promena", "Opis", "HIGH");
        ChangeRequest drugi = service.register("Druga promena", "Opis", "LOW");

        assertThat(prvi.getChangeId()).matches("CHG-\\d{6}");
        assertThat(drugi.getChangeId()).matches("CHG-\\d{6}");
        assertThat(brojIz(drugi)).isEqualTo(brojIz(prvi) + 1);
        assertThat(prvi.getStatus()).isEqualTo(ChangeRequest.STATUS_REGISTERED);
    }

    @Test
    void azuriraStatusPoChangeId() {
        ChangeRequest cr = service.register("Treca promena", "Opis", "MEDIUM");

        service.updateStatus(cr.getChangeId(), ChangeRequest.STATUS_APPROVED);

        assertThat(service.findByChangeId(cr.getChangeId()))
                .get()
                .extracting(ChangeRequest::getStatus)
                .isEqualTo(ChangeRequest.STATUS_APPROVED);
    }

    @Test
    void azuriraPodatkePosleDorade() {
        ChangeRequest cr = service.register("Prvobitni naslov", "Prvobitni opis", "LOW");

        service.updateDetails(cr.getChangeId(), "Doradjen naslov", "Doradjen opis", "HIGH");

        assertThat(service.findByChangeId(cr.getChangeId()))
                .get()
                .extracting(ChangeRequest::getTitle, ChangeRequest::getDescription, ChangeRequest::getPriority)
                .containsExactly("Doradjen naslov", "Doradjen opis", "HIGH");
    }

    @Test
    void beleziIstorijuStatusa() {
        ChangeRequest cr = service.register("Peta promena", "Opis", "HIGH");

        service.updateStatus(cr.getChangeId(), ChangeRequest.STATUS_UNDER_REVIEW);
        service.updateStatus(cr.getChangeId(), ChangeRequest.STATUS_UNDER_REVIEW);
        service.updateStatus(cr.getChangeId(), ChangeRequest.STATUS_APPROVED);

        assertThat(service.findStatusHistory(cr.getChangeId()))
                .extracting(ChangeStatusHistory::getOldStatus, ChangeStatusHistory::getNewStatus)
                .containsExactly(
                        tuple(null, ChangeRequest.STATUS_REGISTERED),
                        tuple(ChangeRequest.STATUS_REGISTERED, ChangeRequest.STATUS_UNDER_REVIEW),
                        tuple(ChangeRequest.STATUS_UNDER_REVIEW, ChangeRequest.STATUS_APPROVED));
    }

    private int brojIz(ChangeRequest cr) {
        return Integer.parseInt(cr.getChangeId().substring("CHG-".length()));
    }
}
