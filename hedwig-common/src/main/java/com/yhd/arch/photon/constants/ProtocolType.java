/**
 * 
 */
package com.yhd.arch.photon.constants;

/**
 * @author root
 *
 */
public enum ProtocolType {
	
	NONE("NONE"),
	HTTP(SupportedProtocol.HTTP),
	HTTPS(SupportedProtocol.HTTPS),
	TCP(SupportedProtocol.TCP),
	UDP(SupportedProtocol.UDP),
	AKKAtcp(SupportedProtocol.AKKAtcp),
	AKKAudp(SupportedProtocol.AKKAudp);
	
	private String prefix;
	private ProtocolType(String prefix){
		this.prefix = prefix;
	}
	public String getPrefix() {
		return prefix;
	}
	
	public static ProtocolType getByPrefix(String prefix){
		for(ProtocolType p:ProtocolType.values()){
			if(p.getPrefix().equals(prefix)){
				return p;
			}
		}
		return NONE;
	}
	
}
