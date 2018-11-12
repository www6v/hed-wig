package com.yhd.arch.laserbeak.provider;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.container.RootContainer;
import com.yhd.arch.photon.common.ProfileUtil;

public class ExporterContainer {

	private static Logger logger = LoggerFactory.getLogger(ExporterContainer.class);
	private AppMeta appMeta;
	private static ExporterContainer container = new ExporterContainer();
	private Map<String, BaseServiceExporter> _serviceExporterMap = new HashMap<String, BaseServiceExporter>();

	public static ExporterContainer getInstance() {
		return container;
	}

	private ExporterContainer() {
		super();
	}

	public AppMeta getAppMeta() {
		return appMeta;
	}

	public void setAppMeta(AppMeta meta) {
		if (this.appMeta == null) {
			this.appMeta = meta;
		}
	}

	public void putServiceExporter(String sName, String profileUUId, BaseServiceExporter sExporter) {
		String key = ProfileUtil.buildProfileKey(sName, profileUUId);
		if (!_serviceExporterMap.containsKey(key)) {
			_serviceExporterMap.put(key, sExporter);
			RootContainer.getInstance().putServiceProfile(sName, sExporter.getProfile());
		} else {
			throw new RuntimeException(key + " is already existed!!");
		}
	}

	public void setServiceEnable(String sName, boolean isEnable) {
		BaseServiceExporter exporter = _serviceExporterMap.get(sName);
		if (exporter != null) {
			exporter.changeServEnable(isEnable);
		} else {
			logger.error(sName + " is not existed");
		}
	}

	public void setAllServiceEnable(boolean isEnable) {
		for (BaseServiceExporter exporter : _serviceExporterMap.values()) {
			if (exporter != null) {
				exporter.changeServEnable(isEnable);
			}
		}
	}

	public void setServiceWeight(String sName, int newWeight) {
		BaseServiceExporter exporter = _serviceExporterMap.get(sName);
		if (exporter != null) {
			exporter.changeServWeight(newWeight);
		} else {
			logger.error(sName + "is not existed");
		}
	}

	public void setAllServiceWeight(int newWeight) {
		for (BaseServiceExporter exporter : _serviceExporterMap.values()) {
			if (exporter != null) {
				exporter.changeServWeight(newWeight);
			}
		}
	}

	public void removeServiceExporter(String sName, String profileUUId) {
		String key = ProfileUtil.buildProfileKey(sName, profileUUId);
		_serviceExporterMap.remove(key);
	}

	public boolean hasServiceExporter() {
		return _serviceExporterMap.size() > 0;
	}
}
