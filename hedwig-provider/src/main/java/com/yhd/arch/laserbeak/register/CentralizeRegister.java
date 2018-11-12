/**
 * 
 */
package com.yhd.arch.laserbeak.register;

import java.util.Date;
import java.util.List;

import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidMappingException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.hedwig.register.IServiceProviderRegister;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.architecture.zkclient.IZkStateListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * @author root
 * 
 */
public class CentralizeRegister implements IServiceProviderRegister<ServiceProfile> {

	private Logger logger = LoggerFactory.getLogger(CentralizeRegister.class);
	private ZkClient _zkClient = null;
	private String parentPath = "";
	private String childPath = "";
	private boolean isRegisted = false;

	public CentralizeRegister() throws HedwigException {
		_zkClient = ZkUtil.getZkClientInstance();
	}

	@Override
	public void regist(final ServiceProfile profile) throws InvalidParamException, InvalidMappingException {
		createPersistentZnodes(profile);
		createEphemeralZnodes(profile);
		// watch connection status change
		_zkClient.subscribeStateChanges(new IZkStateListener() {

			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				logger.debug(InternalConstants.LOG_PROFIX + "zk connection state change to:" + state.toString());
			}

			@Override
			public void handleNewSession() throws Exception {
				logger.debug(InternalConstants.LOG_PROFIX + "Reconnect to zk!!!");
				createEphemeralZnodes(profile);
			}
		});
		String childPath = ZkUtil.createChildPath(profile);
		// watch profile change
		_zkClient.subscribeDataChanges(childPath, new IZkDataListener() {

			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				if (data != null) {
					ServiceProfile nsp = (ServiceProfile) data;
					profile.update(nsp);
				}
			}

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				logger.debug(InternalConstants.LOG_PROFIX + dataPath + " has deleted!!!");
			}

		});
		// keep on the list
		_zkClient.subscribeChildChanges(parentPath, new IZkChildListener() {

			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				createEphemeralZnodes(profile);
			}
		});

		String baseCampPath = ZkUtil.createBaseCampPath(profile);
		_zkClient.subscribeChildChanges(baseCampPath, new IZkChildListener() {

			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				ZkUtil.createRefugeePath(profile);
			}
		});
		isRegisted = true;
	}

	private void createEphemeralZnodes(ServiceProfile profile) throws InvalidParamException {
		// create service node
		childPath = ZkUtil.createChildPath(profile);
		if (!_zkClient.exists(childPath)) {
			profile.setRegistTime(new Date());
			_zkClient.createEphemeral(childPath, profile);
		}
		// create ip node
		String ipNode = ZkUtil.createRollPath(profile) + "/" + ZkUtil.getProcessDesc(profile);
		if (!_zkClient.exists(ipNode)) {
			_zkClient.createEphemeral(ipNode);
		}
	}

	private void createPersistentZnodes(ServiceProfile profile) throws InvalidParamException, InvalidMappingException {
		String rollPath = "";
		String refugeePath = "";
		// create base path
		parentPath = profile.getParentPath();
		if (!_zkClient.exists(parentPath)) {
			_zkClient.createPersistent(parentPath, true);
		}
		// create roll path
		rollPath = ZkUtil.createRollPath(profile);
		// create refugee path
		refugeePath = ZkUtil.createRefugeePath(profile);
		// create dictionary contextPath:appcode
		String appcode = YccGlobalPropertyConfigurer.getMainPoolId();
		ZkUtil.createAppcodeDict(profile, appcode);
	}

	@Override
	public void updateProfile(ServiceProfile newProfile) {
		if (isRegisted) {
			if (newProfile != null) {
				_zkClient.writeData(childPath, newProfile);
			}
		}

	}

	@Override
	public void unRegist(ServiceProfile profile) {
		String servicePath = profile.getServicePath();
		_zkClient.unsubscribeAll();
		if (_zkClient.exists(servicePath)) {
			_zkClient.delete(servicePath);
		}
		isRegisted = false;

	}

}
