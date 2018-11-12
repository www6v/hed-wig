/**
 * 
 */
package com.yihaodian.architecture.hedwig.register;

import java.util.Date;
import java.util.List;

import com.yhd.arch.zone.ZoneContainer;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidMappingException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.StringUtils;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.architecture.zkclient.IZkStateListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * @author root
 * 
 */
public class ServiceProviderZkRegister implements IServiceProviderRegister<ServiceProfile> {

	private Logger logger = LoggerFactory.getLogger(ServiceProviderZkRegister.class);
	private ZkClient _zkClient = null;
	private String parentPath = "";
	private String childPath = "";
	private boolean isRegisted = false;

	public ServiceProviderZkRegister() throws HedwigException {
		_zkClient = ZkUtil.getZkClientInstance();
	}

	@Override
	public void regist(final ServiceProfile profile) throws InvalidParamException, InvalidMappingException {
		setProfileIdc(profile);
		createPersistentZnodes(profile);
		createEphemeralZnodes(profile);
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
				logger.debug(InternalConstants.LOG_PROFIX + dataPath + "has deleted!!!");
			}

		});

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

	private void setProfileIdc(ServiceProfile profile){
		ZoneContainer zoneContainer= ZoneContainer.getInstance();
		String lzone=zoneContainer.getLocalZoneName();
		String lidc=zoneContainer.getIdcContainer().getLocalIdc();
		String level=zoneContainer.getLevel().getCode();
		profile.setPubZone(lzone);
		profile.setPubIdc(lidc);
		profile.setProviderLevel(level);
	}

	private void createEphemeralZnodes(ServiceProfile profile) throws InvalidParamException {
		// create service node
		childPath = ZkUtil.createChildPath(profile);
		if (!_zkClient.exists(childPath)) {
			profile.setRegistTime(new Date());
			try {
				_zkClient.createEphemeral(childPath, profile);
			} catch (Exception e) {
				if (!(e instanceof NodeExistsException)) {
					throw new InvalidParamException(e.getCause());
				}
			}
		}
		// create ip node
		String ipNode = ZkUtil.createRollPath(profile) + "/" + ZkUtil.getProcessDesc(profile);
		if (!_zkClient.exists(ipNode)) {
			try {
				_zkClient.createEphemeral(ipNode);
			} catch (Exception e) {
				if (!(e instanceof NodeExistsException)) {
					throw new InvalidParamException(e.getCause());
				}
			}
		}
	}

	private void createPersistentZnodes(ServiceProfile profile) throws InvalidParamException, InvalidMappingException {
		String rollPath = "";
		String refugeePath = "";
		// create base path
		parentPath = profile.getParentPath();
		if (!_zkClient.exists(parentPath)) {
			try {
				_zkClient.createPersistent(parentPath, true);
			} catch (Exception e) {
				if (!(e instanceof NodeExistsException)) {
					throw new InvalidParamException(e.getCause());
				}
			}
		}
		// create roll path
		rollPath = ZkUtil.createRollPath(profile);
		// create refugee path
		refugeePath = ZkUtil.createRefugeePath(profile);
		// create dictionary contextPath:appcode
		String appcode = YccGlobalPropertyConfigurer.getMainPoolId();
		if (StringUtils.isPoolid(appcode)) {
			ZkUtil.createAppcodeDict(profile, appcode);
		} else {
			logger.error("FATAL_ERROR:Poolid should match pattern xxxx/xxxx," + appcode + " is invalidate!!!!!!!!!");
		}

	}

	@Override
	public void updateProfile(ServiceProfile newProfile) {
		if (isRegisted) {
			if (newProfile != null) {
				if(_zkClient.exists(childPath)){
					_zkClient.writeData(childPath, newProfile);
				}else{
					int i=0;
					while(i<5){
						try {
							Thread.currentThread().sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						logger.error("update profile failed, path :"+childPath+" not exists,going to retry "+i++);
						if(_zkClient.exists(childPath)) {
							_zkClient.writeData(childPath, newProfile);
							break;
						}
					}
				}
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
