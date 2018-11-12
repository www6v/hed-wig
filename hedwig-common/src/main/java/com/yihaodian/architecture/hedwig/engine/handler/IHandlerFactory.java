package com.yihaodian.architecture.hedwig.engine.handler;

import com.yihaodian.architecture.hedwig.engine.event.IEvent;
import com.yihaodian.architecture.hedwig.engine.event.IEventContext;

public interface IHandlerFactory<C extends IEventContext, E extends IEvent<T>, T> {

	public IEventHandler<C, E, T> create(E event);
}
