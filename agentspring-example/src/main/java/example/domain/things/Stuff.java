package example.domain.things;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.service.SimulationParameter;

@NodeEntity
public class Stuff {

    @Indexed
    double price;
    @Indexed
    String label;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
