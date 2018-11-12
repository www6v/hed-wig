package com.yhd.arch.laserbeak.client.locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.photon.constants.RemoteServiceType;
import com.yihaodian.architecture.zkclient.IZkDataListener;

/**
 * Data listener for remote service type.
 * 
 * @author wangbenwang
 */
public class RemoteServiceTypeListener implements IZkDataListener {

	private HedwigClientServiceSelector selector;

	private static Logger logger = LoggerFactory.getLogger(RemoteServiceTypeListener.class);

	public RemoteServiceTypeListener(HedwigClientServiceSelector selector) {
		this.selector = selector;
	}

	@Override
	public void handleDataChange(String path, Object data) throws Exception {
		if (data == null) {
			logger.warn(new StringBuilder().append("Empty value for the path[").append(path).append("], so default value will be set.")
					.toString());
			selector.setRemoteServiceType(RemoteServiceType.DEFAULT);
			selector.getZkClient().writeData(path, RemoteServiceType.DEFAULT);
		} else {
			logger.warn(new StringBuilder().append("Remote service type has been changed from [")
					.append(this.selector.getRemoteServiceType().getValue()).append("] to [").append(data).append("].").toString());
			selector.setRemoteServiceType(RemoteServiceType.getByValue(data.toString()));

			selector.setFinalProxy(selector.bind(selector.getApplicationContext().getBean(selector.getRealBeanName())));
		}
	}

	@Override
	public void handleDataDeleted(String path) throws Exception {
		logger.warn(new StringBuilder().append("The path[").append(path).append("] has been removed.").toString());
		this.selector.getZkClient().unsubscribeAll();
	}

	public HedwigClientServiceSelector getSelector() {
		return selector;
	}

	public void setSelector(HedwigClientServiceSelector selector) {
		this.selector = selector;
	}

}
