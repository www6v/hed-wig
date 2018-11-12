/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.config;

import java.util.Hashtable;
import java.util.Properties;

import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * @author root
 *
 */
public class ConfigCenterHelper {


	private Hashtable<String, String> properites = new Hashtable<String, String>();

	public ConfigCenterHelper(String group, String file) {
		init(group, file);
	}

	public Hashtable<String, String> getProperites() {
		return properites;
	}

	private void init(String group, String file) {
		Properties p = YccGlobalPropertyConfigurer.loadConfigProperties(group, file, false);
		if(p!=null){
			properites = YccGlobalPropertyConfigurer.toHashtable(p);
		}

	}

	public String getProperty(String key, String defaultValue) {
		String value = properites.get(key);
		return value == null ? defaultValue : value;
	}


}
