package kidnox.eventbus.impl;

import kidnox.eventbus.elements.EventSubscriber;

import java.util.List;

public class PackageLocalProvider {

    public static List<EventSubscriber> getSubscribers(BusImpl bus, Object target) {
        return bus.instanceToSubscribersMap.get(target);
    }

/*    public static Map<Class, ClassType> getClassToTypeMap(ClassInfoExtractor classInfoExtractor) {
        return ((ClassInfoExtractorImpl)classInfoExtractor).classToTypeMap;
    }

    public static Map<Class, ClassSubscribers> getSubscibersCache(ClassInfoExtractor classInfoExtractor) {
        return ((ClassInfoExtractorImpl)classInfoExtractor).subscribersCache;
    }

    public static Map<Class, ClassProducers> getProducersCache(ClassInfoExtractor classInfoExtractor) {
        return ((ClassInfoExtractorImpl)classInfoExtractor).producersCache;
    }

    public static Map<String, EventDispatcher> getDispatchersMap(ClassInfoExtractor classInfoExtractor) {
        return ((ClassInfoExtractorImpl)classInfoExtractor).dispatchersMap;
    }

    public static EventDispatcher getDispatcher(String name, ClassInfoExtractor classInfoExtractor) {
        return ((ClassInfoExtractorImpl)classInfoExtractor).getDispatcher(name);
    }*/

}
