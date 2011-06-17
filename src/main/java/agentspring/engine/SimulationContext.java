package agentspring.engine;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SimulationContext extends ClassPathXmlApplicationContext implements ApplicationContext {

    static final String ENGINE_CONTEXT = "engineContext.xml";
    
    public SimulationContext() {
        super(ENGINE_CONTEXT);
    }

    private void setup(String scenarioFile) {
        String[] configLocations = { scenarioFile };
        new ClassPathXmlApplicationContext(configLocations, this);
    }

    public void runSimulation(String scenarioFile) {
        setup(scenarioFile);
        Simulation simulation = getBean(Simulation.class);
        simulation.runSimulation();
    }

}
