package rs.diplomski.change_management;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Dodeljuje ovlascenja grupama kada je provera autorizacije ukljucena.
 *
 * Princip:
 *  - Sve grupe mogu da otvore Tasklist i vide definiciju/instance procesa.
 *  - Rad na konkretnom zadatku je automatski ogranicen na kandidat-grupu
 *    zadatka (camunda:candidateGroups) preko ugradjenog
 *    DefaultAuthorizationProvider-a, pa npr. samo "smt" moze da pregleda,
 *    samo "ccb" da odlucuje itd.
 *  - Samo grupa "requester" moze da pokrene proces (posalje zahtev za promenu).
 *
 * Kreiranje je idempotentno.
 */
@Component
@Order(2)
public class AuthorizationSetup implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationSetup.class);

    private static final String PROCESS_KEY = "changeManagement";
    private static final String[] ALL_GROUPS = {
            "requester", "smt", "ccb", "devops", "approver", "validator"
    };

    private final AuthorizationService authorizationService;

    public AuthorizationSetup(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String group : ALL_GROUPS) {
            // Pristup Tasklist aplikaciji i citanje filtera (da bi se videla lista zadataka)
            grant(group, Resources.APPLICATION, "tasklist", Permissions.ACCESS);
            grant(group, Resources.FILTER, "*", Permissions.READ);

            if ("requester".equals(group)) {
                // Samo podnosioci mogu da pokrenu proces (posalju zahtev za promenu)
                grant(group, Resources.PROCESS_DEFINITION, PROCESS_KEY,
                        Permissions.READ, Permissions.CREATE_INSTANCE);
                grant(group, Resources.PROCESS_INSTANCE, "*",
                        Permissions.READ, Permissions.CREATE);
            } else {
                grant(group, Resources.PROCESS_DEFINITION, PROCESS_KEY, Permissions.READ);
                grant(group, Resources.PROCESS_INSTANCE, "*", Permissions.READ);
            }
        }
    }

    private void grant(String groupId, Resource resource, String resourceId, Permission... permissions) {
        boolean exists = authorizationService.createAuthorizationQuery()
                .groupIdIn(groupId)
                .resourceType(resource)
                .resourceId(resourceId)
                .count() > 0;
        if (exists) {
            return;
        }

        Authorization auth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        auth.setGroupId(groupId);
        auth.setResource(resource);
        auth.setResourceId(resourceId);
        for (Permission permission : permissions) {
            auth.addPermission(permission);
        }
        authorizationService.saveAuthorization(auth);

        LOGGER.info("Ovlascenje: grupa '{}' -> {} [{}]", groupId, resource.resourceName(), resourceId);
    }
}
