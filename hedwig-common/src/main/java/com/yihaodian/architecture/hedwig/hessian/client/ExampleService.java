package com.yihaodian.architecture.hedwig.hessian.client;

/**
 * Hello world!
 *
 */
public interface ExampleService<V, P>
{
	public V execute(P param) throws Throwable;

	public void onwayExecute(P param) throws Throwable;
}
