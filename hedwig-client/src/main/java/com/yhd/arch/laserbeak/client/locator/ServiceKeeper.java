/**
 * 
 */
package com.yhd.arch.laserbeak.client.locator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yihaodian.architecture.hedwig.common.util.HedwigJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.yhd.arch.photon.core.ServiceInfo;
import com.yhd.arch.photon.emitter.event.init.RouterBuildEvent;
import com.yhd.arch.photon.util.ClientSystem;
import com.yihaodian.architecture.hedwig.client.jmx.ServiceKeeperJMXRegistration;
import com.yihaodian.architecture.hedwig.client.jmx.ServiceKeeperMXBean;
import com.yihaodian.architecture.hedwig.client.jmx.switcher.ServiceKeeperJmxSwitcher;
import com.yihaodian.architecture.hedwig.client.jmx.switcher.ServiceKeeperSwitcherRegistration;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.jmx.AbstractJMXRegistration;
import com.yihaodian.architecture.hedwig.common.jmx.JmxRegistration;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.IZkDataListener;

import static com.yihaodian.architecture.hedwig.common.jmx.AbstractJMXRegistration.allowedRegisterOnJmxWebServer; 
import static com.yihaodian.architecture.hedwig.common.jmx.AbstractJMXRegistration.allowedRegisterJmx;
/**
 * @author root
 *
 */
public class ServiceKeeper extends ServiceNodeLocator implements JmxRegistration, ServiceKeeperMXBean {
 
	private Logger logger = LoggerFactory.getLogger(ServiceKeeper.class);
	private Map<String, IZkChildListener> childListenerMap = new HashMap<String, IZkChildListener>();
	private Map<String, IZkDataListener> dataListenerMap = new HashMap<String, IZkDataListener>();
	private IZkChildListener dictChangeListener;
	private IZkDataListener grayNodeListener;
	private IZkChildListener grayflagListener;
	private IZkChildListener campListener;
	private IZkChildListener campMemberListener;
	private IZkChildListener serviceNodesListener;
	private IZkDataListener profileListener;

	public ServiceKeeper(ClientProfile profile, ServiceInfo info) throws HedwigException {
		super(profile, info);
		if (_zkClient != null) {
			initListener();
			subscribeDicChange();
			subscribeGray();
			subscribeCamps();
			subscribeServiceNodeChange();
			subscribeProfileChange();
			
//			jmxRegistration( this );  ///  By default, closed.			
//			jmxSwitcherRegistration();  
		}
	}
	
	@Deprecated
	private void jmxSwitcherRegistration() {	
		try {
			new ServiceKeeperSwitcherRegistration().registerBean( new ServiceKeeperJmxSwitcher(this) );
		} catch (Exception e) {
			logger.error( e.getMessage() + " jmx switcher registration fail.");
		} finally {
			AbstractJMXRegistration.resetDefaultConf();
		}			
	}

	@Deprecated
	@Override
	public <T> void jmxRegistration(T serviceKeeper) {
		if(allowedRegisterOnJmxWebServer()) {
			try {
				new ServiceKeeperJMXRegistration().registerMBeanOnServer(serviceKeeper);
			} catch (Exception e) {
				logger.error( e.getMessage() + " jmx registration on web server fail.");
			}				
		}		
		
		if(allowedRegisterJmx()) {
			try {
				new ServiceKeeperJMXRegistration().registerBean(serviceKeeper);
			} catch (Exception e) {
				logger.error( e.getMessage() + " jmx registration fail.");
			}	
		}
	}

	private void subscribeProfileChange() {
		if (isProfileSensitive) {
			String childFullPath = "";
			for (String child : profileContainer.keySet()) {
				childFullPath = HedwigUtil.getChildFullPath(parentPath, child);
				subscribeDataChange(childFullPath, profileListener);
			}
		}
	}

	private void subscribeServiceNodeChange() {
		if (_zkClient.exists(parentPath)) {
			subscribeChildChange(parentPath, serviceNodesListener);
		}
	}

