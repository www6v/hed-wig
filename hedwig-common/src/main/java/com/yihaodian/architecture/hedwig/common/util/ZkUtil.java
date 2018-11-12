/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.data.Stat;

import com.yhd.arch.laserbeak.common.util.ZoneZkUtil;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.BaseProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidMappingException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.zkclient.ZkClient;

/**
 * @author root
 * 
 */
public class ZkUtil {

	private static ZkClient _zkClient = null;
	private static Lock lock = new ReentrantLock();

	public static synchronized ZkClient getZkClientInstance() throws HedwigException {
		if (_zkClient == null) {
			String serverList = ProperitesContainer.provider().getProperty(PropKeyConstants.ZK_SERVER_LIST);
			if (!HedwigUtil.isBlankString(serverList)) {
				_zkClient = new ZkClient(serverList, InternalConstants.ZK_SESSION_TIMEOUT, Integer.MAX_VALUE);
			} else {
				throw new HedwigException("ZK client initial error, serverList:" + serverList);
			}
		}
		return _zkClient;
	}

	public static void closeInstance() {
		lock.lock();
		try {
			if (_zkClient != null) {
				_zkClient.close();
				_zkClient = null;
			}
		} finally {
			lock.unlock();
		}
	}

	public static String createChildPath(ServiceProfile profile) throws InvalidParamException {
		if (profile == null)
			throw new InvalidParamException(" Service profile must not null!!!");
		StringBuilder path = new StringBuilder(profile.getParentPath()).append("/").append(getProcessDesc(profile));
		return path.toString();
	}

	public static String getProcessDesc(ServiceProfile profile) throws InvalidParamException {
		StringBuilder path = new StringBuilder().append(profile.getHostIp()).append(":").append(profile.getPort());
		return path.toString();
	}

	public static String createAppPath(BaseProfile profile) throws InvalidParamException {
		if (profile == null)
			throw new InvalidParamException(" Service profile must not null!!!");
		StringBuilder appPath = new StringBuilder(profile.getRootPath());
		appPath.append("/").append(profile.getDomainName()).append("/").append(profile.getServiceAppName());
		return appPath.toString();
	}

	public static String createParentPath(BaseProfile profile) throws InvalidParamException {
		if (profile == null)
			throw new InvalidParamException(" Service profile must not null!!!");
		StringBuilder path = new StringBuilder(createAppPath(profile));
		path.append("/").append(profile.getServiceName()).append("/").append(profile.getServiceVersion());
		return path.toString();
	}

	/**
	 * @deprecated Promote to ZoneZkUtil
	 *             Delegate to ZoneZkUtil.createAppcodeDict
	 */
	public static void createAppcodeDict(BaseProfile profile, String appcode) throws InvalidParamException, InvalidMappingException {
//		if (profile == null) {
//			throw new InvalidParamException(" Service profile must not null!!!");
//		}
//		if (HedwigUtil.isBlankString(appcode)) {
//			appcode = "defaultAppName";
//		}
//		String filterCode = StringUtils.replaceSlash(appcode);
//		StringBuilder pathBuilder = new StringBuilder(InternalConstants.HEDWIG_PAHT_APPDICT);
//		String appPath = createAppPath(profile);
//		String fAppPath = StringUtils.replaceSlash(appPath);
//		pathBuilder.append("/").append(fAppPath).append(":").append(filterCode);
//		String path = pathBuilder.toString();
//		if (!_zkClient.exists(path)) {
//			_zkClient.createPersistent(path, true);
//		}
		
		ZoneZkUtil.createAppcodeDict(profile, appcode, _zkClient);
	}

	/**
	 * @deprecated Promote to ZoneZkUtil
	 */
	public static String generatePath(BaseProfile profile, String subPath) throws InvalidParamException {
		String value = "";
		if (profile == null && subPath != null)
			throw new InvalidParamException(" Service profile must not null!!!");
		StringBuilder path = new StringBuilder(profile.getRootPath() == null ? "" : profile.getRootPath());
		path.append("/").append(profile.getDomainName()).append("/").append(profile.getServiceAppName()).append("/").append(subPath);
		value = path.toString();
		if (!value.endsWith("/") && !_zkClient.exists(value)) {
			_zkClient.createPersistent(value, true);
		}
		return value;
	}

