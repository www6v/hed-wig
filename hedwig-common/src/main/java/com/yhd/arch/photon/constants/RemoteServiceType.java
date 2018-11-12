/**
 * 
 */
package com.yhd.arch.photon.constants;

/**
 * Hedwig remote service type enumation.
 * 
 * @author wangbenwang
 *
 */
public enum RemoteServiceType {

	HESSIAN(LaserBeakConstants.REMOTE_SERVICE_TYPE_HESSIAN), AKKA(LaserBeakConstants.REMOTE_SERVICE_TYPE_AKKA), DEFAULT(
			LaserBeakConstants.REMOTE_SERVICE_TYPE_HESSIAN), UNKOWN(LaserBeakConstants.REMOTE_SERVICE_TYPE_UNKNOWN);

	private String value;

	private RemoteServiceType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	/**
	 * Parse a RemoteServiceType from a string, if this string is empty or
	 * invalid, return RemoteServiceType.UNKNOWN.
	 * 
	 * @param value
	 *            the value of RemoteServiceType
	 * @return RemoteServiceType
	 */
	public static RemoteServiceType getByValue(String value) {
		for (RemoteServiceType p : RemoteServiceType.values()) {
			if (p.name().equals(value)) {
				return p;
			}
		}
		return UNKOWN;
	}
}
