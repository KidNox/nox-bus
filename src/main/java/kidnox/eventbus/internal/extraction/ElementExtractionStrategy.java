package kidnox.eventbus.internal.extraction;

import kidnox.eventbus.Bus;
import kidnox.eventbus.internal.element.ElementInfo;
import kidnox.eventbus.internal.element.ElementType;
import kidnox.eventbus.internal.element.ListenerInfo;

import java.lang.reflect.Method;

import static kidnox.eventbus.internal.Utils.*;
import static kidnox.eventbus.internal.extraction.ExtractionUtils.*;

interface ElementExtractionStrategy {

    ElementInfo extract(Method method, Class target);

    ElementExtractionStrategy REGISTER = new ElementExtractionStrategy() {
        @Override public ElementInfo extract(Method method, Class target) {
            if (method.getReturnType() != void.class)
                throwBadMethodException(method, "with @Register must return void type.");
            Class[] params = method.getParameterTypes();
            if (params.length == 0)
                return new ListenerInfo(ElementType.REGISTER, REGISTER_KEY, method, false);
            else if (params.length == 1 && params[0] == Bus.class)
                return new ListenerInfo(ElementType.REGISTER, REGISTER_KEY, method, true);
            else throwBadMethodException(method,
                        "with @Register must require zero arguments or one argument of Bus type.");
            return null;
        }
    };

    ElementExtractionStrategy UNREGISTER = new ElementExtractionStrategy() {
        @Override public ElementInfo extract(Method method, Class target) {
            if (method.getReturnType() != void.class)
                throwBadMethodException(method, "with @Unregister must return void type.");
            Class[] params = method.getParameterTypes();
            if (params.length == 0)
                return new ListenerInfo(ElementType.UNREGISTER, UNREGISTER_VOID_KEY, method, false);
            else if (params.length == 1 && params[0] == Bus.class)
                return new ListenerInfo(ElementType.UNREGISTER, UNREGISTER_VOID_KEY, method, true);
            else throwBadMethodException(method,
                        "with @Unregister must require zero arguments or one argument of Bus type.");
            return null;
        }
    };

    ElementExtractionStrategy SUBSCRIBE = new ElementExtractionStrategy() {
        @Override public ElementInfo extract(Method method, Class target) {
            if (method.getReturnType() != void.class)
                throwBadMethodException(method, "with @Subscribe must return void type. Try @Handle for this case");

            Class[] params = method.getParameterTypes();
            if (params.length != 1)
                throwBadMethodException(method, "with @Subscribe must require a single argument.");

            Class type = params[0];
            if (type.isInterface())
                throwBadMethodException(method, "can't subscribe for interface.");

            return new ElementInfo(ElementType.SUBSCRIBE, type, method);
        }
    };

    ElementExtractionStrategy PRODUCE = new ElementExtractionStrategy() {
        @Override public ElementInfo extract(Method method, Class target) {
            Class returnType = method.getReturnType();
            if (returnType == void.class)
                throwBadMethodException(method, "with @Produce can't return void type.");
            if (method.getParameterTypes().length != 0)
                throwBadMethodException(method, "with @Produce must require zero arguments.");

            if (returnType.isInterface())
                throwBadMethodException(method, "can't produce interface.");

            return new ElementInfo(ElementType.PRODUCE, returnType, method);
        }
    };

    ElementExtractionStrategy HANDLE = new ElementExtractionStrategy() {
        @Override public ElementInfo extract(Method method, Class target) {
            if (method.getReturnType() == void.class)
                throwBadMethodException(method, "with @Handle must return not void type. Try @Subscribe for this case");

            Class[] params = method.getParameterTypes();
            if (params.length != 1)
                throwBadMethodException(method, "with @Handle must require a single argument.");

            Class type = params[0];
            if (type.isInterface())
                throwBadMethodException(method, "can't handle interface.");
            if(method.getReturnType().isInterface())
                throwBadMethodException(method, "can't return interface type");

            return new ElementInfo(ElementType.HANDLE, type, method);
        }
    };

    ElementExtractionStrategy EXECUTE = new ElementExtractionStrategy() {
        @Override public ElementInfo extract(Method method, Class target) {
            if (method.getParameterTypes().length != 0)
                throwBadMethodException(method, "with @Execute must require zero arguments.");
            if(method.getReturnType().isInterface())
                throwBadMethodException(method, "can't return interface type");

            return new ElementInfo(ElementType.EXECUTE, EXECUTE_KEY, method);
        }
    };

}
