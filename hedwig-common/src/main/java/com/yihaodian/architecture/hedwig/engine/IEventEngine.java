/**
 * 
 */
package com.yihaodian.architecture.hedwig.engine;

import java.util.concurrent.Future;

import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.engine.event.IEvent;
import com.yihaodian.architecture.hedwig.engine.event.IEventContext;

/**
 * Event engine is use to invoke event handler in different style such as
 * sync,async,one way, timely scheduler
 * 
 * @author root
 * 
 */
public interface IEventEngine<C extends IEventContext, E extends IEvent<T>, T> {

	/**
	 * Invoke event handler in the caller's thread,can't retry.
	 * 
	 * @param event
	 * @param retry
	 * @return
	 * @throws Exception
	 */
	public T syncInnerThreadExec(C context, final E event) throws HedwigException;

	/**
	 * Invoke event handler in thread pool
	 * 
	 * @param event
	 * @param retry
	 * @return
	 */
	public T syncPoolExec(C context, final E event) throws HedwigException;

	/**
	 * Invoke event handler in thread pool
	 * 
	 * @param event
	 * @param retry
	 * @return
	 */
	public Future<T> asyncExec(C context, final E event) throws HedwigException;

	/**
	 * Reliable asynchronous request executor,base on message server
	 * 
	 * @param event
	 * @throws HedwigException
	 */
	public void asyncReliableExec(C context, final E event) throws HedwigException;
	/**
	 * Invoke event handler at most on time
	 * 
	 * @param event
	 */
	public T oneWayExec(C context, final E event) throws HedwigException;

	/**
	 * Invoke event handler after a specify interval
	 * 
	 * @param event
	 * @param retry
	 */
	public void schedulerExec(C context, final E event) throws HedwigException;

	public Object exec(C context, E event) throws HedwigException;

	public void shutdown();
}
