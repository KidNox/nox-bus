package kidnox.eventbus.internal;

import kidnox.eventbus.*;
import kidnox.eventbus.impl.ClassInfoExtractorImpl;
import kidnox.eventbus.impl.ClassInfoExtractorValidation;

public final class InternalFactory {

    public static ClassInfoExtractor createClassInfoExtractor(boolean extraValidation) {
        if(extraValidation) return new ClassInfoExtractorValidation();
        else                return new ClassInfoExtractorImpl();
    }

    public static final EventDispatcher CURRENT_THREAD_DISPATCHER = new EventDispatcher() {

        @Override public boolean isDispatcherThread() {
            return true;
        }

        @Override public void dispatch(Runnable event) { }
    };

    public static EventDispatcher.Factory getDefaultDispatcherFactory() {
        return new EventDispatcher.Factory() {
            @Override public EventDispatcher getDispatcher(String name) {
                if(name.isEmpty()) {
                    return InternalFactory.CURRENT_THREAD_DISPATCHER;
                } else {
                    throw new IllegalArgumentException("Dispatcher ["+name+"] not found");
                }
            }
        };
    }

    public static ExceptionHandler getStubExHandler() {
        return new ExceptionHandler() {
            @Override public boolean handle(Throwable thr, Object target, Object event) {
                return false;
            }
        };
    }

    public static DeadEventHandler getStubDeadEvHandler() {
        return new DeadEventHandler() {
            @Override public void onDeadEvent(Object event) { }
        };
    }

    public static EventLogger getStubLogger() {
        return new EventLogger() {
            @Override public void logEvent(Object event, Object target, String what) { }
        };
    }

    public static Interceptor getStubInterceptor() {
        return new Interceptor() {
            @Override public boolean intercept(Object event) {
                return false;
            }
        };
    }


    private InternalFactory() {}
}
