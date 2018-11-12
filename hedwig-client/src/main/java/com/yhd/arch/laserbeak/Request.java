package com.yhd.arch.laserbeak;

/**
 * @author root
 *
 * @param <C>
 */
public interface Request<C> {

	public C getContext();

	public String getMethodName();

	public Object[] getArguments();
}
