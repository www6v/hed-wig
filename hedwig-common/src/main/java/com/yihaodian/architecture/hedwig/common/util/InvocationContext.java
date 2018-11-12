package com.yihaodian.architecture.hedwig.common.util;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;

public class InvocationContext implements Cloneable, Serializable {
	private static final long serialVersionUID = 2786907400458670384L;
	private static Logger logger = LoggerFactory.getLogger(InvocationContext.class);
	private static Map<String, String> globalKeyList = new ConcurrentHashMap<String, String>();
	private Map<String, Object> globalContext = new ConcurrentHashMap<String, Object>();
	private Map<String, Object> localContext = new ConcurrentHashMap<String, Object>();

	static {
		globalKeyList.put(InternalConstants.HEDWIG_GLOBAL_ID, "");
		globalKeyList.put(InternalConstants.HEDWIG_REQUEST_HOP, "");
		globalKeyList.put(PropKeyConstants.HEDWIG_TOKEN_GRAY, "");
	}

	private Map<String, Object> getGlobalContext() {
		return globalContext;
	}

	private Map<String, Object> getLocalContext() {
		return localContext;
	}

	public void putValue(String key, Object value) {
		if (key != null && value != null) {
			if (value instanceof String) {
				if (value != null) {
					String str = (String) value;
					str = HedwigUtil.limitString(str, InternalConstants.VALUE_LENGTH_LIMIT);
					value = str;
				}
			}
			if (isGlobalKey(key)) {
				getGlobalContext().put(key, value);
			} else {
				getLocalContext().put(key, value);
			}
		}
	}

	public void removeValue(String key) {
		if (key != null) {
			if (isGlobalKey(key)) {
				getGlobalContext().remove(key);
			} else {
				getLocalContext().remove(key);
			}
		}
	}

	public Object getValue(String key, Object defaultObj) {
		Object value = null;
		if (key != null) {
			if (isGlobalKey(key)) {
				value = getGlobalContext().get(key);
			} else {
				value = getLocalContext().get(key);
			}
		}
		value = value == null ? defaultObj : value;
		return value;
	}

	public String getStrValue(String key, String defValue) {
		Object obj = getValue(key, defValue);
		return obj == null ? "" : obj.toString();// 注意：obj.toString()
	}

	private boolean isGlobalKey(String key) {
		boolean result = false;
		if (!HedwigUtil.isBlankString(key)) {
			result = globalKeyList.containsKey(key);
		}
		return result;
	}

	public void cleanLocalContext() {
		if (getLocalContext() != null) {
			getLocalContext().clear();
		}
	}

	public void cleanGlobalContext() {
		if (getLocalContext() != null) {
			getLocalContext().clear();
		}
		if (getGlobalContext() != null) {
			getGlobalContext().clear();
		}
	}

	public int getHopValue() {
		int i = -1;
		String v = getStrValue(InternalConstants.HEDWIG_REQUEST_HOP, "");
		if (HedwigUtil.isBlankString(v)) {
			v = 1 + "";
			putValue(InternalConstants.HEDWIG_REQUEST_HOP, v);
		}
		try {
			i = Integer.valueOf(v);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			i = 1;
			putValue(InternalConstants.HEDWIG_REQUEST_HOP, 1 + "");
		}
		return i;
	}

	public void increaseHopValue() {
		putValue(InternalConstants.HEDWIG_REQUEST_HOP, getHopValue() + 1);
	}

	@Override
	public InvocationContext clone() {
		InvocationContext result = new InvocationContext();
		result.globalContext.putAll(this.getGlobalContext());
		result.localContext.putAll(this.getLocalContext());
		return result;
	}
}