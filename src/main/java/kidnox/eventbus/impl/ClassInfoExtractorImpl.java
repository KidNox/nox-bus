package kidnox.eventbus.impl;

import kidnox.common.Factory;
import kidnox.common.Pair;
import kidnox.eventbus.ClassInfoExtractor;
import kidnox.eventbus.ClassFilter;
import kidnox.eventbus.ClassInfo;
import kidnox.eventbus.Dispatcher;
import kidnox.eventbus.Subscribe;
import kidnox.eventbus.Subscriber;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

final class ClassInfoExtractorImpl implements ClassInfoExtractor {

    final Map<Class, ClassInfo> cache = new HashMap<Class, ClassInfo>();
    final Map<String, Dispatcher> dispatcherMap = new HashMap<String, Dispatcher>();

    final ClassFilter classFilter;
    final Factory<Dispatcher, String> dispatcherFactory;

    ClassInfoExtractorImpl(ClassFilter filter, Factory<Dispatcher, String> factory) {
        this.classFilter = filter == null ? ClassFilter.JAVA : filter;
        this.dispatcherFactory = factory == null ? BusDefaults.createDefaultDispatcherFactory() : factory;
    }

    @SuppressWarnings("unchecked")
    @Override public ClassInfo findClassInfo(Class clazz) {
        ClassInfo classInfo = cache.get(clazz);
        if(classInfo != null) return classInfo;

        List<Pair<Dispatcher, Map<Class, Method>>> dispatchersToTypedMethodList = null;
        for(Class mClass = clazz; !skipClass(mClass); mClass = mClass.getSuperclass()){

            Subscriber subscriberAnnotation = (Subscriber) mClass.getAnnotation(Subscriber.class);

            if(subscriberAnnotation != null){
                final Map<Class, Method> subscribers = getSubscribedMethods(mClass);
                if(subscribers.isEmpty())
                    continue;

                Dispatcher dispatcher = getDispatcher(subscriberAnnotation.value());

                if(dispatchersToTypedMethodList == null)
                    dispatchersToTypedMethodList = new LinkedList<Pair<Dispatcher, Map<Class, Method>>>();

                dispatchersToTypedMethodList.add(new Pair<Dispatcher, Map<Class, Method>>(dispatcher, subscribers));
            }
        }
        classInfo = dispatchersToTypedMethodList == null ?
                ClassInfo.EMPTY : new ClassInfo(dispatchersToTypedMethodList);
        cache.put(clazz, classInfo);

        return classInfo;
    }

    boolean skipClass(Class clazz) {
        return clazz == null || classFilter.skipClass(clazz);
    }

    private Map<Class, Method> getSubscribedMethods(Class targetClass){
        Map<Class, Method> classToMethodMap = null;

        for(Method method : targetClass.getDeclaredMethods()){

            if ((method.getModifiers() & Modifier.PUBLIC) == 0)
                continue;
            if(method.getReturnType() != void.class)
                continue;

            final Class[] params = method.getParameterTypes();
            if(params.length != 1)
                continue;

            if(method.isAnnotationPresent(Subscribe.class)){
                if(classToMethodMap == null)
                    classToMethodMap = new HashMap<Class, Method>();

                final Class clazz = params[0];
                if(clazz.isInterface())
                    throw new IllegalArgumentException("Can't subscribe for interface.");
                classToMethodMap.put(clazz, method);
            }
        }
        return classToMethodMap == null ? Collections.<Class, Method>emptyMap() : classToMethodMap;
    }

    private Dispatcher getDispatcher(String dispatcherName) {
        Dispatcher dispatcher = dispatcherMap.get(dispatcherName);
        if(dispatcher == null){
            dispatcher = dispatcherFactory.get(dispatcherName);
            if(dispatcher == null){
                dispatcher = BusDefaults.DISPATCHER;
            }
            dispatcherMap.put(dispatcherName, dispatcher);
        }
        return dispatcher;
    }

}
