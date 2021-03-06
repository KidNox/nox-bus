package kidnox.eventbus.internal;

import kidnox.eventbus.Dispatcher;
import kidnox.eventbus.internal.element.Element;
import kidnox.eventbus.internal.element.ListenerInfo;

public class ElementsGroup {

    protected final ClassInfo classInfo;
    protected final Dispatcher dispatcher;

    protected ElementsGroup(ClassInfo classInfo, Dispatcher dispatcher) {
        this.classInfo = classInfo;
        this.dispatcher = dispatcher;
    }

    public void registerGroup(Object target, AsyncBus bus) {
        ListenerInfo listenerInfo = (ListenerInfo) classInfo.elements.get(Utils.REGISTER_KEY);
        if(listenerInfo != null)
            dispatch(new Element(listenerInfo, target), bus, listenerInfo.withBusArgument ? bus : null);
    }

    public void unregisterGroup(Object target, AsyncBus bus) {
        ListenerInfo listenerInfo = (ListenerInfo) classInfo.elements.get(Utils.UNREGISTER_VOID_KEY);
        if(listenerInfo != null)
            dispatch(new Element(listenerInfo, target), bus, listenerInfo.withBusArgument ? bus : null);
    }

    protected void dispatch(final Element element, final AsyncBus bus, final Object param) {
        if(dispatcher.isDispatcherThread()) {
            invokeElement(element, bus, param);
        } else {
            dispatcher.dispatch(new Runnable() {
                @Override public void run() {
                    invokeElement(element, bus, param);
                }
            });
        }
    }

    protected Object invokeElement(Element element, AsyncBus bus, Object param) {
        if(param == null)   return bus.invokeElement(element);
        else                return bus.invokeElement(element, param);
    }

    static final ElementsGroup EMPTY = new ElementsGroup(null, null) {
        @Override public void registerGroup(Object target, AsyncBus asyncBus) { }

        @Override public void unregisterGroup(Object target, AsyncBus bus) { }
    };
}
