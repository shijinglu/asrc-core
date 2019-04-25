package org.shijinglu.asrc.core;

import java.util.HashMap;

/** Map based event */
public class SimpleEvent extends HashMap<String, String> implements IEvent {
    public static SimpleEvent of(String key, String val) {
        SimpleEvent e = new SimpleEvent();
        e.put(key, val);
        return e;
    }
}
