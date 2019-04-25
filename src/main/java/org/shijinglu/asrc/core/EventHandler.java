package org.shijinglu.asrc.core;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class EventHandler {
    private static final AtomicReference<EventSender> SENDER_HOLDER = new AtomicReference<>();

    @VisibleForTesting
    protected static final ConcurrentLinkedQueue<IEvent> EVENTS_QUEUE =
            new ConcurrentLinkedQueue<>();

    private EventHandler() {}

    /**
     * Set analytics sender and forward previously saved events to the sender immediately
     *
     * @param sender an instance that can write the message to corresponding
     * @return
     */
    public static CompletableFuture setSender(EventSender sender) {
        SENDER_HOLDER.set(sender);
        return acceptEvent(null);
    }

    /**
     * Enqueue the event if logger is not set yet, forward the event to the logger other wise.
     *
     * @param event nullable map that represents a JSON message to be sent. If it is null, it means
     *     sending all saved events in the queue.
     * @return CompletableFuture whose return is the number of events sent.
     */
    public static CompletableFuture acceptEvent(IEvent event) {
        EventSender sender = SENDER_HOLDER.get();
        if (sender == null) {
            if (event != null) {
                EVENTS_QUEUE.offer(event);
            }
            return CompletableFuture.completedFuture(0);
        }
        CompletableFuture[] tasks =
                EVENTS_QUEUE.stream()
                        .map(e -> CompletableFuture.supplyAsync(() -> sender.send(e)))
                        .toArray(CompletableFuture[]::new);
        EVENTS_QUEUE.clear();
        return CompletableFuture.allOf(tasks)
                .thenAccept(
                        v -> {
                            if (event != null) {
                                sender.send(event);
                            }
                        });
    }
}
