package agentspring.engine;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.graph.annotation.NodeEntity;

import agentspring.engine.role.Role;

@NodeEntity
public abstract class AbstractAgent implements Agent {

    static Logger logger = LoggerFactory.getLogger(AbstractAgent.class);

    private String label;

    public AbstractAgent() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @SuppressWarnings("unchecked")
    public void act(Role role) {
        role.act(this);
    }

    @Override
    public String toString() {
        if (getLabel() != null) {
            return getLabel();
        } else {
            return super.toString();
        }
    }

}
