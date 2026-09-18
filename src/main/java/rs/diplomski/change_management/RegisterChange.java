package rs.diplomski.change_management;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Named;
import rs.diplomski.change_management.model.ChangeRequest;
import rs.diplomski.change_management.service.ChangeRequestService;

/**
 * Registruje novi zahtev za promenu: dodeljuje jedinstveni identifikator
 * promene (changeId), belezi je u sistem i cuva u bazi podataka.
 */
@Named
public class RegisterChange implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterChange.class);

    private final ChangeRequestService changeRequestService;

    public RegisterChange(ChangeRequestService changeRequestService) {
        this.changeRequestService = changeRequestService;
    }

    @Override
    public void execute(DelegateExecution execution) {

        String priority = (String) execution.getVariable("changePriority");

        ChangeRequest cr = changeRequestService.register(
                (String) execution.getVariable("changeTitle"),
                (String) execution.getVariable("changeDescription"),
                priority);

        execution.setVariable("changeId", cr.getChangeId());
        execution.setVariable("changePriorityLabel", ChangePriorityLabels.of(priority));

        LOGGER.info("Registrovana nova promena: {} - \"{}\" (prioritet: {})",
                cr.getChangeId(), cr.getTitle(), cr.getPriority());
    }
}

