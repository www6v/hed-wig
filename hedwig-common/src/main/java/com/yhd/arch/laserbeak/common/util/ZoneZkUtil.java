package com.yhd.arch.laserbeak.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.BaseProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidMappingException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.StringUtils;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.ZkClient;

public class ZoneZkUtil {
	private static Logger logger = LoggerFactory.getLogger(ZoneZkUtil.class);

	private static List<String> appPathDict;
	
	public static String createAppcodeDict(BaseProfile profile, String appcode, ZkClient zoneZk) throws InvalidParamException ,InvalidMappingException {
		if (profile == null) {
			throw new InvalidParamException(" Service profile must not null!!!");
		}
		if (HedwigUtil.isBlankString(appcode)) {
			appcode = "defaultLaserAppName";
		}
		String filterCode = StringUtils.replaceSlash(appcode); // defaultLaserAppName
		StringBuilder pathBuilder = new StringBuilder(InternalConstants.HEDWIG_PAHT_APPDICT); // /AppPathDict
		String appPath = ZkUtil.createAppPath(profile); // /TheStore/ARCH-201-SOA/hedwig-test-server-laser
		String fAppPath = StringUtils.replaceSlash(appPath); // #TheStore#ARCH-201-SOA#hedwig-test-server-laser
		pathBuilder.append("/").append(fAppPath).append(":").append(filterCode); // /AppPathDict/#TheStore#ARCH-201-SOA#hedwig-test-server-laser:defaultLaserAppName
		String path = pathBuilder.toString();
				
		if (path.endsWith("/") == false && zoneZk.exists(path) == false) { // 映射关系不存在  // 前面已经创建
			String contextPath = path.substring(0, path.indexOf(":"));
			// 如果path不存在，但已经存在contextPath说明是脏数据退出并打印异常
			if (zoneZk.exists(contextPath)) {
				logger.error("#### CreateAppcodeDict Path ERROR!! ContextPath Already Existed!! contextPath=" + contextPath + "--- path="
						+ path);
				System.exit(-1);
			} else {

				/// validation of servicePath and poolid relationships
				List<String> appPathDict = getAppPathDict();
				String servicePath = fAppPath;
							
				int sumOfRelations = 0;
				for (String fullPath : appPathDict) {
					String[] pathAndPoolidMapping = fullPath.split(":");
					if(pathAndPoolidMapping.length == 2) {
						String svrPath = pathAndPoolidMapping[0];
						if(servicePath.equals(svrPath)){
							sumOfRelations++; 
							logger.info("Mapping:" + fullPath + " existed on zk"  );
						}
					}
				}
				
				if(sumOfRelations >0){ 
					logger.info("sumOfRelations :"+ sumOfRelations);
					logger.error("#### There are 1:n relationships between servicePath and poolId, pls clean the relationships first.");
					
					System.exit(-1);
					throw new InvalidMappingException("#### There are 1:n relationships between servicePath and poolId, pls clean the relationships first.");
				}
				zoneZk.createPersistent(path, true); // 创建映射关系
			}
		}
		return path;
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
	public static String generatePath(BaseProfile profile, String subPath, ZkClient zoneZk) throws InvalidParamException {
		String value = "";
		if (profile == null && subPath != null)
			throw new InvalidParamException(" Service profile must not null!!!");
		StringBuilder path = new StringBuilder(profile.getRootPath() == null ? "" : profile.getRootPath());
		path.append("/").append(profile.getDomainName()).append("/").append(profile.getServiceAppName()).append("/").append(subPath);
		value = path.toString();
		createPersistentPathIfNotExit(value, zoneZk);
		return value;
	}

	public static String createRollPath(BaseProfile profile, ZkClient zoneZk) throws InvalidParamException {
		return generatePath(profile, InternalConstants.HEDWIG_PAHT_ROLL, zoneZk);
	}

	public static String createRefugeePath(BaseProfile profile, ZkClient zoneZk) throws InvalidParamException {
		return generatePath(profile, InternalConstants.HEDWIG_PAHT_CAMPS + "/" + InternalConstants.HEDWIG_PAHT_REFUGEE, zoneZk);
	}

	public static String createCampPath(BaseProfile profile, String campName, ZkClient zoneZk) throws InvalidParamException {
		return generatePath(profile, InternalConstants.HEDWIG_PAHT_CAMPS + "/" + campName, zoneZk);
	}

	public static String createBaseCampPath(BaseProfile profile, ZkClient zoneZk) {
		String value = "";
		try {
			value = generatePath(profile, InternalConstants.HEDWIG_PAHT_CAMPS, zoneZk);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public static void createPersistentPathIfNotExit(String path, ZkClient zkClient) {
		if (!path.endsWith("/") && !zkClient.exists(path)) {
			zkClient.createPersistent(path, true);
		}
	}

	public static void createEphemeralPathIfNotExit(String path, Object data, ZkClient zkClient) {
		if (!path.endsWith("/") && !zkClient.exists(path)) {
			zkClient.createEphemeral(path, data);
		}
	}

}
