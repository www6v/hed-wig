package com.yihaodian.architecture.hedwig.engine.handler;

import com.yihaodian.architecture.hedwig.engine.event.IEvent;
import com.yihaodian.architecture.hedwig.engine.event.IEventContext;

public interface IEventHandler<C extends IEventContext, E extends IEvent<T>, T> {

	public T handle(C eventContext, E event) throws Throwable;
}
