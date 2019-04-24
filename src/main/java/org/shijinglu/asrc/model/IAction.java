package org.shijinglu.asrc.model;

import org.shijinglu.lure.extensions.IData;

/**
 * Protocol that defines an action when an allocation node is visited or a match is found. For
 * example, log_analytics logs an analytical event. log_debug, logs the detailed eval steps at each
 * node.
 */
public interface IAction {
    /**
     * Name of the action for configuration purpose.
     *
     * @return name of the action.
     */
    String getName();

    void run(Formula root, Formula leaf, IData resolved);
}
