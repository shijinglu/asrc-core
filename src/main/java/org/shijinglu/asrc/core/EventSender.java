package org.shijinglu.asrc.core;

/** Sending the event to corresponding pipelines. */
public class EventSender {
    public boolean send(IEvent event) {
        System.out.println(event);
        return true;
    }
}
