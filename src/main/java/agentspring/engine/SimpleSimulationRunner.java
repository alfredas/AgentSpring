package agentspring.engine;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import agentspring.engine.graphstore.GenericRepository;
import agentspring.engine.role.Role;
import agentspring.engine.role.ScriptComponent;

@Component
public class SimpleSimulationRunner implements Simulation {

    static Logger logger = LoggerFactory.getLogger(SimpleSimulationRunner.class);

    private ApplicationContext applicationContext;

    @Autowired
    private GenericRepository genericRepository;

    @SuppressWarnings("unchecked")
    private void buildSchedule() {
        // get scheduled roles ...
        Map<String, Object> roleMap = getApplicationContext().getBeansWithAnnotation(ScriptComponent.class);
        Schedule.getSchedule().clear();

        // ... and add them to the schedule
        for (Object obj : roleMap.values()) {
            Role<? extends AbstractAgent> role = (Role<? extends AbstractAgent>) obj;
            ScriptComponent annotation = role.getClass().getAnnotation(ScriptComponent.class);

            if (!annotation.enabled()) {
                continue;
            }

            String roleName = annotation.name().equals("") ? role.getClass().getSimpleName() : annotation.name();
            Class<? extends AbstractAgent> agentClass = role.agentClass();
            String agentName = agentClass.getSimpleName();

            logger.info("Will try to add role " + roleName + " for agent type " + agentName + " to the schedule");

            try {
                if (!agentClass.isInterface()) {
                    // add role for every agent in the store
                    for (Agent agent : genericRepository.findAllAtRandom(agentClass)) {
                        logger.info("Adding agent {} with role {} to schedule.", agent, role);
                        Schedule.getSchedule().addRole(roleName, role, agent);
                    }
                } else {
                    // enable roles like rain
                    Schedule.getSchedule().addRole(roleName, role, null);
                }
            } catch (Exception e) {
                logger.error("Error adding role", e);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public long getCurrentTick() {
        return Schedule.getSchedule().getCurrentTick();
    }

    @Override
    public void runSimulation() {
        this.buildSchedule();
        Schedule.getSchedule().start();
    }

    @Override
    public void stopSimulation() {
        Schedule.getSchedule().stop();
    }

    @Override
    public boolean isRunning() {
        return Schedule.getSchedule().isRunning();
    }

    @Override
    public void pauseSimulation() {
        Schedule.getSchedule().pause();
    }

    @Override
    public void resumeSimulation() {
        Schedule.getSchedule().resume();
    }

}
