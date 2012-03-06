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
        logger.warn("I am {}", agent.getName());
        
        long stuffCount = stuffRepository.count();
        long randomStuffIndex = Math.min(Math.round(Math.random() * stuffCount), stuffCount-1);
        
        Stuff randomStuff = null;
        int ix = 0;
        for (Stuff stuff : stuffRepository.findAll()) {
            if (ix == randomStuffIndex) {
                randomStuff = stuff;
                break;
            }
            ix++;
        }
        
        if (agent.getCash() > randomStuff.getPrice()) {
            agent.getMyStuff().add(randomStuff);
            agent.setCash(agent.getCash() - randomStuff.getPrice());
            agent.persist();
            logger.warn("Just bought {}", randomStuff.getName());
        }
        String stuffStr = "";
        for (Stuff stuff : stuffRepository.findMyStuff(agent)) {
            stuffStr += stuff.getName() + " ";
        }
        logger.warn("Now I've got {}", stuffStr);
    }
}