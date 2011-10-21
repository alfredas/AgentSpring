package agentspring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.neo4j.core.NodeBacked;
import org.springframework.transaction.annotation.Transactional;

public class PersistingBeanPostProcessor implements BeanPostProcessor {

    static Logger logger = LoggerFactory.getLogger(PersistingBeanPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    @Transactional
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof NodeBacked) {
            logger.info("Persisting bean {} with name {}", bean, beanName);
            ((NodeBacked) bean).persist();
        }
        return bean;
    }

}
