package agentspring.trend;


import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.service.SimulationParameter;

@NodeEntity
public abstract class AbstractTrend implements Trend {

	@SimulationParameter(label = "Initial Value")
	private double start;

	public double getStart() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
