package org.shijinglu.asrc.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventHandlerTest {
    private static final SimpleEvent E1 = SimpleEvent.of("one", "Monday");
    private static final SimpleEvent E2 = SimpleEvent.of("two", "Tuesday");
    private static final SimpleEvent E3 = SimpleEvent.of("three", "Wednesday");
    private static final SimpleEvent E4 = SimpleEvent.of("four", "Thursday");
    private static final SimpleEvent E5 = SimpleEvent.of("four", "Friday");

    @Before
    public void setup() {
        EventHandler.setSender(null);
    }

    @Test
    public void enqueueEventsIfLoggerNotSet() throws ExecutionException, InterruptedException {

        EventHandler.acceptEvent(E1).get(); // E1 to queue
        Assert.assertTrue(EventHandler.EVENTS_QUEUE.contains(E1));
        EventHandler.acceptEvent(E2).get(); // E2 to queue
        Assert.assertTrue(EventHandler.EVENTS_QUEUE.contains(E2));

        ConcurrentLinkedQueue<IEvent> sentEvents = new ConcurrentLinkedQueue<>();
        EventSender sender =
                new EventSender() {
                    @Override
                    public boolean send(IEvent event) {
                        return sentEvents.add(event);
                    }
                };
        EventHandler.setSender(sender).get(); // Dequeue E1, E2
        Assert.assertTrue(EventHandler.EVENTS_QUEUE.isEmpty());
        Assert.assertEquals(sentEvents.size(), 2);
        Assert.assertTrue(sentEvents.contains(E1));
        Assert.assertTrue(sentEvents.contains(E2));

        EventHandler.acceptEvent(E3).get(); // E3 to sender
        Assert.assertTrue(sentEvents.contains(E3));

        EventHandler.setSender(null).get(); // no-op
        EventHandler.acceptEvent(E4).get(); // E4 to queue
        Assert.assertFalse(sentEvents.contains(E4));

        EventHandler.setSender(sender).get(); // Dequeue E4
        Assert.assertTrue(sentEvents.contains(E4));
        EventHandler.acceptEvent(E5).get(); // E5 to sender
        Assert.assertTrue(sentEvents.contains(E5));
        Assert.assertEquals(5, sentEvents.size());
    }

    @Test
    public void acdeptLoggerThreadSafety() {
        ConcurrentLinkedQueue<IEvent> sentEvents = new ConcurrentLinkedQueue<>();
        EventSender sender =
                new EventSender() {
                    @Override
                    public boolean send(IEvent event) {
                        return sentEvents.add(event);
                    }
                };

        int parallelism = 1000;
        CompletableFuture[] cfs1 = new CompletableFuture[parallelism];
        CompletableFuture[] cfs2 = new CompletableFuture[parallelism];
        for (int i = 0; i < parallelism; i++) {
            if (i % 11 == 0) {
                cfs2[i] = EventHandler.setSender(sender);
            } else if (i % 11 == 5) {
                cfs2[i] = EventHandler.setSender(null);
            } else {
                cfs2[i] = CompletableFuture.completedFuture(0);
            }
            SimpleEvent event = SimpleEvent.of(String.valueOf(i), String.valueOf(i));
            cfs1[i] = EventHandler.acceptEvent(event);
        }
        CompletableFuture.allOf(
                        CompletableFuture.allOf(cfs1),
                        CompletableFuture.allOf(cfs2),
                        EventHandler.setSender(sender))
                .join();
        Assert.assertEquals(parallelism, sentEvents.size());
    }
}