	/**
	 * Observe group change, include group increase/decrease and process
	 * increase/decrease.
	 */
	private void subscribeCamps() {
		try {
			final String baseCamp = ZkUtil.createBaseCampPath(clientProfile);
			String campPath = null;
			// observe group change add or delete
			subscribeChildChange(baseCamp, campListener);
			List<String> camps = _zkClient.getChildren(baseCamp);
			if (camps != null && camps.size() > 0) {
				for (String campName : camps) {
					campPath = ZkUtil.createCampPath(clientProfile, campName);
					// observe available process change of interesting group
					subscribeChildChange(campPath, campMemberListener);
				}
			}
		} catch (Exception e) {
			logger.error("Observe camps failed!!!");
		}

	}

	private void subscribeGray() {
		if(grayInitialized) {
			if (_zkClient.exists(grayPath)) {
				subscribeDataChange(grayPath, grayNodeListener);
			} else {
				if (!_zkClient.exists(flagPath)) {
					_zkClient.createPersistent(flagPath, true);
				}
				subscribeChildChange(flagPath, grayflagListener);
			}
		}
	}

	private void subscribeDicChange() {

		subscribeChildChange(InternalConstants.HEDWIG_PAHT_APPDICT, dictChangeListener);

	}

	protected void subscribeChildChange(String path, IZkChildListener childListener) {
		_zkClient.subscribeChildChanges(path, childListener);
		childListenerMap.put(path, childListener);
	}

	protected void subscribeDataChange(String path, IZkDataListener dataListener) {
		_zkClient.subscribeDataChanges(path, dataListener);
		dataListenerMap.put(path, dataListener);
	}

	protected void unsubscribeChildChange(String path, IZkChildListener childListener) {
		_zkClient.unsubscribeChildChanges(path, childListener);
		childListenerMap.remove(path);
	}

	protected void unsubscribeDataChange(String path, IZkDataListener childListener) {
		_zkClient.unsubscribeDataChanges(path, childListener);
		dataListenerMap.remove(path);
	}

	public void destory() {
		if (childListenerMap.size() > 0) {
			for (Map.Entry<String, IZkChildListener> entry : childListenerMap.entrySet()) {
				if (entry != null) {
					_zkClient.unsubscribeChildChanges(entry.getKey(), entry.getValue());
				}
			}
		}
		if (dataListenerMap.size() > 0) {
			for (Map.Entry<String, IZkDataListener> entry : dataListenerMap.entrySet()) {
				if (entry != null) {
					_zkClient.unsubscribeDataChanges(entry.getKey(), entry.getValue());
				}
			}
		}

	}

