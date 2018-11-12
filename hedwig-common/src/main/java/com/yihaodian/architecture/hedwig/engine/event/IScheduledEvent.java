package com.yihaodian.architecture.hedwig.engine.event;

import java.util.concurrent.TimeUnit;

public interface IScheduledEvent<T> extends IEvent<T> {

	public long getDelay();

	public TimeUnit getDelayUnit();
}
