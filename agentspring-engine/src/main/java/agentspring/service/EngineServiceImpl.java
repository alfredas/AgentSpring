package agentspring.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.neo4j.core.NodeBacked;
import org.springframework.data.neo4j.fieldaccess.NodeDelegatingFieldAccessorFactory;
import org.springframework.data.neo4j.support.GraphDatabaseContext;
import org.springframework.data.neo4j.support.node.NodeEntityStateFactory;

import agentspring.Simulation;
import agentspring.SimulationListener;
import agentspring.facade.ConfigurableObject;
import agentspring.facade.EngineEvent;
import agentspring.facade.EngineService;
import agentspring.facade.EngineState;
import agentspring.facade.ScenarioParameter;

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

public class EngineServiceImpl implements EngineService, ApplicationContextAware {

    @Autowired
    private Simulation simulation;

    @Autowired
    private GraphDatabaseContext graphDatabaseContext;

    ApplicationContext applicationContext;

    private NodeEntityStateFactory entityStateFactory = new NodeEntityStateFactory();

    private static final Logger logger = LoggerFactory.getLogger(EngineServiceImpl.class);

    private LinkedList<LoggingEvent> log = new LinkedList<LoggingEvent>();
    private LinkedList<EngineEvent> events = new LinkedList<EngineEvent>();
    private Neo4jGraph graph;
    private Object waiter = new Object();
    private ClassPathXmlApplicationContext context;
    private List<String> scenarios;
    private String currentScenario;
    private boolean scenarioLoaded = false;
    private HashMap<String, ConfigurableObject> scenarioParameters = new HashMap<String, ConfigurableObject>();

    @SuppressWarnings("unchecked")
    public void init() throws EngineException {
        try {
            scenarios = (List<String>) applicationContext.getBean("scenarios");
        } catch (NoSuchBeanDefinitionException err) {
            throw new EngineException("Scenarios not defined. Please define scenarios in a bean 'scenarios' of type List<String>");
        } catch (BeansException err) {
            throw new EngineException("Scenarios not defined. Please define scenarios in a bean 'scenarios' of type List<String>");
        }

        if (scenarios != null && scenarios.size() > 0) {
            init(scenarios.get(0));
        } else {
            throw new EngineException("Scenarios not defined. Please define scenarios in a bean 'scenarios' of type List<String>");
        }
    }

    public void init(String scenario) throws EngineException {
        org.apache.log4j.Logger.getRootLogger().addAppender(new LogAppender());
        currentScenario = scenario;
        if (scenarios == null) {
            scenarios = new ArrayList<String>();
        }
        if (scenarios.indexOf(scenario) < 0) {
            scenarios.add(scenario);
        }
        this.simulation.listen(new SimulationListenerImpl());
        this.graph = new Neo4jGraph(this.graphDatabaseContext.getGraphDatabaseService());
        this.entityStateFactory = new NodeEntityStateFactory();
        this.entityStateFactory.setGraphDatabaseContext(this.graphDatabaseContext);
        this.entityStateFactory.setEntityManagerFactory(null);
        this.entityStateFactory.setNodeDelegatingFieldAccessorFactory(new NodeDelegatingFieldAccessorFactory(this.graphDatabaseContext));
        this.loadScenario(this.currentScenario);
    }

    @Override
    public synchronized void start() {
        if (this.simulation.getState() != EngineState.STOPPED && this.simulation.getState() != EngineState.CRASHED)
            return;
        if (!this.scenarioLoaded) {
            this.loadScenario(this.currentScenario);
        }
        this.updateScenarioParameters();
        simulation.runSimulation();
        this.scenarioLoaded = false;
        logger.info("Starting new simulation");
    }

    @Override
    public synchronized void stop() {
        simulation.stopSimulation();
    }

    @Override
    public synchronized void pause() {
        simulation.pauseSimulation();
    }

    @Override
    public synchronized void resume() {
        simulation.resumeSimulation();
    }

