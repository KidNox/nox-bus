package kidnox.eventbus;

import kidnox.common.Factory;
import kidnox.eventbus.async.AsyncDispatcherExt;
import kidnox.eventbus.async.AsyncDispatcherFactory;
import kidnox.eventbus.async.PackageLocalProvider;
import kidnox.eventbus.async.SingleThreadWorker;
import kidnox.eventbus.impl.AsyncDispatcher;
import kidnox.eventbus.internal.AbsAsyncSubscriber;
import kidnox.eventbus.internal.AsyncDispatchersFactory;
import kidnox.eventbus.internal.NamedAsyncDispatcher;
import org.junit.Test;

import static org.junit.Assert.*;

public class AsyncTest {

    Bus bus;

    @Test public void baseAsyncTest() throws InterruptedException {
        AsyncDispatchersFactory factory = new AsyncDispatchersFactory();
        bus = BusFactory.builder().withDispatcherFactory(factory).create();

        final NamedAsyncDispatcher dispatcher1 = new NamedAsyncDispatcher("worker-1");
        final NamedAsyncDispatcher dispatcher2 = new NamedAsyncDispatcher("worker-2");
        final NamedAsyncDispatcher dispatcher3 = new NamedAsyncDispatcher("worker-3");

        factory.addDispatcher(dispatcher1);
        factory.addDispatcher(dispatcher2);
        factory.addDispatcher(dispatcher3);

        @Subscriber("worker-1")
        class Worker1 extends AbsAsyncSubscriber {
            @Subscribe public void obtainEvent(Object event) {
                currentEvent = event;
                checkThread(dispatcher1.getThread());

                dispatcher1.getWorker().dismiss(true);
            }
        }

        @Subscriber("worker-2")
        class Worker2 extends AbsAsyncSubscriber {
            @Subscribe public void obtainEvent(Object event) {
                currentEvent = event;
                checkThread(dispatcher2.getThread());

                dispatcher2.getWorker().dismiss(true);
            }
        }

        @Subscriber("worker-3")
        class Worker3 extends AbsAsyncSubscriber {
            @Subscribe public void obtainEvent(Object event) {
                currentEvent = event;
                checkThread(dispatcher3.getThread());

                dispatcher3.getWorker().dismiss(true);
            }
        }

        AbsAsyncSubscriber subscriber1 = new Worker1();
        AbsAsyncSubscriber subscriber2 = new Worker2();
        AbsAsyncSubscriber subscriber3 = new Worker3();

        bus.register(subscriber1);
        bus.register(subscriber2);
        bus.register(subscriber3);

        Object event = new Object();
        bus.post(event);

        dispatcher1.getThread().join();
        dispatcher2.getThread().join();
        dispatcher3.getThread().join();

        assertEquals("wrong event", event, subscriber1.getCurrentEvent());
        assertEquals("wrong event", event, subscriber2.getCurrentEvent());
        assertEquals("wrong event", event, subscriber3.getCurrentEvent());
    }

    @Test public void asyncPost() throws InterruptedException {
        final Bus bus = BusFactory.getDefault();
        final NamedAsyncDispatcher dispatcher1 = new NamedAsyncDispatcher("worker-1");

        @Subscriber
        class SubscriberClass extends AbsAsyncSubscriber {
            @Subscribe public void obtainEvent(Object event) {
                currentEvent = event;
                checkThread(dispatcher1.getThread());

                dispatcher1.getWorker().dismiss(true);
            }
        }

        final SubscriberClass subscriberClass = new SubscriberClass();
        bus.register(subscriberClass);

        dispatcher1.getWorker().execute(new Runnable() {
            @Override
            public void run() {
                bus.post(new Object());
            }
        });

        dispatcher1.getThread().join();

        assertNotNull("wrong event", subscriberClass.getCurrentEvent());
    }

    @Test public void testAsyncDispatcherFactory() throws InterruptedException {
        final Factory<Dispatcher, String> factory = new AsyncDispatcherFactory()
                .addDispatcher(Dispatcher.WORKER, AsyncDispatcherFactory.getWorkerDispatcher());
        final Bus bus = BusFactory.builder()
                .withDispatcherFactory(factory)
                .create();

        final AsyncDispatcherExt dispatcher = (AsyncDispatcherExt) factory.get(Dispatcher.WORKER);
        final SingleThreadWorker worker = PackageLocalProvider.getSingleThreadWorker(dispatcher);

        @Subscriber(Dispatcher.WORKER)
        class SubscriberClass extends AbsAsyncSubscriber {
            @Subscribe public void obtainEvent(Object event) {
                currentEvent = event;
                assertTrue(PackageLocalProvider.inCurrentThread(dispatcher));
                worker.dismiss(true);
            }
        }

        SubscriberClass subscriberClass = new SubscriberClass();
        bus.register(subscriberClass);
        bus.post(new Object());

        worker.getWorkerThread().join();

        assertNotNull(subscriberClass.getCurrentEvent());

    }

    private void checkThread(Thread expected) {
        assertEquals("wrong thread", expected, Thread.currentThread());
    }

}
