package kidnox.eventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can contain {@link OnRegister}, {@link OnUnregister}, {@link Subscribe} and {@link Handle} elements.
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Subscriber {

    /**
     * Name of the dispatcher for dispatchers factory
     * */
    String value() default "";
}