	/**
	 * @deprecated Promote to ZoneZkUtil
	 */
	public static String createRollPath(BaseProfile profile) throws InvalidParamException {
		return ZkUtil.generatePath(profile, InternalConstants.HEDWIG_PAHT_ROLL);
	}

	/**
	 * @deprecated Promote to ZoneZkUtil
	 */
	public static String createRefugeePath(BaseProfile profile) throws InvalidParamException {
		return ZkUtil.generatePath(profile, InternalConstants.HEDWIG_PAHT_CAMPS + "/" + InternalConstants.HEDWIG_PAHT_REFUGEE);
	}

	public static String createCampPath(BaseProfile profile, String campName) throws InvalidParamException {
		return ZkUtil.generatePath(profile, InternalConstants.HEDWIG_PAHT_CAMPS + "/" + campName);
	}

	/**
	 * @deprecated Promote to ZoneZkUtil
	 */
	public static String createBaseCampPath(BaseProfile profile) {
		String value = "";
		try {
			value = ZkUtil.generatePath(profile, InternalConstants.HEDWIG_PAHT_CAMPS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public static String assembleProfilePath(String domain, String appName, String servName, String servVersion, String providerHost)
			throws Exception {

		StringBuilder sb = new StringBuilder(InternalConstants.BASE_ROOT);
		if (!HedwigUtil.isBlankString(domain)) {
			sb.append("/").append(domain);
		} else {
			throw new Exception("Domain must not null!!!");
		}
		if (!HedwigUtil.isBlankString(appName)) {
			sb.append("/").append(appName);
		} else {
			throw new Exception("appName must not null!!!");
		}
		if (!HedwigUtil.isBlankString(servName)) {
			sb.append("/").append(servName);
		} else {
			throw new Exception("servName must not null!!!");
		}
		if (!HedwigUtil.isBlankString(servVersion)) {
			sb.append("/").append(servVersion);
		} else {
			throw new Exception("servVersion must not null!!!");
		}
		if (!HedwigUtil.isBlankString(providerHost)) {
			sb.append("/").append(providerHost);
		} else {
			throw new Exception("providerHost must not null!!!");
		}
		return sb.toString();
	}

	/**
	 * ZK 数据copy工具(不包含临时节点）
	 *
	 * @param from
	 *            源ZK集群客户端
	 * @param fromPath
	 *            源ZK数据路径
	 * @param to
	 *            目标ZK集群客户端
	 * @param toPath
	 *            目标ZK数据路径（如果为NULL，则和源数据路径一致）
	 */
	public static void doCopy(ZkClient from, String fromPath, ZkClient to, String toPath) {
		if (fromPath != null) {
			List<String> childPaths = from.getChildren(fromPath);
			// 叶子节点做拷贝动作，完成后退出
			if (childPaths == null || childPaths.size() == 0) {
				if (toPath == null) {
					toPath = fromPath;
				}
				Stat fromStat = from.existsWithStat(fromPath);
				// 非临时节点才做拷贝
				if (fromStat != null && fromStat.getEphemeralOwner() == 0) {
					byte[] data = from.readRawData(fromPath, true);
					if (!to.exists(toPath)) {
						to.createPersistent(toPath, true);
						to.writeRawData(toPath, data, -1);
					} else {
						to.writeRawData(toPath, data, -1);
					}
				}
				return;
			}
			// 非叶子节点继续执行递归
			for (String childPath : childPaths) {
				StringBuilder fromChildPath = new StringBuilder(fromPath).append("/").append(childPath);
				if (fromPath.equals("/")) {
					fromChildPath = new StringBuilder(fromPath).append(childPath);
				}
				StringBuilder toChildPath = fromChildPath;
				if (toPath != null) {
					if (toChildPath.equals("/")) {
						toChildPath = new StringBuilder(toChildPath).append(childPath);
					} else {
						toChildPath = new StringBuilder(toPath).append("/").append(childPath);
					}
				}
				doCopy(from, fromChildPath.toString(), to, toChildPath.toString());
			}
		}
	}
}
