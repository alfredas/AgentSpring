package example.role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.ScriptComponent;
import example.domain.agent.ExampleAgent;
import example.domain.things.Stuff;
import example.repository.StuffRepository;

@ScriptComponent
public class ExampleRole extends AbstractRole<ExampleAgent> implements Role<ExampleAgent> {

    static Logger logger = LoggerFactory.getLogger(ExampleRole.class);

    @Autowired
    StuffRepository stuffRepository;

    @Transactional
    public void act(ExampleAgent agent) {
        logger.warn("I am {}", agent);

        for (Stuff stuff : stuffRepository.findMyStuff(agent)) {
            if (agent.getCash() > stuff.getPrice() && stuff.getPrice() < 0.5) {
                agent.getMyStuff().add(stuff);
                agent.setCash(agent.getCash() - stuff.getPrice());
                agent.persist();
                logger.warn("Just bought {}", stuff.getLabel());
            }
        }
    }
}