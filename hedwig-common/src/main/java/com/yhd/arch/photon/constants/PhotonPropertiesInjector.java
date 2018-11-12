package com.yhd.arch.photon.constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PhotonPropertiesInjector {

	Map<String, String> systemProperties = new HashMap<String, String>();

	public void inject() {
		if (systemProperties != null) {
			for (Entry<String, String> entry : systemProperties.entrySet()) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		}
	}

	public Map<String, String> getSystemProperties() {
		return systemProperties;
	}

	public void setSystemProperties(Map<String, String> systemProperties) {
		this.systemProperties = systemProperties;
	}

}
