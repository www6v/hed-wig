/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.locator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.balancer.BalancerFactory;
import com.yihaodian.architecture.hedwig.balancer.LoadBalancer;
import com.yihaodian.architecture.hedwig.client.event.engine.HedwigEventEngine;
import com.yihaodian.architecture.hedwig.client.jmx.GrayWRRBalancerJMXRegistration;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.jmx.AbstractJMXRegistration;
import com.yihaodian.architecture.hedwig.common.jmx.JmxRegistration;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ServiceRelivePolicy;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.architecture.zkclient.ZkClient;

/**
 * This locator is implemented base on zookeeper ephemeral node. It will
 * synchronize with zk server to make sure the profileContainer is up-to-date.
 * 
 * @author root
 * 
 */
public class ZkServiceLocator implements JmxRegistration, IServiceLocator<ServiceProfile> {

	private static Logger logger = LoggerFactory.getLogger(ZkServiceLocator.class);
	protected ZkClient _zkClient = null;
	protected Map<String, ServiceProfile> profileContainer = new ConcurrentHashMap<String, ServiceProfile>();
	protected static boolean isProfileSensitive = false;
	protected final LoadBalancer<ServiceProfile> balancer;
	protected boolean initialized = false;
	protected ClientProfile clientProfile;
	protected ScheduledExecutorService ses = HedwigEventEngine.getEngine().getStpes();
	protected int DELY = InternalConstants.DEFAULT_SCHEDULER_DELY;
	private IZkChildListener serviceChildListener;
	private IZkDataListener serviceDataListener;

	public ZkServiceLocator(ClientProfile clientProfile) throws HedwigException {
		super();
		this.clientProfile = clientProfile;
		this.DELY = ProperitesContainer.client().getIntProperty(PropKeyConstants.HEDWIG_SCHEDULER_POOL_DELY, this.DELY);
		this.isProfileSensitive = clientProfile.isProfileSensitive();
		this._zkClient = ZkUtil.getZkClientInstance();
		this.balancer = BalancerFactory.getInstance().getBalancer(clientProfile.getBalanceAlgo());
		initZKServiceListener();
		this.loadServiceProfile(clientProfile);

		///jmxRegistration(this.balancer);
	}

	@Override
	public <T> void jmxRegistration(T balancer) {	
		if(AbstractJMXRegistration.allowedRegisterOnJmxWebServer()) {
			try {
				new GrayWRRBalancerJMXRegistration().registerMBeanOnServer(balancer);
			} catch (Exception e) {
				logger.error(e.getMessage() + " registerJMXBean fail.");
			}			
		}
		
		if(AbstractJMXRegistration.allowedRegisterJmx()) {
			try {
				new GrayWRRBalancerJMXRegistration().registerBean(balancer);
			} catch (Exception e) {
				logger.error(e.getMessage() + " registerJMXBean fail.");
			} finally {
				AbstractJMXRegistration.resetDefaultConf();
			}		
		}
	}
	
	private void initZKServiceListener() {
		serviceChildListener = new IZkChildListener() {

			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				if (parentPath != null) {
					Map<String, ServiceProfile> newProfileMap = new HashMap<String, ServiceProfile>();
					ServiceProfile profile = null;
					String childPath;
					for (String child : currentChilds) {
						childPath = HedwigUtil.getChildFullPath(parentPath, child);
						if (profileContainer.containsKey(child)) {
							profile = profileContainer.get(child);
						} else {
							profile = _zkClient.readData(childPath, true);
							observeSpecifyChildData(childPath);
						}
						if (profile != null && InternalConstants.PROTOCOL_PROFIX_HTTP.equals(profile.getProtocolPrefix())) {
							newProfileMap.put(child, profile);
						}
					}
					profileContainer = newProfileMap;
					balancer.updateProfiles(profileContainer.values());
				}
			}
		};
		serviceDataListener = new IZkDataListener() {

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				if (!HedwigUtil.isBlankString(dataPath)) {
					String child = HedwigUtil.getChildShortPath(dataPath);
					if (profileContainer.containsKey(child)) {
						profileContainer.remove(child);
					}
				}
				balancer.updateProfiles(profileContainer.values());
			}

			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				if (!HedwigUtil.isBlankString(dataPath)) {
					String child = HedwigUtil.getChildShortPath(dataPath);
					updatProfile(child, data);
				}
			}
		};

	}

	private void loadServiceProfile(ClientProfile profile) {
		String parentPath = profile.getParentPath();
		List<String> childList = null;
		if (parentPath != null) {
			if (!_zkClient.exists(parentPath)) {
				logger.error("Can't find path " + parentPath + " in ZK. Can't find service provider for now");
				_zkClient.createPersistent(parentPath, true);
			}
			observeChild(parentPath);
			childList = _zkClient.getChildren(parentPath);
			observeChildData(parentPath, childList);
		}
		initialized = true;
	}

	private void observeChildData(final String parentPath, List<String> childList) {
		if (childList != null && childList.size() > 0) {
			for (String child : childList) {
				String childPath = HedwigUtil.getChildFullPath(parentPath, child);
				if (_zkClient.exists(childPath)) {
					Object obj = _zkClient.readData(childPath, true);
					addServiceProfile(child, obj);
					observeSpecifyChildData(childPath);
				}
			}
		}
	}

	private void addServiceProfile(String child, Object obj) {
		if (obj != null) {
			ServiceProfile sp = (ServiceProfile) obj;
			if(InternalConstants.PROTOCOL_PROFIX_HTTP.equals(sp.getProtocolPrefix())){
				sp.setRelivePolicy(new ServiceRelivePolicy(sp.getHostString()));
				profileContainer.put(child, sp);
			}
		}

	}

	/**
	 * Observe specify node profile change.
	 * 
	 */
	private void observeSpecifyChildData(String childPath) {

		if (isProfileSensitive) {
			_zkClient.subscribeDataChanges(childPath, serviceDataListener);
		}
	}

	public void updatProfile(String child, Object obj) {
		if (obj != null) {
			ServiceProfile nsp = (ServiceProfile) obj;
			ServiceProfile osp = profileContainer.get(child);
			if (osp != null) {
				if (nsp.getWeighted() != osp.getWeighted()) {
					osp.update(nsp);
					balancer.updateProfiles(profileContainer.values());
				} else {
					osp.update(nsp);
				}
			}
		}
	}

	/**
	 * Observe the node change, add or delete
	 * 
	 * @param basePath
	 */
	private void observeChild(String basePath) {
		if (_zkClient.exists(basePath)) {
			_zkClient.subscribeChildChanges(basePath, serviceChildListener);
		}

	}

	@Override
	public ServiceProfile getService() {
		ServiceProfile sp = null;
		while (!initialized) {
			Thread.yield();
		}
		sp = balancer.select();
		
		return sp;
	}

	@Override
	public Collection<ServiceProfile> getAllService() {
		return profileContainer.values();
	}
	@Override
	public LoadBalancer<ServiceProfile> getLoadBalancer() {
		return this.balancer;
	}
}
