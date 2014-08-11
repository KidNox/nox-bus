package kidnox.eventbus.impl;

import kidnox.eventbus.Bus;
import kidnox.eventbus.EventDispatcher;
import kidnox.eventbus.elements.EventProducer;
import kidnox.eventbus.elements.EventSubscriber;

public abstract class AsyncEventDispatcher implements EventDispatcher {

    protected abstract void dispatch(Runnable runnable);

    @Override public void dispatchSubscribe(EventSubscriber subscriber, Object event) {
        if(inCurrentThread()) {
            subscriber.invoke(event);
        } else {
            dispatch(getRunnableSubscription(subscriber, event));
        }
    }

    @Override public void dispatchProduce(EventProducer eventProducer, EventSubscriber eventSubscriber) {
        //TODO
        eventSubscriber.receive(eventProducer.invoke(null));
    }

    public boolean inCurrentThread(){
        return false;
    }

    static Runnable getRunnableSubscription(final EventSubscriber subscriber, final Object event) {
        return new Runnable() {
            @Override
            public void run() {
                subscriber.invoke(event);
            }
        };
    }
    //TODO
    static Runnable getRunnableProduction(final EventProducer eventProducer, final EventSubscriber eventSubscriber) {
        return new Runnable() {
            @Override public void run() {
                eventSubscriber.receive(eventProducer.invoke(null));
            }
        };
    }

    static Runnable getRunnableRegistration(final Bus bus, final Object target) {
        return new Runnable() {
            @Override public void run() {
                bus.register(target);
            }
        };
    }

    static Runnable getRunnableUnregistration(final Bus bus, final Object target) {
        return new Runnable() {
            @Override public void run() {
                bus.unregister(target);
            }
        };
    }

}