package com.yihaodian.architecture.hedwig.engine.event;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInvocation;

public interface IEvent<T> extends Serializable {

	public boolean isRetryable();

	public int getExecCount();

	public long getExpireTime();

	public TimeUnit getExpireTimeUnit();

	public void increaseExecCount();

	public T getResult();
	
	public void setResult(T result);

	public MethodInvocation getInvocation();

	public EventState getState();

	public void setState(EventState state);

	public void setErrorMessage(String errorMessage);

	public String getReqestId();

	public long getStart();
}
