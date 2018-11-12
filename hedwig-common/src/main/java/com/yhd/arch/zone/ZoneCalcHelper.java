package com.yhd.arch.zone;

import java.util.HashMap;
import java.util.Map;

public class ZoneCalcHelper {

	private static double EARTH_RADIUS = 6378.137;
	public static long BANDWIDTH_K = 1024;
	public static long BANDWIDTH_M = 1024 * 1024;
	public static long BANDWIDTH_G = 1024 * 1024 * 1024;

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	public static double calcDistance(double lat1, double lng1, double lat2, double lng2) {
		double s = 0;
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	public static Map<String, Double> calcDistences(Map<String, Zone> zoneMap) {
		Map<String, Double> map = new HashMap<String, Double>();
		for (Zone z1 : zoneMap.values()) {
			for (Zone z2 : zoneMap.values()) {
				String z1Name = z1.getName();
				String z2Name = z2.getName();
				if (!z1Name.equals(z2Name)) {
					double lat1 = z1.getLatitude();
					double lat2 = z2.getLatitude();
					double lng1 = z1.getLongtitude();
					double lng2 = z2.getLongtitude();
					String key = genKey(z1Name, z2Name);
					double distence = calcDistance(lat1, lng1, lat2, lng2);
					map.put(key, distence);
					key = genKey(z2Name, z1Name);
					map.put(key, distence);
				} else {
					String key = genKey(z1Name, z2Name);
					map.put(key, 0d);
				}

			}
		}
		return map;
	}

	public static String genKey(String srcZone, String destZone) {
		return srcZone + "#" + destZone;
	}

	public static Map<String, Long> calcBandwidth(Map<String, Zone> zoneMap) {
		Map<String, Long> map = new HashMap<String, Long>();
		for (Zone z1 : zoneMap.values()) {
			for (Zone z2 : zoneMap.values()) {
				String z1Name = z1.getName();
				String z2Name = z2.getName();
				if (!z1Name.equals(z2Name)) {
					long z1out = z1.getBandwidthOut();
					long z2in = z2.getBandwidthIn();
					long z1z2Bandwidth = z1out < z2in ? z1out : z2in;
					String key = genKey(z1Name, z2Name);
					map.put(key, bandwidthFoolProof(z1z2Bandwidth));
					long z2out = z2.getBandwidthOut();
					long z1in = z1.getBandwidthIn();
					long z2z1Bandwidth = z2out < z1in ? z2out : z1in;
					key = genKey(z2Name, z1Name);
					map.put(key, bandwidthFoolProof(z2z1Bandwidth));
				} else {
					String key = genKey(z1Name, z2Name);
					map.put(key, BANDWIDTH_G);
				}

			}
		}
		return map;
	}

	private static long bandwidthFoolProof(long value) {
		if (value < 0) {
			return BANDWIDTH_K;
		} else if (value == 0) {
			return 0;
		} else {
			return value;
		}
	}
}
