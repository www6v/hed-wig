/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.util;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yhd.arch.container.RootContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.StringUtils;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.hedwig.common.uuid.UUID;
import com.yihaodian.architecture.hedwig.engine.event.IEvent;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * @author root
 * 
 */
public class HedwigClientUtil {
	private static Logger logger = LoggerFactory.getLogger(HedwigClientUtil.class);
	private static String PoolName = "";
	private static volatile long mark = -1;
	private static int SEQUENCE = 0;
	private static Lock lock = new ReentrantLock();
	private static String shortIP = "";
	private static List<String> appPathDict;
	static {
		genShortIp();
	}

	public static Object getHessianProxy(HedwigContext context, String serviceUrl) throws MalformedURLException {
		Object proxy = null;
		if (context.getHessianProxyMap().containsKey(serviceUrl)) {
			proxy = context.getHessianProxyMap().get(serviceUrl);
		} else {
			proxy = createProxy(context, serviceUrl);
		}
		return proxy;
	}

	public static Object createProxy(HedwigContext context, String serviceUrl) throws MalformedURLException {
		Object proxy = null;
		proxy = context.getProxyFactory().create(context.getServiceInterface(), serviceUrl);
		if (proxy != null) {
			context.getHessianProxyMap().put(serviceUrl, proxy);
		}
		return proxy;
	}

	public static String generateReqId(IEvent<Object> event) {
		String reqId = "";
		long t = HedwigUtil.getCurrentTime();
		reqId = "req-" + t + "-" + shortIP + event.hashCode() + getSeq();
		return reqId;
	}

	public static String generateGlobalId(IEvent<Object> event) {
		String glbId = "";
		long t = HedwigUtil.getCurrentTime();
		glbId = "glb-" + t + "-" + shortIP + event.hashCode() + getSeq();
		return glbId;
	}

	private static int getSeq() {
		lock.lock();
		try {
			int v = 0;
			long l = System.currentTimeMillis();
			if (mark < l) {
				mark = l;
				SEQUENCE = 0;
				v = 0;
			} else {
				v = ++SEQUENCE;
			}
			return v;
		} finally {
			lock.unlock();
		}
	}

	public static String generateTransactionId() {
		String txnId = "";
		txnId = "txn-" + new UUID().toString();
		return txnId;
	}

	public static int getRedoCount(HedwigContext context) {
		int nodeCount = context.getLocator().getAllService().size();
		int redoCount = nodeCount >= 1 ? (nodeCount - 1) : 0;
		return redoCount;
	}

	public static void genShortIp() {
		StringBuilder sb = new StringBuilder();
		String hostIp = ProperitesContainer.client().getProperty(PropKeyConstants.HOST_IP, "");
		if (!HedwigUtil.isBlankString(hostIp)) {
			String[] nodes = hostIp.split("\\.");
			if (nodes != null && nodes.length == 4) {
				sb.append(nodes[2]).append(".").append(nodes[3]).append("-");
			}
		}
		shortIP = sb.toString();
	}

	public static String generateReqId(IEvent<Object> event, String clientAppName, String serviceAppName) {
		String reqId = "";
		long t = HedwigUtil.getCurrentTime();
		reqId = clientAppName + "-" + serviceAppName + "-" + t + "-" + shortIP + event.hashCode() + getSeq();
		return reqId;
	}

	public static String getClientPoolName() {
		if (HedwigUtil.isBlankString(PoolName)) {
			lock.lock();
			try {
				PoolName = YccGlobalPropertyConfigurer.getMainPoolId();
			} finally {
				lock.unlock();
			}
		}
		return PoolName;
	}

	public static String getServPoolName(ClientProfile profile, List<String> appPathDict) {
		String resultPoolName = getServPoolNameFromServiceProfile(profile);
		if (org.apache.commons.lang.StringUtils.isBlank(resultPoolName)) {
			if (logger.isDebugEnabled()) {
				logger.debug("###getServPoolNameFromServiceProfile is NULL servPool=" + resultPoolName);
			}
			resultPoolName = getServPoolNameFromAppPathDict(profile, appPathDict);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("###getServPoolName() resultPoolName=" + resultPoolName);
		}
		return resultPoolName;
	}

	// 优先从ServiceProfile 拿服务所属poolId (注：为未来hedwig 去除对appPathDict依赖做准备)
	private static String getServPoolNameFromServiceProfile(ClientProfile profile) {
		String servPool = null;
		try {
			if (profile != null) {
				String parentPath = profile.getParentPath();
				servPool=RootContainer.getInstance().getServicePoolName(parentPath);
				if(servPool==null) {
					ZkClient _zkClient = ZkUtil.getZkClientInstance();
					if (parentPath != null && _zkClient.exists(parentPath)) {
						List<String> childList = _zkClient.getChildren(parentPath);
						if (childList != null && childList.size() > 0) {
							for (String child : childList) {
								String childPath = HedwigUtil.getChildFullPath(parentPath, child);
								if (_zkClient.exists(childPath)) {
									Object obj = _zkClient.readData(childPath, true);
									if (obj != null) {
										ServiceProfile sp = (ServiceProfile) obj;
										if (sp != null && sp.getPubPoolName() != null) {
											servPool = sp.getPubPoolName();
											RootContainer.getInstance().putServicePoolName(parentPath,servPool);
											break;
										}
									}
								}
							}
						}
					}
				}
				if(servPool==null){
					servPool=InternalConstants.UNKONW_POOlId;
					RootContainer.getInstance().putServicePoolName(parentPath,servPool);
				}

				if(servPool!=null&&servPool.equals(InternalConstants.UNKONW_POOlId)){
					servPool=null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return servPool;
	}

	// 从appPathDict 拿服务所属poolId
	private static String getServPoolNameFromAppPathDict(ClientProfile profile, List<String> appPathDict) {
		String servPool = null;
		if (profile != null && appPathDict != null && appPathDict.size() > 0) {
			String appPath;
			try {
				appPath = ZkUtil.createAppPath(profile);
				String noSlashAppPath = StringUtils.replaceSlash(appPath) + ":";
				String pathPoolPair = null;
				for (String pair : appPathDict) {
					if (pair.startsWith(noSlashAppPath)) {
						pathPoolPair = pair;
						servPool = pathPoolPair.replace(noSlashAppPath, "");
						RootContainer.getInstance().putServicePoolName(profile.getParentPath(),servPool);
						break;
					}
				}
			} catch (InvalidParamException e) {
				e.printStackTrace();
			}
		}
		return servPool;
	}

	public synchronized static List<String> getAppPathDict() {
		if (appPathDict == null) {
			try {
				appPathDict = ZkUtil.getZkClientInstance().getChildren(InternalConstants.HEDWIG_PAHT_APPDICT);
			} catch (HedwigException e) {
				e.printStackTrace();
			}
		}
		return appPathDict;
	}

	public static void setAppPathDict(List<String> appPathDict) {
		HedwigClientUtil.appPathDict = appPathDict;
	}
}
