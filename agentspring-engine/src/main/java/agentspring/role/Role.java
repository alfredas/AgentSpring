package agentspring.role;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import agentspring.agent.Agent;
/**
 * Role encapsulates agent's behavior. 
 * Roles are modular pieces of behavior that can be chained and combined to produce more sophisticated behaviors.
 * @author alfredas
 *
 * @param <T>
 */
public interface Role<T extends Agent> extends ApplicationContextAware {

    public void act(T agent);

    public Class<T> agentClass();

    public ApplicationContext getApplicationContext();

}
