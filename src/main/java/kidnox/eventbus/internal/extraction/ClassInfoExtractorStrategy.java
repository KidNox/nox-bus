package kidnox.eventbus.internal.extraction;

import kidnox.eventbus.*;
import kidnox.eventbus.internal.*;
import kidnox.eventbus.internal.element.ElementInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;

import static kidnox.eventbus.internal.Utils.*;
import static kidnox.eventbus.internal.extraction.ExtractionUtils.*;

@SuppressWarnings("unchecked")
interface ClassInfoExtractorStrategy<T extends Annotation> {//TODO

    ClassInfo extract(Class clazz, T annotation);


    ClassInfoExtractorStrategy<Subscriber> SUBSCRIBER = new InheritanceSupportedStrategy<Subscriber>
            (ClassType.SUBSCRIBER, OnRegister.class, OnUnregister.class, Subscribe.class, Handle.class) {

        @Override String getAnnotationValue(Subscriber annotation) {
            return annotation.value();
        }

        @Override boolean check(Class current, Class child, String childAnnotationValue) {
            Subscriber subscriber = (Subscriber) current.getAnnotation(Subscriber.class);
            if(subscriber == null) return false;
            checkAnnotationValue(current, subscriber.value(), childAnnotationValue, child);
            return true;
        }
    };

    ClassInfoExtractorStrategy<Producer> PRODUCER = new InheritanceSupportedStrategy<Producer>
            (ClassType.PRODUCER, OnRegister.class, OnUnregister.class, Produce.class) {

        @Override String getAnnotationValue(Producer annotation) {
            return annotation.value();
        }

        @Override boolean check(Class current, Class child, String childAnnotationValue) {
            Producer producer = (Producer) current.getAnnotation(Producer.class);
            if (producer == null) return false;
            checkAnnotationValue(current, producer.value(), childAnnotationValue, child);
            return true;
        }
    };

    ClassInfoExtractorStrategy<EventTask> TASK = new InheritanceNotSupportedStrategy<EventTask>
            (ClassType.TASK, OnRegister.class, OnUnregister.class, Execute.class, Schedule.class) {

        @Override String getAnnotationValue(EventTask annotation) {
            return annotation.value();
        }
    };

    static abstract class InheritanceSupportedStrategy<T extends Annotation> extends AbstractStrategy<T> {

        InheritanceSupportedStrategy(ClassType type, Class<? extends Annotation>... args) {
            super(type, args);
        }

        @Override public ClassInfo extract(Class clazz, T annotation) {
            Map<Class, ElementInfo> elementsInfoMap = null;
            final String value = getAnnotationValue(annotation);

            for(Class mClass = clazz; check(mClass, clazz, value); mClass = mClass.getSuperclass()) {
                for (Method method : mClass.getDeclaredMethods()) {
                    if ((method.getModifiers() & Modifier.PUBLIC) == 0)// ignore all not public methods
                        continue;
                    Annotation[] annotations = method.getDeclaredAnnotations();
                    if (isNullOrEmpty(annotations)) continue;
                    if(method.isBridge()) continue; //java 8 annotated bridge methods fix

                    for (Annotation mAnnotation : annotations) {
                        ElementExtractionStrategy strategy = getElementStrategy(mAnnotation, type, clazz);
                        if (strategy == null) continue;
                        if (elementsInfoMap == null) elementsInfoMap = newHashMap(8);

                        ElementInfo element = strategy.extract(method, clazz);
                        ElementInfo oldEntry = elementsInfoMap.put(element.eventType, element);
                        if (oldEntry != null) {
                            //overridden method check
                            if (oldEntry.method.getName().equals(element.method.getName())) {
                                elementsInfoMap.put(element.eventType, element);
                            } else {
                                throwMultiplyMethodsException(clazz, element.eventType, type.toString().toLowerCase());
                            }
                        }
                        break;//we find strategy for annotation and extract element so can break
                    }
                }
            }
            return createInfo(clazz, type, value, elementsInfoMap);
        }

        abstract String getAnnotationValue(T annotation);

        abstract boolean check(Class current, Class child, String childAnnotationValue);
    }

    static abstract class InheritanceNotSupportedStrategy<T extends Annotation> extends AbstractStrategy<T> {
        InheritanceNotSupportedStrategy(ClassType type, Class<? extends Annotation>... args) {
            super(type, args);
        }

        @Override public ClassInfo extract(Class clazz, T annotation) {
            Map<Class, ElementInfo> elementsInfoMap = null;
            final String value = getAnnotationValue(annotation);

            for(Method method : clazz.getDeclaredMethods()) {
                if ((method.getModifiers() & Modifier.PUBLIC) == 0)// ignore all not public methods
                    continue;
                Annotation[] annotations = method.getDeclaredAnnotations();
                if (isNullOrEmpty(annotations)) continue;
                if(method.isBridge()) continue; //java 8 annotated bridge methods fix

                for (Annotation mAnnotation : annotations) {
                    ElementExtractionStrategy strategy = getElementStrategy(mAnnotation, type, clazz);
                    if (strategy == null) continue;
                    if (elementsInfoMap == null) elementsInfoMap = newHashMap(8);

                    ElementInfo element = strategy.extract(method, clazz);
                    elementsInfoMap.put(element.eventType, element);
                    break;
                }
            }
            return createInfo(clazz, type, value, elementsInfoMap);
        }

        abstract String getAnnotationValue(T annotation);
    }

    static abstract class AbstractStrategy<T extends Annotation> implements ClassInfoExtractorStrategy<T> {
        final ClassType type;
        final Map<Class<? extends Annotation>, ElementExtractionStrategy> strategyMap;

        //allowed annotations for getElementStrategy
        AbstractStrategy(ClassType type, Class<? extends Annotation>... args) {
            this.type = type;
            this.strategyMap = getElementStrategiesFor(args);
        }

        ElementExtractionStrategy getElementStrategy(Annotation elementAnnotation, ClassType classType, Class clazz) {
            Class<? extends Annotation> elementType = elementAnnotation.annotationType();
            ElementExtractionStrategy strategy = strategyMap.get(elementType);
            if(strategy == null && ELEMENT_STRATEGIES.containsKey(elementType)) {
                throwAnnotationNotAllowedHere(clazz, classType, elementType);
            }
            return strategy;
        }

        ClassInfo createInfo(Class clazz, ClassType type, String annotationValue, Map<Class, ElementInfo> elementsMap) {
            if(elementsMap == null) {
                return new ClassInfo(clazz, type, annotationValue, Collections.<ElementInfo>emptyList());
            } else {
                ElementInfo onRegister = elementsMap.remove(REGISTER_VOID_KEY);
                ElementInfo onRegisterBusType = elementsMap.remove(REGISTER_BUS_KEY);
                if(onRegister == null) {
                    onRegister = onRegisterBusType;
                } else {
                    if(onRegisterBusType != null)
                        throw new BusException("to many @OnRegister methods, can be only one");
                }
                ElementInfo onUnregister = elementsMap.remove(UNREGISTER_VOID_KEY);
                return new ClassInfo(clazz, type, annotationValue, elementsMap.values(), onRegister, onUnregister);
            }
        }
    }
}
