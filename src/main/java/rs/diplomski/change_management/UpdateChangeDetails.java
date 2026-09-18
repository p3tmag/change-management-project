package rs.diplomski.change_management;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Named;
import rs.diplomski.change_management.service.ChangeRequestService;

/**
 * Prepisuje naslov, opis i prioritet promene iz procesnih promenljivih u bazu.
 * Koristi se posle dorade zahteva, gde podnosilac menja changeTitle,
 * changeDescription i changePriority, pa bi u tabeli inace ostale
 * prvobitne, odbacene vrednosti.
 *
 * Poziva se preko camunda:delegateExpression="${updateChangeDetails}".
 */
@Named
public class UpdateChangeDetails implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateChangeDetails.class);

    private final ChangeRequestService changeRequestService;

    public UpdateChangeDetails(ChangeRequestService changeRequestService) {
        this.changeRequestService = changeRequestService;
    }

    @Override
    public void execute(DelegateExecution execution) {

        String changeId = (String) execution.getVariable("changeId");
        String priority = (String) execution.getVariable("changePriority");

        changeRequestService.updateDetails(
                changeId,
                (String) execution.getVariable("changeTitle"),
                (String) execution.getVariable("changeDescription"),
                priority);

        execution.setVariable("changePriorityLabel", ChangePriorityLabels.of(priority));

        LOGGER.info("Podaci promene {} azurirani posle dorade zahteva.", changeId);
    }
}
