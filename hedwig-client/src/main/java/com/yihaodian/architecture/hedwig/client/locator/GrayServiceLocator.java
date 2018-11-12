/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.locator;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.yihaodian.architecture.hedwig.balancer.LoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.balancer.special.DynamicLoadBalancer;
import com.yihaodian.architecture.hedwig.balancer.special.GrayInfo;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.IZkDataListener;

/**
 * @author root
 * 
 */
public class GrayServiceLocator extends GroupServiceLocator {

	private static Logger logger = LoggerFactory.getLogger(GrayServiceLocator.class);
	private String grayPath = "";
	private String flagPath = "";
	private DynamicLoadBalancer<ServiceProfile, String> grayBalancer;
	private IZkDataListener grayNodeListener;
	private IZkChildListener dictChangeListener;

	public GrayServiceLocator(final ClientProfile clientProfile) throws HedwigException {
		super(clientProfile);
		List<String> dict = HedwigClientUtil.getAppPathDict();
		String poolName = HedwigClientUtil.getServPoolName(clientProfile, dict);
		if (!HedwigUtil.isBlankString(poolName)) {
			initGrayServiceLocator(poolName);
		} else {
			logger.error("Pool:" + poolName
					+ " not suport gray publish, u can fix this problem by upgrade hedwig-provider to 0.1.3 or later version"
			+", clientProfile="+ JSON.toJSONString(clientProfile));
			dictChangeListener = new IZkChildListener() {

				@Override
				public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
					String poolName = HedwigClientUtil.getServPoolName(clientProfile, currentChilds);
					if (!HedwigUtil.isBlankString(poolName)) {
						initGrayServiceLocator(poolName);
					}
				}
			};
			_zkClient.subscribeChildChanges(InternalConstants.HEDWIG_PAHT_APPDICT, dictChangeListener);
		}

	}

	private void initGrayServiceLocator(String poolName) {
		flagPath = HedwigUtil.genPoolFlagsPath(poolName);
		grayPath = flagPath + InternalConstants.FLAG_GRAY;
		try {
			grayBalancer = (DynamicLoadBalancer<ServiceProfile, String>) balancer;
			byte[] rawData = null;
			if (_zkClient.exists(grayPath)) {
				rawData = _zkClient.readRawData(grayPath, true);
			}
			parseGrayNode(rawData);
			initGrayListener();
			observeGray();
			if (dictChangeListener != null) {
				_zkClient.unsubscribeChildChanges(InternalConstants.HEDWIG_PAHT_APPDICT, dictChangeListener);
			}
		} catch (Exception e) {
			logger.error("Balancer not support gray", e);
		}
	}

	private void parseGrayNode(byte[] rawData) {
		if (logger.isDebugEnabled()) {
			logger.debug("Parse gray data!!!");
		}
		String resultPath = grayPath + InternalConstants.FLAG_GRAY_RESULT;
		if (!_zkClient.exists(resultPath)) {
			_zkClient.createPersistent(resultPath, true);
		}
		if (rawData != null && rawData.length > 0) {
			GrayInfo gi = cast2GrayInfo(rawData);
			if (gi != null) {
				if (gi.getValidateCode() == 0) {
					grayBalancer.setSpecialInfo(gi);
					grayBalancer.updateProfiles(profileContainer.values());
				}

				String result = generateResult(gi);
				if (!_zkClient.exists(resultPath)) {
					_zkClient.createPersistent(resultPath);
					_zkClient.writeRawData(resultPath, result);
				} else {
					byte[] barr = _zkClient.readRawData(resultPath, false);
					String rr = new String(barr);
					if (!rr.contains(gi.getPublishId())) {
						_zkClient.writeRawData(resultPath, result);
					}
				}
			} else {
				logger.error("There is no gray data or the data can not be parsed by hedwig!!!");
				_zkClient.writeRawData(resultPath, rawData);
			}
		} else {
			_zkClient.writeRawData(resultPath, rawData);
			grayBalancer.setSpecialInfo(null);
			grayBalancer.updateProfiles(profileContainer.values());
		}
	}

	private String generateResult(GrayInfo gi) {
		StringBuilder sb = new StringBuilder(gi.getPublishId());
		int code = gi.getValidateCode();
		sb.append(";").append(code);
		if (code != 0) {
			sb.append(";").append(gi.toString());
		}
		sb.append(";");
		return sb.toString();
	}

	private GrayInfo cast2GrayInfo(byte[] rawData) {
		GrayInfo info = null;
		if (rawData != null && rawData.length > 0) {
			String str = new String(rawData);
			if (str.contains(";")) {
				info = new GrayInfo();
				String[] sArr = str.split(";");
				if (sArr != null && sArr.length > 1) {
					info.setPublishId(sArr[0]);
					info.setRange(sArr[1]);
					info.setGraySet(sArr[2]);
				}
			}
		}
		return info;
	}

	private void initGrayListener() {

		grayNodeListener = new IZkDataListener() {

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {

			}

			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Trigger gray data watcher!!!");
					}
					byte[] ba = (byte[]) data;
					parseGrayNode(ba);
				} catch (Exception e) {
					logger.error("Can't cast data to byte[],"+dataPath+"data:" + data, e);
				}
			}
		};
	}

	private void observeGray() {
		if (!_zkClient.exists(grayPath)) {
			_zkClient.createPersistent(grayPath, true);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Watch gray data change,path" + grayPath);
		}
		_zkClient.subscribeDataChanges(grayPath, grayNodeListener);
	}

	@Override
	public LoadBalancer<ServiceProfile> getLoadBalancer() {
		return this.grayBalancer;
	}
}
