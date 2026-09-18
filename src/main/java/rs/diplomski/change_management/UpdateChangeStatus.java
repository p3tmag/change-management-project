package rs.diplomski.change_management;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import jakarta.inject.Named;
import rs.diplomski.change_management.service.ChangeRequestService;

/**
 * Azurira status zahteva za promenu u bazi podataka na osnovu injektovanog
 * polja "status". Poziva se preko camunda:delegateExpression="#{updateChangeStatus}".
 *
 * Primer upotrebe u BPMN-u:
 *   camunda:delegateExpression="#{updateChangeStatus}"
 *   sa camunda:field name="status" vrednoscu npr. "APPROVED"
 */
@Named
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UpdateChangeStatus implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateChangeStatus.class);

    private final ChangeRequestService changeRequestService;

    /** Novi status koji treba upisati — injektuje se iz BPMN-a. */
    private Expression status;

    public UpdateChangeStatus(ChangeRequestService changeRequestService) {
        this.changeRequestService = changeRequestService;
    }

    @Override
    public void execute(DelegateExecution execution) {

        if (status == null) {
            throw new IllegalStateException(
                    "Polje 'status' nije injektovano u UpdateChangeStatus — proveriti camunda:field u BPMN-u.");
        }

        String newStatus = (String) status.getValue(execution);
        String changeId = (String) execution.getVariable("changeId");

        changeRequestService.updateStatus(changeId, newStatus);

        LOGGER.info("Status promene {} azuriran na: {}.", changeId, newStatus);
    }

    public void setStatus(Expression status) {
        this.status = status;
    }
}
