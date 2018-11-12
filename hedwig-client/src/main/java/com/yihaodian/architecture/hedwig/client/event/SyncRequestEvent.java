/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.event;

import org.aopalliance.intercept.MethodInvocation;

/**
 * @author root
 *
 */
public class SyncRequestEvent extends BaseEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8464312440807572894L;

	public SyncRequestEvent(MethodInvocation invocation) {
		super(invocation);
	}

	@Override
	public String toString() {
		return "SyncRequestEvent [" + super.toString() + "]";
	}

}
