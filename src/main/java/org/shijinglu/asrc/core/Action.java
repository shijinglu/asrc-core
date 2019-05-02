package org.shijinglu.asrc.core;

import static org.shijinglu.asrc.core.Formula.FIELDS.CATEGORY;
import static org.shijinglu.asrc.core.Formula.FIELDS.KEY;
import static org.shijinglu.asrc.core.Formula.FIELDS.RULE;
import static org.shijinglu.asrc.core.Formula.FIELDS.VALUE;

import org.shijinglu.lure.extensions.IData;

/**
 * Protocol that defines an action when an allocation node is visited or a match is found. For
 * example, log_analytics logs an analytical event. log_debug, logs the detailed eval steps at each
 * node. - log_implicit: log current event if current is a leaf - log_debug: log an analytical event
 */
public enum Action {
    NOOP("no_op"),
    LOG_IMPLICIT("log_implicit") {
        @Override
        void run(Formula root, Formula leaf, IData resolved) {
            if (leaf.isLeaf()) {
                EventHandler.acceptEvent(Action.buildEvent(root, leaf, resolved));
            }
            super.run(root, leaf, resolved);
        }
    },
    LOG_DEBUG("log_debug") {
        @Override
        void run(Formula root, Formula leaf, IData resolved) {
            EventHandler.acceptEvent(Action.buildEvent(root, leaf, resolved));
            super.run(root, leaf, resolved);
        }
    };

    /**
     * Name of the action for configuration purpose.
     *
     * @return name of the action.
     */
    private final String name;

    Action(String name) {
        this.name = name;
    }

    void run(Formula root, Formula leaf, IData resolved) {}

    private static IEvent buildEvent(Formula root, Formula leaf, IData resolved) {
        return new SimpleEvent() {
            {
                put(KEY.name(), leaf.key);
                put(VALUE.name(), resolved.toString());
                put(CATEGORY.name(), leaf.category.toString());
                put(RULE.name(), leaf.rule.toString());
            }
        };
    }

    /** Get action enum from its description */
    static Action from(String name) {
        for (Action action : Action.values()) {
            if (action.name.equalsIgnoreCase(name)) {
                return action;
            }
        }
        return NOOP;
    }
}
