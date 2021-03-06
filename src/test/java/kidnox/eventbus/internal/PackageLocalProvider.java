package kidnox.eventbus.internal;

import kidnox.eventbus.internal.element.AsyncElement;

import java.util.Set;

public final class PackageLocalProvider {

    public static Set<AsyncElement> getSubscribers(AsyncBus bus, Class eventType) {
        return bus.eventTypeToSubscribersMap.get(eventType);
    }

}
