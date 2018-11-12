/**
 * 
 */
package com.yhd.arch.zone;

/**
 * @author root
 *
 */
public interface ZoneConstants {

	public static final String ZONE_ROOT = "/ZoneMeta";
	public static final String FLAG_CROSS_ZONE = "/crossZone_hedwig";
	public static final String ZK_USAGE_SOA = "SOA";
	public static final String ZK_USAGE_MQ = "MQ";
	public static final String ZK_USAGE_CACHE = "CACHE";
	public static final String ZK_USAGE_DAL = "DAL";
	public static final String ZK_USAGE_SCHEDULER = "SCHEDULER";
	public static final String ZK_USAGE_UNKNOWN = "UNKNOWN";
	public static final String ZK_USAGE_OPS = "OPS";
	public static final String ZK_USAGE_EXT1 = "EXT1";
	public static final String ZK_USAGE_EXT2 = "EXT2";
	public static final int ROUTE_PRIORITY_PRIMARY = 10;
	public static final int ROUTE_PRIORITY_DEFAULT = 5;
	public static final int ROUTE_PRIORITY_BACKUP = 1;
	public static final int ROUTE_PRIORITY_NONE = 0;

	public static final int ZK_SESSION_TIMEOUT = 15000;

	public static final String ZONE_PATH = "/ZoneMeta/zones";
	public static final String IDC_ZONE_PATH = "/SOA/ZoneMeta/zones";

}
