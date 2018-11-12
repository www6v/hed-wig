/**
 * 
 */
package com.yhd.arch.zone.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author root
 *
 */
public class ServiceCrossZoneInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6374747985328192153L;

	private String serviceName;
	private Map<String, List<MethodCrossZoneInfo>> method4Zone = new HashMap<String, List<MethodCrossZoneInfo>>();

	public ServiceCrossZoneInfo() {
		super();
	}

	public ServiceCrossZoneInfo(String sName) {
		this.serviceName = sName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setMethodCrossZoneInfo(String s, List<MethodCrossZoneInfo> infos) {
		method4Zone.put(s, infos);
	}

	public Map<String, List<MethodCrossZoneInfo>> getMethod4Zone() {
		return method4Zone;
	}

	public void setMethod4Zone(Map<String, List<MethodCrossZoneInfo>> method4Zone) {
		this.method4Zone = method4Zone;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
