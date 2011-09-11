package agentspring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.repository.DirectGraphRepositoryFactory;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Component;

import agentspring.facade.EngineState;
import agentspring.role.Role;
import agentspring.role.ScriptComponent;

@Component
public class SimpleSimulationRunner implements Simulation {

    static Logger logger = Logger.getLogger(SimpleSimulationRunner.class);

    private ApplicationContext applicationContext;

    @Autowired
    DirectGraphRepositoryFactory graphRepositoryFactory;

    // @Autowired
    // private GenericRepository genericRepository;
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
                    GraphRepository<? extends AbstractAgent> agentRepository = graphRepositoryFactory
                            .createGraphRepository(agentClass);
                    List<? extends AbstractAgent> agents = Utils.asList(agentRepository.findAll());
                    Collections.shuffle(agents, new Random());
                    // add role for every agent in the store
                    for (AbstractAgent agent : agents) {
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
    public void listen(SimulationListener listener) {
        Schedule.getSchedule().listen(listener);
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
    public EngineState getState() {
        return Schedule.getSchedule().getState();
    }

    @Override
    public void pauseSimulation() {
        Schedule.getSchedule().pause();
    }

    @Override
    public void resumeSimulation() {
        Schedule.getSchedule().resume();
    }

    @Override
    public void wake() {
        Schedule.getSchedule().wake();
    }

    static class Utils {

        public static <T> List<T> asList(Iterable<T> iterable) {
            List<T> list;
            if (iterable instanceof List<?>) {
                list = (List<T>) iterable;
            } else {
                list = new ArrayList<T>();
                for (T t : iterable) {
                    list.add(t);
                }
            }
            return list;
        }

        public static <T, E extends T> List<E> asCastedList(Iterable<T> iterable) {
            List<E> list = new ArrayList<E>();
            for (T t : iterable) {
                list.add((E) t);
            }
            return list;
        }

        public static <E, T extends E> List<E> asDownCastedList(Iterable<T> iterable) {
            List<E> list = new ArrayList<E>();
            for (T t : iterable) {
                list.add((E) t);
            }
            return list;
        }

    }

}
