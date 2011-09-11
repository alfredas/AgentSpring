package agentspring.role;


import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import agentspring.Agent;

public interface Role<T extends Agent> extends ApplicationContextAware {

    public void act(T agent);

    public Class<T> agentClass();

    public ApplicationContext getApplicationContext();

}
