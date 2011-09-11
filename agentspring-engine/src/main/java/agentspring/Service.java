package agentspring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import agentspring.service.EngineException;
import agentspring.service.EngineServiceImpl;

public class Service {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    public static void main(String args[]) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("engineContext.xml",
                "serviceContext.xml");
        EngineServiceImpl engine = context.getBean(EngineServiceImpl.class);

        try {
            engine.init();
            // enable security manager for gremlin script sandboxing
            System.setSecurityManager(new SecurityManager());
            // wait indefinitely
            Service service = new Service();

            logger.warn("RMI Mode - open up your browser");
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
            logger.error(err.getMessage());
        }
    }
}
