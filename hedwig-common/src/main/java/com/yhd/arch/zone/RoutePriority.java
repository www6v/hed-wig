/**
 * 
 */
package com.yhd.arch.zone;

/**
 * @author root
 *
 */
public enum RoutePriority {

	Primary(ZoneConstants.ROUTE_PRIORITY_PRIMARY), Default(ZoneConstants.ROUTE_PRIORITY_DEFAULT), Backup(
			ZoneConstants.ROUTE_PRIORITY_BACKUP), None(ZoneConstants.ROUTE_PRIORITY_NONE);

	private int code;

	private RoutePriority(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static RoutePriority getByCode(int c) {
		for (RoutePriority p : RoutePriority.values()) {
			if (p.getCode() == c) {
				return p;
			}
		}
		return None;
	}
}
