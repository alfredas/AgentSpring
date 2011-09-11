package agentspring.trend;


import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.service.SimulationParameter;

@NodeEntity
public class LinearTrend extends AbstractTrend implements Trend {

    @SimulationParameter(label="Increment per time step")
    private double increment;

    public double getValue(long time) {
        return ((double) time * increment) + getStart();
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }

}
