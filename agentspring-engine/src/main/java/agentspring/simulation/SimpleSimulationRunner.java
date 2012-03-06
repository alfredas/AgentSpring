package agentspring.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Component;

import agentspring.agent.AbstractAgent;
import agentspring.facade.EngineState;
import agentspring.role.Role;
import agentspring.role.ScriptComponent;

/**
 * Simulation implementation. Start/stop/pause/listen
 * 
 * @author alfredas
 * 
 */
@Component
public class SimpleSimulationRunner implements Simulation {

    static Logger logger = LoggerFactory.getLogger(SimpleSimulationRunner.class);

    private ApplicationContext applicationContext;

    @Autowired
    Neo4jTemplate template;

    @SuppressWarnings("unchecked")
    private void buildSchedule() {

        // clear schedule
        Schedule.getSchedule().clear();

        // get scheduled roles ...
        Map<String, Object> map = getApplicationContext().getBeansWithAnnotation(ScriptComponent.class);
        List<Role<? extends AbstractAgent>> roleList = new ArrayList<Role<? extends AbstractAgent>>();
        for (Object obj : map.values()) {
            Role<? extends AbstractAgent> role = (Role<? extends AbstractAgent>) obj;
            roleList.add(role);
        }
        // find their index
        Map<Role<? extends AbstractAgent>, Integer> roleIndexMap = new HashMap<Role<? extends AbstractAgent>, Integer>();
        for (Role<? extends AbstractAgent> role : roleList) {
            if (!roleIndexMap.containsKey(role)) {
                roleIndexMap.put(role, findRoleIndex(role, roleList));
            }
        }

        // create comparator based on index values
        ValueComparator comparator = new ValueComparator(roleIndexMap);
        // sort by value using treemap
        TreeMap<Role<? extends AbstractAgent>, Integer> sortedIdexMap = new TreeMap(comparator);
        sortedIdexMap.putAll(roleIndexMap);

        // ... and add them to the schedule
        for (Role<? extends AbstractAgent> role : sortedIdexMap.keySet()) {
            ScriptComponent annotation = role.getClass().getAnnotation(ScriptComponent.class);
            String roleName = annotation.name().equals("") ? role.getClass().getSimpleName() : annotation.name();
            Class<? extends AbstractAgent> agentClass = role.agentClass();
            String agentName = agentClass.getSimpleName();

            logger.info("Will try to add role " + roleName + " for agent type " + agentName + " to the schedule");

            try {
                if (!agentClass.isInterface()) {
                    // Alfredas: Change
                    List<? extends AbstractAgent> agents = Utils.asList(template.findAll(agentClass));

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

    private int findRoleIndex(Role<? extends AbstractAgent> role, List<Role<? extends AbstractAgent>> roleList) {
        ScriptComponent annotation = role.getClass().getAnnotation(ScriptComponent.class);
        if (annotation.first()) {
            return 0;
        }
        if (annotation.last()) {
            return roleList.size() - 1;
        }
        if (!annotation.after().equals("")) {
            String after = annotation.after();
            for (Role<? extends AbstractAgent> r : roleList) {
                ScriptComponent a = r.getClass().getAnnotation(ScriptComponent.class);
                String afterName = a.name().equals("") ? r.getClass().getSimpleName() : a.name();
                if (afterName.equals(after)) {
                    return findRoleIndex(r, roleList) + 1;
                }
            }
        }
        return 0;
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

    @SuppressWarnings("rawtypes")
    class ValueComparator implements Comparator {

        @SuppressWarnings("rawtypes")
        Map base;

        public ValueComparator(Map base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {
            if ((Integer) base.get(a) >= (Integer) base.get(b)) {
                return 1;
            } else {
                return -1;
            }
        }
    }

}
