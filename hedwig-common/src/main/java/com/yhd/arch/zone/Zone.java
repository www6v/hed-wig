/**
 * 
 */
package com.yhd.arch.zone;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author root
 *
 */
public class Zone implements Serializable {
	private static final long serialVersionUID = -3449736799079134998L;
	private String name;
	private String alias;
	private Map<ZkClusterUsage, String> zkClusterMap = new HashMap<ZkClusterUsage, String>();
	private Map<String,String> extZkClusterMap = new HashMap<String, String>();
	private double longtitude;
	private double latitude;
	private String platform;
	private String platformName;
	private long bandwidthIn;
	private long bandwidthOut;
	private String desc;
	private DeployLevel zoneLevel;

	public Zone() {
		super();
	}

	public Zone(String name, String alias, Map<ZkClusterUsage, String> zkClusterMap, double longtitude, double latitude,
			String platformName, long bandwidthIn, long bandwidthOut, String platform, String desc) {
		super();
		this.name = name;
		this.alias = alias;
		this.zkClusterMap = zkClusterMap;
		this.longtitude = longtitude;
		this.latitude = latitude;
		this.platform = platform;
		this.platformName = platformName;
		this.bandwidthIn = bandwidthIn;
		this.bandwidthOut = bandwidthOut;
		this.desc = desc;
	}

	public Zone(String name, String alias, String platformName, String platform, String desc) {
		super();
		this.name = name;
		this.alias = alias;
		this.platform = platform;
		this.platformName = platformName;
		this.desc = desc;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Map<ZkClusterUsage, String> getZkClusterMap() {
		return zkClusterMap;
	}

	public void setZkClusterMap(Map<ZkClusterUsage, String> zkClusterMap) {
		this.zkClusterMap = zkClusterMap;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getPlatformName() {
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public long getBandwidthIn() {
		return bandwidthIn;
	}

	public void setBandwidthIn(long bandwidthIn) {
		this.bandwidthIn = bandwidthIn;
	}

	public long getBandwidthOut() {
		return bandwidthOut;
	}

	public void setBandwidthOut(long bandwidthOut) {
		this.bandwidthOut = bandwidthOut;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getZkClusterByUsage(ZkClusterUsage usage) {
		return zkClusterMap.get(usage);
	}

	public String getZkClusterByUsageCode(String usageCode){
		String serverList = null;
		ZkClusterUsage usage = ZkClusterUsage.getByCode(usageCode);
		if(!usage.equals(ZkClusterUsage.UNKNOWN)){
			if(zkClusterMap!=null){
				serverList = zkClusterMap.get(usage);
			}
		}else{
			if(extZkClusterMap!=null){
				serverList = extZkClusterMap.get(usageCode);
			}
		}
		return serverList;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}


	public String getName() {

		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getExtZkClusterMap() {
		return extZkClusterMap;
	}

	public void setExtZkClusterMap(Map<String, String> extZkClusterMap) {
		this.extZkClusterMap = extZkClusterMap;
	}

	public DeployLevel getZoneLevel() {
		return zoneLevel;
	}

	public void setZoneLevel(DeployLevel zoneLevel) {
		this.zoneLevel = zoneLevel;
	}
}
