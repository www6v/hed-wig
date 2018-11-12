/**
 *
 */
package com.yhd.arch.laserbeak.register;

import java.util.Date;
import java.util.List;

import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.laserbeak.common.util.ZoneZkUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidMappingException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.architecture.zkclient.IZkStateListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * @author root
 */
public class ZoneProfileKeeper {

    private static Logger logger = LoggerFactory.getLogger(ZoneProfileKeeper.class);
    private ZkClient _zkClient;
    private ServiceProfile _profile;
    private String parentPath;
    private String childPath;
    private String rollNode;
    private IZkStateListener stateListener;
    private IZkChildListener profileExistListener;
    private IZkDataListener profileDataListener;

    public ZoneProfileKeeper(ZkClient zkclient, ServiceProfile profile) throws InvalidParamException,InvalidMappingException {
        super();
        this._zkClient = zkclient;
        this._profile = profile;
        this.childPath = ZkUtil.createChildPath(profile);
        initPersistentPath();
        initListener();

    }

    private void initPersistentPath() throws InvalidParamException, InvalidMappingException {
        // create base path
        this.parentPath = _profile.getParentPath();
        ZoneZkUtil.createPersistentPathIfNotExit(parentPath, _zkClient);
        // create roll path
        String rollPath = ZoneZkUtil.createRollPath(_profile, _zkClient);
        // create refugee path
        ZoneZkUtil.createRefugeePath(_profile, _zkClient);
        // create dictionary contextPath:appcode
        String appcode = YccGlobalPropertyConfigurer.getMainPoolId();

		ZoneZkUtil.createAppcodeDict(_profile, appcode, _zkClient);

        String processNode = ZkUtil.getProcessDesc(_profile);
        this.rollNode = rollPath + "/" + processNode;
    }

    private void initListener() {
        this.stateListener = new IZkStateListener() {

            @Override
            public void handleStateChanged(KeeperState state) throws Exception {
                logger.debug(InternalConstants.LOG_PROFIX + "zk connection state change to:" + state.toString());
            }

            @Override
            public void handleNewSession() throws Exception {
                logger.debug(InternalConstants.LOG_PROFIX + "Reconnect to zk!!!");
                createEphemeralPath();
            }

        };
        this.profileExistListener = new IZkChildListener() {

            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                createEphemeralPath();
            }

        };
        this.profileDataListener = new IZkDataListener() {

            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                if (data != null) {
                    ServiceProfile nsp = (ServiceProfile) data;
                    _profile.update(nsp);

                }
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                logger.debug(InternalConstants.LOG_PROFIX + dataPath + " has deleted!!!");
            }

        };
    }

    public void createEphemeralPath() {
        _profile.setRegistTime(new Date());
        ZoneZkUtil.createEphemeralPathIfNotExit(this.childPath, _profile, _zkClient);
        // create ip node
        ZoneZkUtil.createEphemeralPathIfNotExit(this.rollNode, null, _zkClient);
        if (logger.isDebugEnabled()) {
            logger.debug("###ZoneProfileKeeper.createEphemeralPath=" + this.childPath + "####");
        }
    }

    public void regist() {
        cleanEphemeralPath();
        createEphemeralPath();
        _zkClient.subscribeStateChanges(stateListener);
        _zkClient.subscribeChildChanges(parentPath, profileExistListener);
        _zkClient.subscribeDataChanges(childPath, profileDataListener);
        if (logger.isDebugEnabled()) {
            logger.debug("###ZoneProfileKeeper.regist() childPath=" + this.childPath + ",zkClient="
                    + _zkClient.getConnection().getServers() + "####");
        }
    }

    private void cleanEphemeralPath() {
        if(_zkClient.exists(this.childPath)){
            _zkClient.delete(this.childPath);
        }
        if(_zkClient.exists(this.rollNode)){
            _zkClient.delete(this.rollNode);
        }
    }

    public void updateProfile(ServiceProfile newProfile) {
        this._profile = newProfile;
        if (_zkClient.exists(childPath)) {
            _zkClient.writeData(childPath, _profile);
            if (logger.isDebugEnabled()) {
                logger.debug("###ZoneProfileKeeper.updateProfile() childPath=" + this.childPath + ",zkClient="
                        + _zkClient.getConnection().getServers() + "####");
            }
        } else {
            logger.error("zk node not exist:" + childPath);
        }
    }

    public void unRegist() {
        String sl = _zkClient.getConnection() == null ? "" : _zkClient.getConnection().getServers();
        _zkClient.unsubscribeChildChanges(this.parentPath, profileExistListener);
        _zkClient.unsubscribeDataChanges(this.childPath, profileDataListener);
        _zkClient.unsubscribeStateChanges(stateListener);
        if (_zkClient.exists(this.childPath)) {
            _zkClient.delete(this.childPath);
        }
        if (_zkClient.exists(this.rollNode)) {
            _zkClient.delete(this.rollNode);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("###ZoneProfileKeeper.unRegist() childPath=" + this.childPath + ",zkClient=" + sl + "####");
        }
    }

}
