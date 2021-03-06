package agentspring.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import agentspring.agent.AbstractAgent;

@Aspect
public class ExecutionInspector {

    static Logger logger = LoggerFactory.getLogger(ExecutionInspector.class);

    @Pointcut("execution(public void act(..))")
    private void actMethod() {
    }

    @Pointcut("within(*.role..*)")
    private void inRole() {
    }

    @Pointcut("inRole() && actMethod() ")
    private void roleAct() {
    }

    @Around("roleAct()")
    @Transactional
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {

        long start = System.currentTimeMillis();
        String roleName = pjp.getThis().getClass().getSimpleName();
        String printRoleName = roleName.replace("Role", "");
        String agentName = getName(pjp.getArgs()[0]);

        logger.info("============================================================");
        logger.info("START:  {} is going to {}", agentName, printRoleName);
        logger.info("============================================================");

        Object output = pjp.proceed();

        logger.info("============================================================");
        double elapsedTime = (System.currentTimeMillis() - start) / 1000.0;
        logger.info("FINISH: {} finished to {} in " + elapsedTime + "s", agentName, printRoleName);
        logger.info("============================================================");
        // execution.setEnd(System.currentTimeMillis());
        return output;
    }

    private String getName(Object object) {
        String name = "NOBODY";
        if (object != null) {
            AbstractAgent agent = (AbstractAgent) object;
            if (agent.getName() != null) {
                name = agent.getName();
            } else {
                name = object.getClass().getSimpleName();
            }
        }
        return name;
    }

}