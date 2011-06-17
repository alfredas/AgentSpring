package agentspring.engine;

import org.springframework.context.ApplicationContextAware;

public interface Simulation extends ApplicationContextAware {

    public void runSimulation();

    public void pauseSimulation();

    public void stopSimulation();

    public void resumeSimulation();

    public boolean isRunning();

    public long getCurrentTick();

}
