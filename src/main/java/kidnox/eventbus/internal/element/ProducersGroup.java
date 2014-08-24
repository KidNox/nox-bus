package kidnox.eventbus.internal.element;

import kidnox.eventbus.internal.ElementsGroup;

import java.util.List;
import java.util.Map;

public final class ProducersGroup implements ElementsGroup {

    final List<AsyncElement> producers;
    final Map<Class, AsyncElement> mapReference;

    public ProducersGroup(List<AsyncElement> producers, Map<Class, AsyncElement> mapReference) {
        this.producers = producers;
        this.mapReference = mapReference;
    }

    @Override public void unregisterGroup() {
        for(AsyncElement producer : producers) {
            mapReference.remove(producer.eventType);
            producer.onUnregister();
        }
    }
}
