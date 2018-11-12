/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.event;

import org.aopalliance.intercept.MethodInvocation;

/**
 * @author root
 * 
 */
public class BroadcastEvent extends BaseEvent {

	public BroadcastEvent(MethodInvocation invocation) {
		super(invocation);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6836966238480427455L;

}
