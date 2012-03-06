package agentspring.simulation;

import org.springframework.context.ApplicationContextAware;

import agentspring.facade.EngineState;

/**
 * Simulation interface. Describes methods to start/stop/pause the simulation
 * 
 * @author alfredas
 * 
 */
public interface Simulation extends ApplicationContextAware {

    public void runSimulation();

    public void pauseSimulation();

    public void stopSimulation();

    public void resumeSimulation();

    public EngineState getState();

    public long getCurrentTick();

    public void wake();

    public void listen(SimulationListener listener);

}
