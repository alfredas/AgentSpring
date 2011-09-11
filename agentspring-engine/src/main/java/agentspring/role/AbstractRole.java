package agentspring.role;

import java.lang.reflect.ParameterizedType;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import agentspring.Schedule;

public abstract class AbstractRole<T> {

    public Logger logger = LoggerFactory.getLogger(AbstractRole.class);

    ApplicationContext applicationContext;

    public long getCurrentTick() {
        return Schedule.getSchedule().getCurrentTick();
    }

    @SuppressWarnings("unchecked")
    public Class<T> agentClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