    @Override
    public EngineEvent listen() {
        if (this.events.isEmpty()) {
            synchronized (this.waiter) {
                try {
                    this.waiter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (this.events) {
            return this.events.pop();
        }
    }

    @Override
    public String popLog() {
        if (!this.log.isEmpty()) {
            synchronized (this.log) {
                return this.log.pop().getMessage().toString();
            }
        } else {
            return null;
        }
    }

    @Override
    public long getCurrentTick() {
        return this.simulation.getCurrentTick();
    }

    /**
     * Flush accumulated events
     */
    @Override
    public synchronized void flush() {
        synchronized (this.log) {
            this.log.clear();
        }
        synchronized (this.events) {
            this.events.clear();
        }
    }

    @Override
    public EngineState getState() {
        return this.simulation.getState();
    }

    @Override
    public synchronized void wake() {
        this.simulation.wake();
    }

    @Override
    public synchronized void release() {
        synchronized (this.events) {
            this.events.add(EngineEvent.RELEASE);
        }
        synchronized (this.waiter) {
            this.waiter.notifyAll();
        }
    }

    private class LogAppender extends AppenderSkeleton {

        @Override
        public boolean requiresLayout() {
            return false;
        }

        @Override
        public void close() {
        }

        @Override
        protected void append(LoggingEvent event) {
            synchronized (log) {
                log.add(event);
            }
            synchronized (events) {
                events.add(EngineEvent.LOG_MESSAGE);
            }
            synchronized (waiter) {
                waiter.notifyAll();
            }
        }
    }

    private class SimulationListenerImpl implements SimulationListener {
        @Override
        public void act(EngineEvent event) {
            synchronized (events) {
                events.add(event);
            }
            synchronized (waiter) {
                waiter.notifyAll();
            }
        }
    }

    @Override
    public String getCurrentScenario() {
        return this.currentScenario;
    }

    @Override
    public Map<String, ConfigurableObject> getScenarioParameters() {
        return this.scenarioParameters;
    }

    @Override
    public String[] getScenarios() {
        return this.scenarios.toArray(new String[0]);
    }

    @Override
    public synchronized void loadScenario(String scenario) {
        if (this.simulation.getState() != EngineState.STOPPED && this.simulation.getState() != EngineState.CRASHED)
            return;

        boolean found = false;
        for (String scen : this.scenarios) {
            if (scenario.equals(scen)) {
                found = true;
                break;
            }
        }
        if (!found)
            throw new RuntimeException("Scenario " + scenario + " does not exist");

        if (this.context != null) {
            this.context.close();
        }
        try {
            this.graph.clear();
        } catch (Exception err) {
            logger.error("Error clearing graph", err);
        }
        this.context = new ClassPathXmlApplicationContext(scenario);

        if (!this.currentScenario.equals(scenario) || this.scenarioParameters.isEmpty()) {
            this.scenarioParameters.clear();

            String[] names = this.context.getBeanNamesForType(Object.class);
            for (String beanName : names) {
                Object bean = this.context.getBean(beanName);
                ArrayList<java.lang.reflect.Field> fields = new ArrayList<java.lang.reflect.Field>();
                Class<?> clazz = bean.getClass();
                do {
                    for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                        fields.add(field);
                    }
                    clazz = clazz.getSuperclass();
                } while (clazz != null);
                clazz = bean.getClass();
                for (java.lang.reflect.Field field : fields) {
                    String name = field.getName();
                    SimulationParameter annotation = field.getAnnotation(SimulationParameter.class);
                    if (annotation != null) {
                        Object value = null;
                        try {
                            Method getter;
                            Class<?> fieldType = field.getType();
                            if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                                getter = bean.getClass().getMethod("is" + this.capitalize(name));
                            } else {
                                getter = bean.getClass().getMethod("get" + this.capitalize(name));
                            }
                            value = getter.invoke(bean);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        ConfigurableObject cfgObj = this.scenarioParameters.get(beanName);
                        if (cfgObj == null) {
                            cfgObj = new ConfigurableObject(beanName, clazz.getName());
                            this.scenarioParameters.put(beanName, cfgObj);
                        }
                        ScenarioParameter parameter = new ScenarioParameter(name, value, annotation.label());
                        if (!Double.isNaN(annotation.from()) && !Double.isNaN(annotation.to())) {
                            parameter.setFrom(annotation.from());
                            parameter.setTo(annotation.to());
                        }
                        if (!Double.isNaN(annotation.step())) {
                            parameter.setStep(annotation.step());
                        }
                        cfgObj.addParameter(parameter);
                    }
                }
            }
        }

        this.currentScenario = scenario;
        this.scenarioLoaded = true;
    }

    @Override
    public synchronized void setScenarioParameters(Map<String, Map<String, ScenarioParameter>> parameters) {
        // perform sanity check first to avoid exploitation by hackers
        for (String obj : parameters.keySet()) {
            if (!this.scenarioParameters.containsKey(obj)) {
                throw new RuntimeException("Object with id '" + obj + "' can not be configured");
            }
            ConfigurableObject oldObj = this.scenarioParameters.get(obj);
            Map<String, ScenarioParameter> newParams = parameters.get(obj);
            for (String field : newParams.keySet()) {
                if (!oldObj.containsParam(field)) {
                    throw new RuntimeException("'" + obj + "'" + " has no configurable field '" + field + "'");
                }
                ScenarioParameter newParam = newParams.get(field);
                oldObj.setParamValue(field, newParam.getValue());
            }
        }
        this.updateScenarioParameters();
    }

    // HACK: reflection
    private void updateScenarioParameters() {
        for (ConfigurableObject configObj : this.scenarioParameters.values()) {
            String beanName = configObj.getId();
            Object bean = this.context.getBean(beanName);
            Class<?> clazz = bean.getClass();
            HashMap<String, java.lang.reflect.Field> declaredFields = new HashMap<String, java.lang.reflect.Field>();
            do {
                for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                    declaredFields.put(field.getName(), field);
                }
                clazz = clazz.getSuperclass();
            } while (clazz != null);
            clazz = bean.getClass();
            for (ScenarioParameter parameter : configObj.getParameters()) {
                String field = this.capitalize(parameter.getField());
                Object value = parameter.getValue();
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (method.getName().equals("set" + field)) {
                        try {
                            method.invoke(bean, value);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        ((NodeBacked) bean).persist();
                        break;
                    }
                }
            }
        }
    }

    private String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1, string.length());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
