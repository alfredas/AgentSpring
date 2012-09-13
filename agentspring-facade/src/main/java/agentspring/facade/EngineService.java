package agentspring.facade;

import java.util.Map;

public interface EngineService {

    // main engine controls

    public void start();

    public void stop();

    public void pause();

    public void resume();

    // engine info

    public EngineState getState();

    public long getCurrentTick();

    public String getCurrentScenario();

    // Scenario editor

    public String[] getScenarios();

    public void loadScenario(String scenario);

    public Map<String, ConfigurableObject> getScenarioParameters();

    public void setScenarioParameters(Map<String, Map<String, ScenarioParameter>> parameters);

    public void setScenarioParameter(String obj, String field, Object value);

    // helper methods

    /**
     * Engine sleeps after each tick ends. Client has to call wake() after it
     * has collected all needed info for the last tick.
     */
    public void wake();

    /**
     * Listen for engine events. This method returns only when some new event
     * has occured.
     * 
     * @return Pop first event from engine event FIFO stack
     */
    public EngineEvent listen();

    /**
     * Flush engine logs & event stack
     */
    public void flush();

    /**
     * Release listener bound on listen()
     */
    public void release();

    /**
     * @return First log message from engine log FIFO stack
     */
    public String popLog();
}
