package agentspring.trend;

import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class GeometricTrend extends AbstractTrend implements Trend {

    private double growthRate;

    public double getValue(long time) {
        return (Math.pow((1 + growthRate), time) * getStart());
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

}
