/**
 * 
 */
package com.yhd.arch.photon.common;

import java.util.HashMap;
import java.util.Map;

import com.yhd.arch.photon.constants.PhotonStatus;
import com.yhd.arch.photon.constants.ProtocolType;
import com.yhd.arch.photon.constants.SupportedCodec;
import com.yhd.arch.zone.RoutePriority;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.uuid.MD5;

/**
 * @author root
 * 
 */
public class HostInfo {

	private PhotonStatus status = PhotonStatus.ENABLE;
	private String hostUrl;
	private String ip;
	private int port;
	private Integer weight = Integer.valueOf(1);
	private ProtocolType protocol;
	private String codecType = SupportedCodec.HEDWIG;
	private String serTrigger = "";
	private Map<String, RoutePriority> methodRP = new HashMap<String, RoutePriority>();

	private String zone;
	private String idc;
	private String level;

	public HostInfo(String host) {
		this.hostUrl = host;
	}

	public HostInfo(String ip, int port, String hostString, PhotonStatus ps, ProtocolType protocol, int curWeight) {
		this.status = ps;
		this.hostUrl = hostString;
		this.protocol = protocol;
		this.weight = curWeight;
	}

	public HostInfo(ServiceProfile profile) {
		this.ip = profile.getHostIp();
		this.port = profile.getPort();
		this.status = PhotonStatus.getStatusByCode(profile.getCurStatus().getCode());
		this.hostUrl = profile.getHostString();
		this.protocol = profile.getTransProtocol();
		this.weight = profile.getWeighted();
		this.codecType = profile.getCodecName();
		this.serTrigger = profile.getSearalizeTrigger();
		try {
			this.methodRP = profile.getMethodRP();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		this.zone =profile.getPubZone();
		this.idc =profile.getPubIdc();
		this.level =profile.getProviderLevel();
	}

	public Map<String, RoutePriority> getMethodRP() {
		return methodRP;
	}

	public void setMethodRP(Map<String, RoutePriority> methodRP) {
		this.methodRP = methodRP;
	}

	public String getSerTrigger() {
		return serTrigger;
	}

	public RoutePriority getHostRoutePriority(String uniqMethodName) {
		if (methodRP != null && methodRP.size() > 0) {
			RoutePriority rp = methodRP.get(uniqMethodName);
			return rp == null ? RoutePriority.Default : rp;
		} else {
			return RoutePriority.Default;
		}

	}

	public PhotonStatus getStatus() {
		return status;
	}

	public void setStatus(PhotonStatus status) {
		this.status = status;
	}

	public String getHostUrl() {
		return hostUrl;
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public ProtocolType getProtocol() {
		return protocol;
	}

	public void setProtocol(ProtocolType protocol) {
		this.protocol = protocol;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getCodecType() {
		return codecType;
	}

	public void setZone(String zone){
		this.zone = zone;
	}
	public String getZone(){
		return zone;
	}
	public void setIdc(String idc){
		this.idc = idc;
	}
	public String getIdc(){
		return idc;
	}

	public void setLevel(String level){
		this.level = level;
	}
	public String getLevel(){
		return level;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostUrl == null) ? 0 : hostUrl.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((weight == null) ? 0 : weight.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HostInfo other = (HostInfo) obj;
		if (hostUrl == null) {
			if (other.hostUrl != null)
				return false;
		} else if (!hostUrl.equals(other.hostUrl))
			return false;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		if (protocol != other.protocol)
			return false;
		if (status != other.status)
			return false;
		if (weight == null) {
			if (other.weight != null)
				return false;
		} else if (!weight.equals(other.weight))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HostInfo [status=" + status + ", hostUrl=" + hostUrl + ", weight=" + weight + ", protocol=" + protocol + "]";
	}

	public String getMD5String() {
		StringBuilder sb = new StringBuilder();
		if (status != null) {
			sb.append(status);
		}
		if (weight != null) {
			sb.append(weight);
		}
		if (methodRP != null && methodRP.size() > 0) {
			for (Map.Entry<String, RoutePriority> entry : methodRP.entrySet()) {
				sb.append(entry.getKey());
				sb.append(entry.getValue().getCode());
			}
		}
		String result = MD5.getInstance().getMD5String(sb.toString());
		return result;
	}

}
