/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.config;

import java.util.Hashtable;
import java.util.concurrent.Executor;

import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;
import com.yihaodian.configcentre.listener.ConfigureTargetListener;

/**
 * @author root
 * 
 */
public class ProperitesListener implements ConfigureTargetListener {

	private String configInfo;

	private Object targetObject;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yihaodian.configcentre.manager.ManagerListener#getExecutor()
	 */
	@Override
	public Executor getExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yihaodian.configcentre.manager.ManagerListener#receiveConfigInfo(
	 * java.lang.String)
	 */
	@Override
	public void receiveConfigInfo(String configInfo) {
		this.configInfo = configInfo;
		if (configInfo != null) {
			Hashtable<String, String> properties = YccGlobalPropertyConfigurer.loadProperties(configInfo);
			if (targetObject instanceof ProperitesContainer) {
				((ProperitesContainer) targetObject).pullAll(properties);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yihaodian.configcentre.listener.ConfigureTargetListener#setTargetObject
	 * (java.lang.Object)
	 */
	@Override
	public void setTargetObject(Object targetObj) {
		this.targetObject = targetObj;

	}

}
