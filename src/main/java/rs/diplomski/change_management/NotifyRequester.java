package rs.diplomski.change_management;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import jakarta.inject.Named;
import rs.diplomski.change_management.model.ChangeRequest;
import rs.diplomski.change_management.service.ChangeRequestService;

/**
 * Obavestava podnosioca zahteva o ishodu promene (odbijena, odustanak ili
 * uspesno zavrsena) i azurira status u bazi podataka. Ishod se prosledjuje
 * preko injektovanog polja "outcome".
 *
 * Koristi se preko camunda:delegateExpression="#{notifyRequester}".
 * Prototype opseg osigurava bezbednu injekciju polja za svaku instancu zadatka.
 */
@Named
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NotifyRequester implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyRequester.class);

    private final ChangeRequestService changeRequestService;

    private Expression outcome;

    public NotifyRequester(ChangeRequestService changeRequestService) {
        this.changeRequestService = changeRequestService;
    }

    @Override
    public void execute(DelegateExecution execution) {

        if (outcome == null) {
            throw new IllegalStateException(
                    "Polje 'outcome' nije injektovano u NotifyRequester — proveriti camunda:field u BPMN-u.");
        }

        String outcomeValue = (String) outcome.getValue(execution);

        String message;
        String newStatus;
        switch (outcomeValue) {
            case "rejected":
                message = "odbijena";
                newStatus = ChangeRequest.STATUS_REJECTED;
                break;
            case "backedOut":
                message = "povucena (odustali smo od promene)";
                newStatus = ChangeRequest.STATUS_BACKED_OUT;
                break;
            case "completed":
                message = "uspesno zavrsena";
                newStatus = ChangeRequest.STATUS_COMPLETED;
                break;
            default:
                throw new IllegalStateException(
                        "Nepoznat ishod promene u NotifyRequester: " + outcomeValue);
        }

        String changeId = (String) execution.getVariable("changeId");

        LOGGER.info("Obavestenje podnosiocu: promena [{}] \"{}\" je {}.",
                changeId, execution.getVariable("changeTitle"), message);

        execution.setVariable("lastNotification", message);

        changeRequestService.updateStatus(changeId, newStatus);

        LOGGER.info("Status promene {} azuriran na: {}.", changeId, newStatus);
    }

    public void setOutcome(Expression outcome) {
        this.outcome = outcome;
    }
}

