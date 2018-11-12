/**
 * 
 */
package com.yhd.arch.photon.constants;

/**
 * @author root
 *
 */
public interface LaserBeakConstants {

	public static final String CENTRALIZE_REGISTER = "unicornRegister";
	public static final String ZONE_REGISTER = "zoneRegister";

	/*
	 * According to the convention of yihaodian, the port range of [1800,1999]
	 * is used for hedwig listening servcies.
	 * 
	 * Frank Wang updated on Dec. 22ed, 2015.
	 */
	public static final int DEFAULT_AKKA_PORT = 1920;

	public static final String REMOTE_SERVICE_TYPE_HESSIAN = "HESSIAN";

	public static final String REMOTE_SERVICE_TYPE_AKKA = "AKKA";

	public static final String REMOTE_SERVICE_TYPE_UNKNOWN = "UNKNOWN";

}
