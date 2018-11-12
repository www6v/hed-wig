package com.yihaodian.architecture.hedwig.common.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.common.KeyUtil;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

public class HedwigContextUtil {
	private static Logger logger = LoggerFactory.getLogger(HedwigContextUtil.class);
	private static ThreadLocal<InvocationContext> invocationContext = new ThreadLocal<InvocationContext>();

	public static InvocationContext getInvocationContext() {
		InvocationContext ic = invocationContext.get();
		if (ic == null) {
			ic = new InvocationContext();
			invocationContext.set(ic);
		}
		return ic;
	}

	public static void setInvocationContext(InvocationContext context) {
		if (context != null) {
			invocationContext.set(context);
		}
	}

	public static String getGlobalId() {
		String id = getString(InternalConstants.HEDWIG_GLOBAL_ID, "");
		if (HedwigUtil.isBlankString(id)) {
			String appName = YccGlobalPropertyConfigurer.getMainPoolId();
			if (HedwigUtil.isBlankString(appName)) {
				appName = System.getProperty("clientAppName", "unknownApp");
			}
			id = KeyUtil.getGlobalId(appName, new Date());
			setAttribute(InternalConstants.HEDWIG_GLOBAL_ID, id);
		}
		return id;
	}

	public static void setGlobalId(String globalId) {
		if (!HedwigUtil.isBlankString(globalId)) {
			setAttribute(InternalConstants.HEDWIG_GLOBAL_ID, globalId);
		}
	}

	public static HedwigGlobalIdVo getGlobalIdVo() {
		HedwigGlobalIdVo resultVo = new HedwigGlobalIdVo();
		String id = getString(InternalConstants.HEDWIG_GLOBAL_ID, "");
		if (HedwigUtil.isBlankString(id)) {
			String appName = YccGlobalPropertyConfigurer.getMainPoolId();
			if (HedwigUtil.isBlankString(appName)) {
				appName = System.getProperty("clientAppName", "unknownApp");
			}
			id = KeyUtil.getGlobalId(appName, new Date());
			setAttribute(InternalConstants.HEDWIG_GLOBAL_ID, id);
			resultVo.setNewCreated(true);
		}
		resultVo.setGlobalId(id);
		return resultVo;
	}

	public static String getRequestId() {
		return getString(InternalConstants.HEDWIG_REQUEST_ID, "0");

	}

	public static void setRequestId(String requestId) {
		if (!HedwigUtil.isBlankString(requestId)) {
			setAttribute(InternalConstants.HEDWIG_REQUEST_ID, requestId);
		}
	}

	public static int getRequestHop() {
		return getInvocationContext().getHopValue();
	}

	public static boolean isVoidMethod() {
		String isVoid = getString(InternalConstants.HEDWIG_METHOD_IS_VOID_KEY, "0");
		if (isVoid != null && isVoid.equals("1")) {
			return true;
		} else {
			return false;
		}
	}

	public static void setVoidMethod(boolean b) {
		if (b) {
			setAttribute(InternalConstants.HEDWIG_METHOD_IS_VOID_KEY, "1");
		} else {
			setAttribute(InternalConstants.HEDWIG_METHOD_IS_VOID_KEY, "0");
		}
	}

	public static Object[] getArguments() {
		Object obj = getAttribute(InternalConstants.HEDWIG_METHOD_ARGUMENTS_KEY, null);
		if (obj != null && obj instanceof Object[]) {
			return (Object[]) obj;
		} else {
			return null;
		}
	}

	public static void setArguments(Object[] params) {
		if (params != null) {
			setAttribute(InternalConstants.HEDWIG_METHOD_ARGUMENTS_KEY, params);
		}
	}

	public static String getTransactionId() {
		return getString(InternalConstants.HEDWIG_TXN_ID, "");
	}

	public static void setTransactionId(String txnId) {
		if (!HedwigUtil.isBlankString(txnId)) {
			setAttribute(InternalConstants.HEDWIG_TXN_ID, txnId);
		}
	}

	public static void setAttribute(String key, Object value) {
		getInvocationContext().putValue(key, value);
	}

	public static Object getAttribute(String key, Object defValue) {
		Object value = defValue;
		try {
			value = getInvocationContext().getValue(key, defValue);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return value;
	}

	public static String getString(String key, String defValue) {
		String v = defValue;
		try {
			v = getInvocationContext().getStrValue(key, defValue);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return v;
	}

	@Deprecated
	public static void clean() {
		cleanGlobal();
	}

	// 清除hedwig内部异步调用线程变量
	@Deprecated
	private static void cleanLocal() {
		getInvocationContext().cleanLocalContext();
	}

	// 清除hedwig外部调用线程变量，比如Web Action
	public static void cleanGlobal() {
		getInvocationContext().cleanGlobalContext();
	}

	// 根据globalId生成情况来清除hedwig线程变量(谁生成谁清理原则）
	public static void cleanGlobal(HedwigGlobalIdVo globalIdVo) {
		if (globalIdVo != null) {
			if (globalIdVo.isNewCreated()) {
				getInvocationContext().cleanGlobalContext();
			} else {
				getInvocationContext().cleanLocalContext();
			}
		}
	}
}
