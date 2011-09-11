package agentspring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.neo4j.core.NodeBacked;
import org.springframework.transaction.annotation.Transactional;

public class PersistingBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    @Transactional
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof NodeBacked) {
            ((NodeBacked) bean).persist();
        }
        return bean;
    }

}
