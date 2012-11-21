package agentspring.lod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface LODType {
    String query() default "";

    String endpoint() default "";

    String namespace() default "";

    String type() default "";

    String limit() default "";

    String[] filters() default {};

    String id() default "";
}
