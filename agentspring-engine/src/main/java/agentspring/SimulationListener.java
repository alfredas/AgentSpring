package agentspring;

import agentspring.facade.EngineEvent;

public interface SimulationListener {
    public void act(EngineEvent event);
}
