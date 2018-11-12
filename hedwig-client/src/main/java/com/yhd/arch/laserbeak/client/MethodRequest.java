/**
 * 
 */
package com.yhd.arch.laserbeak.client;

import com.yhd.arch.laserbeak.Request;

/**
 * @author root
 * @param <C>
 *
 */
public class MethodRequest<C> implements Request<C> {

	private C context;
	private String methodName;
	private Object[] arguments;
	
	public MethodRequest(C context, String methodName, Object[] arguments) {
		super();
		this.context = context;
		this.methodName = methodName;
		this.arguments = arguments;
	}

	@Override
	public C getContext() {
		return context;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public Object[] getArguments() {
		return arguments;
	}

}
