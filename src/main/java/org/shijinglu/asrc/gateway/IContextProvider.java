package org.shijinglu.asrc.gateway;


import org.shijinglu.asrc.model.IData;

/**
 * Some contexts are not provided from clients, instead they are provided on servers. For example,
 * Average volatility of a stock, it is a dynamic and server provided. In a remote config server.
 * It should be stored in a distributed cache like Redis.
 *
 * This interface delegates the call to retrieve these server provided contexts.
 */
public interface IContextProvider {

    IData getContext(String key);
}