	private void initListener() {
		dictChangeListener = new IZkChildListener() {

			@Override
			public void handleChildChange(String pPath, List<String> currentChilds) throws Exception {
				if (logger.isDebugEnabled()) {
					logger.debug("Trigger appDict change:" + pPath + "," + System.currentTimeMillis());
				}
				HedwigClientUtil.setAppPathDict(currentChilds);
			}

		};

		grayNodeListener = new IZkDataListener() {

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				if (logger.isDebugEnabled()) {
					logger.debug("Trigger gray data delete:" + dataPath + "," + System.currentTimeMillis());
				}
				info.setGrayHost(null);
				info.setGrayOffset(0);
				ClientSystem.getInstance().getServiceManager().tell(new RouterBuildEvent(info), ActorRef.noSender());
			}

			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				if (logger.isDebugEnabled()) {
					logger.debug("Trigger gray data change:" + dataPath + "," + System.currentTimeMillis());
				}
				try {
					byte[] ba = (byte[]) data;
					parseGrayNode(ba);
				} catch (Exception e) {
					logger.error("Can't cast data to byte[],data:" + data, e);
				}

			}
		};

		grayflagListener = new IZkChildListener() {

			@Override
			public void handleChildChange(String pPath, List<String> currentChilds) throws Exception {
				if (logger.isDebugEnabled()) {
					logger.debug("Trigger gray flag change:" + pPath + "," + System.currentTimeMillis());
				}
				String grayPath = flagPath + InternalConstants.FLAG_GRAY;
				if (currentChilds.contains(InternalConstants.FLAG_GRAY)) {
					_zkClient.createPersistent(grayPath, true);
					subscribeDataChange(grayPath, grayNodeListener);
					unsubscribeChildChange(flagPath, this);
				}
			}
		};

		campListener = new IZkChildListener() {
			@Override
			public void handleChildChange(String pPath, List<String> currentChilds) throws Exception {
				if (logger.isDebugEnabled()) {
					logger.debug("Trigger camp change:" + pPath + "," + HedwigJsonUtil.toJSONString(currentChilds));
				}
				List<String> camps = _zkClient.getChildren(baseCamp);
				if (camps != null && camps.size() > 0) {
					//然后添加监听
					for (String campName : camps) {
						String campPath = ZkUtil.createCampPath(clientProfile, campName);
						subscribeChildChange(campPath, campMemberListener);
					}
				}
				loadMemberWhiteList();
				notifyQualifiedMembers();
			}
		};

		campMemberListener = new IZkChildListener() {
			@Override
			public void handleChildChange(String campPath, List<String> currentChilds) throws Exception {
				if (logger.isDebugEnabled()) {
					logger.debug("Trigger camp member change:" + campPath + "," + HedwigJsonUtil.toJSONString(currentChilds));
				}
				if (currentChilds == null || currentChilds.size() ==0) {
					currentChilds=new ArrayList<String>();
				}
				updateCampMembers(campPath, currentChilds);
			}
		};

		serviceNodesListener = new IZkChildListener() {

			@Override
			public void handleChildChange(String pPath, List<String> currentChilds) throws Exception {
				if (logger.isDebugEnabled()) {
					logger.debug("Trigger service node change:" + pPath + "," + System.currentTimeMillis());
				}
				if (pPath != null) {
					Map<String, ServiceProfile> newProfileMap = new HashMap<String, ServiceProfile>();
					ServiceProfile profile = null;
					String childPath;
					for (String child : currentChilds) {
						childPath = HedwigUtil.getChildFullPath(parentPath, child);
						if (profileContainer.containsKey(child)) {
							profile = profileContainer.get(child);
						} else {
							profile = _zkClient.readData(childPath, true);
							subscribeDataChange(childPath, profileListener);
						}
						addServiceProfile2Container(child, profile, newProfileMap);
					}
					profileContainer = newProfileMap;
					loadMemberWhiteList();
					notifyQualifiedMembers();
				}
			}
		};

		profileListener = new IZkDataListener() {

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				if (logger.isDebugEnabled()) {
					logger.debug("Trigger node data delete:" + dataPath + "," + System.currentTimeMillis());
				}
				if (!HedwigUtil.isBlankString(dataPath)) {
					String child = HedwigUtil.getChildShortPath(dataPath);
					if (profileContainer.containsKey(child)) {
						profileContainer.remove(child);
					}
					unsubscribeDataChange(dataPath, this);
				}
			}

			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				if (logger.isDebugEnabled()) {
					logger.debug("Trigger node data change:" + dataPath + "," + System.currentTimeMillis());
				}
				if (!HedwigUtil.isBlankString(dataPath)) {
					String child = HedwigUtil.getChildShortPath(dataPath);
					updatProfile(child, data);
				}
			}
		};
	}
	
    ///// jmx
	@Deprecated
    public void  routeTriggerForJmx() {
		if (logger.isDebugEnabled()) {
			logger.debug("route build trigged for jmx ");
		}
//		info.setGrayHost(null);
//		info.setGrayOffset(0);
		ClientSystem.getInstance().getServiceManager().tell(new RouterBuildEvent(info), ActorRef.noSender());    	
    }
}
