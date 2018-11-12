/**
 * 
 */
package com.yhd.arch.zone.bean;

import java.io.Serializable;

import com.yhd.arch.zone.RoutePriority;

/**
 * @author root
 *
 */
public class MethodCrossZoneInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4220226037697110386L;

	private String methodName;
	private String zoneName;
	private RoutePriority routerPriority;

	public MethodCrossZoneInfo() {
		super();
	}

	public MethodCrossZoneInfo(String methodName, String zoneName, int priority) {
		super();
		this.methodName = methodName;
		this.zoneName = zoneName;
		this.routerPriority = RoutePriority.getByCode(priority);
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	public RoutePriority getRouterPriority() {
		return routerPriority;
	}

	public void setRouterPriority(RoutePriority routerPriority) {
		this.routerPriority = routerPriority;
	}

	@Override
	public String toString() {
		return "MethodCrossZoneInfo [methodName=" + methodName + ", zoneName=" + zoneName + ", routerPriority=" + routerPriority + "]";
	}

}
