package agentspring.simulation;

import agentspring.facade.EngineEvent;

/**
 * Simulation listener. Used by EngineService to act upon EngineEvents that come
 * from the client
 * 
 * @author alfredas
 * 
 */
public interface SimulationListener {
    public void act(EngineEvent event);
}
