package agentspring.agent;

import agentspring.role.Role;

public interface Agent {

    public String getLabel();

    public void setLabel(String label);

    public void act(Role<? extends Agent> role);

}
