package agentspring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import agentspring.service.EngineServiceImpl;

/**
 * Simulation service runner. This is the class that initiates the application
 * context and wires the engine and the model together.
 * 
 * @author alfredas
 * 
 */
public class Service {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    public static void main(String args[]) {
        try {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("settings.xml", "engineContext.xml",
                    "serviceContext.xml");

            EngineServiceImpl engine = context.getBean(EngineServiceImpl.class);

            engine.init();
            // enable security manager for gremlin script sandboxing
            System.setSecurityManager(new SecurityManager());
            // wait indefinitely
            Service service = new Service();

            logger.warn("Open up your browser");
            synchronized (service) {
                while (true) {
                    try {
                        service.wait();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (EngineException err) {
            System.out.println("ERROR: " + err.getMessage());
            logger.error(err.getMessage());
        }
    }
}
