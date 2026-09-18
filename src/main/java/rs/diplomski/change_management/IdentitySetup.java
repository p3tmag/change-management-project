package rs.diplomski.change_management;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Kreira demo grupe i korisnike pri pokretanju aplikacije, kako bi se
 * u Camunda Tasklist-u moglo prijaviti kao razliciti ucesnici procesa
 * (podnosilac, SMT, CCB, DevOps, odobravalac, validator) i preuzimati
 * zadatke dodeljene odgovarajucim grupama (camunda:candidateGroups).
 *
 * Kreiranje je idempotentno — postojeci korisnici/grupe se ne diraju.
 */
@Component
@Order(1)
public class IdentitySetup implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentitySetup.class);

    private final IdentityService identityService;

    public IdentitySetup(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureGroup("requester", "Podnosioci zahteva");
        ensureGroup("smt", "Tim za upravljanje uslugama");
        ensureGroup("ccb", "Odbor za kontrolu promena (CCB)");
        ensureGroup("devops", "DevOps");
        ensureGroup("approver", "Odobravaoci promene");
        ensureGroup("validator", "Validatori");

        ensureUser("pera", "Petar", "Peric", "pera", "requester");
        ensureUser("sofija", "Sofija", "Simic", "sofija", "smt");
        ensureUser("nikola", "Nikola", "Nikolic", "nikola", "ccb");
        ensureUser("milan", "Milan", "Milic", "milan", "devops");
        ensureUser("jelena", "Jelena", "Jovic", "jelena", "approver");
        ensureUser("vlada", "Vladimir", "Vasic", "vlada", "validator");
    }

    private void ensureGroup(String id, String name) {
        if (identityService.createGroupQuery().groupId(id).count() == 0) {
            Group group = identityService.newGroup(id);
            group.setName(name);
            group.setType("WORKFLOW");
            identityService.saveGroup(group);
            LOGGER.info("Kreirana grupa: {} ({})", id, name);
        }
    }

    private void ensureUser(String id, String firstName, String lastName, String password, String group) {
        if (identityService.createUserQuery().userId(id).count() == 0) {
            User user = identityService.newUser(id);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(password);
            user.setEmail(id + "@example.com");
            identityService.saveUser(user);
            identityService.createMembership(id, group);
            LOGGER.info("Kreiran korisnik: {} (grupa: {})", id, group);
        }
    }
}
