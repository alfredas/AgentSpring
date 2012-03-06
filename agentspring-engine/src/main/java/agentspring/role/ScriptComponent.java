package agentspring.role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Script annotation. Script is a top-level role that is added to simulation schedule given its parameter settings
 * @author alfredas
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ScriptComponent {

    String name() default "";

    long start() default 0;

    long end() default Long.MAX_VALUE;

    long timeStep() default 1;

    boolean last() default false;

    boolean first() default false;

    String after() default "";

}
