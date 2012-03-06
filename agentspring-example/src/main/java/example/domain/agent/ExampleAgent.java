package example.domain.agent;

import java.util.Set;

import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import agentspring.agent.AbstractAgent;
import agentspring.agent.Agent;
import agentspring.simulation.SimulationParameter;
import example.domain.things.Stuff;

@NodeEntity
public class ExampleAgent extends AbstractAgent implements Agent {

    @RelatedTo(type = "OWN")
    private Set<Stuff> myStuff;
    @SimulationParameter(label = "Agents Cash Balance", from = 1, to = 100)
    double cash;

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public Set<Stuff> getMyStuff() {
        return myStuff;
    }

    public void setMyStuff(Set<Stuff> myStuff) {
        this.myStuff = myStuff;
    }

}