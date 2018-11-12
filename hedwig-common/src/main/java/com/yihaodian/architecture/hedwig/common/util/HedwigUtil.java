/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

import java.lang.reflect.Field;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.codec.binary.Base64;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.BaseProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

/**
 * @author root
 * 
 */
public class HedwigUtil {

	public static String getRawClassName(Object object) {
		if (object != null) {
			if (AopUtils.isAopProxy(object)) {
				String v = AopUtils.getTargetClass(object).getSimpleName();
				if (v.contains("$Proxy") || v.contains("$$")) {
					try {
						Object o = ((Advised) object).getTargetSource().getTarget();
						return getRawClassName(o);
					} catch (Exception e) {
					}
				} else {
					return v;
				}
			} else if (AopUtils.isCglibProxy(object)) {
				return AopUtils.getTargetClass(object).getSimpleName();
			} else {
				return object.getClass().getSimpleName();
			}
		}
		return "UnknowClass";
	}

	public static String generateKey(BaseProfile profile) {
		StringBuilder sb = new StringBuilder(profile.getServiceAppName());
		sb.append("_").append(profile.getServiceName()).append("_").append(profile.getServiceVersion());
		return sb.toString();
	}

	public static String getChildFullPath(String parentPath, String shortChildPath) {
		return parentPath + "/" + shortChildPath;
	}

	public static String getChildShortPath(String fullPath) {
		return fullPath.substring(fullPath.lastIndexOf("/") + 1);
	}

	public static String list2String(List<String> list) {
		StringBuilder sb = new StringBuilder("[");
		if (list != null) {
			for (String o : list) {
				if (o != null) {
					sb.append(o.toString()).append(",");
				}
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public static boolean isBlankString(String value) {
		return value == null || "".equals(value.trim());
	}

	public static boolean isEmptyList(List list) {
		return list == null || list.size() == 0;
	}

	public static String generateServiceUrl(ServiceProfile sp) {
		String prefix = sp.getProtocolPrefix();
		StringBuilder sb = new StringBuilder();
		if (prefix.equals(InternalConstants.PROTOCOL_PROFIX_HTTP)) {
			sb.append(prefix).append("://").append(sp.getHostIp()).append(":").append(sp.getPort()).append("/");
			if (sp.isAssembleAppName()) {
				sb.append(sp.getServiceAppName()).append("/");
			}
			sb.append(sp.getUrlPattern()).append("/").append(sp.getServiceName());
		} else if (prefix.equals(InternalConstants.PROTOCOL_PROFIX_AKKATCP)) {
			sb.append(prefix).append("://").append(sp.getHostIp()).append(":").append(sp.getPort());
		}

		return sb.toString();
	}

	public static int parseString2Int(String value, int defaultValue) {
		int i = defaultValue;
		try {
			i = Integer.valueOf(value);
		} catch (Exception e) {
		}
		return i;
	}

	public static String generateHandlerName(Class clz) {
		return clz.getName();
	}

	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}

	public static long getCurrentNanoTime() {
		return System.nanoTime();
	}

	public static String getHostFromUrl(String url) {
		String value = "";
		if (!isBlankString(url)) {
			String[] arr = url.split("/", 6);
			value = arr[2];
		}
		return value;
	}

	public static String getMethodName(MethodInvocation invocation) {
		String value = "unknowMethod";
		if (invocation != null) {
			value = invocation.getMethod().getName();
		}
		return value;
	}

	public static String getClassName(MethodInvocation invocation) {
		String value = "unKnowClass";
		if (invocation != null) {
			value = invocation.getMethod().getDeclaringClass().getSimpleName();
		}
		return value;
	}

	public static String getShortClassName(String clazzName) {
		String value = clazzName;
		if (clazzName != null) {
			String[] arr = clazzName.split("\\.");
			if (arr != null && arr.length > 0) {
				value = arr[(arr.length - 1)];
			}
		}
		return value;
	}

	public static String getErrorMsg(Throwable ex) {
		String value = "";
		if (ex != null) {
			value = ex.getMessage();
			if (isBlankString(value)) {
				value = ex.getClass().getName();
			}
		}
		return value;
	}

	public static String filterString(String value) {
		return value.replaceAll("/", "_");
	}

	public static String replaceSlash(String value) {
		String v = "unknowPoolName";
		if (!isBlankString(value)) {
			v = value.replaceAll("/", "#");
		}
		return v;
	}

	public static String limitString(String value, int limit) {
		if (value != null && limit > 0 && value.length() > limit) {
			value = value.substring(0, limit);
		}
		return value;
	}

	public static Object getPrivateField(Object source, String filedName) {
		Object o = null;
		if (source != null) {
			Class clazz = source.getClass();
			try {
				Field field = clazz.getDeclaredField(filedName);
				field.setAccessible(true);
				o = field.get(source);
			} catch (Exception e) {

			}
		}
		return o;
	}

	public static String getPrivateStringField(Object source, String filedName) {
		Object o = getPrivateField(source, filedName);
		return o == null ? null : (String) o;
	}

	public static String genAuthorization(String user, String passwd) {
		String enStr = user + ":" + passwd;
		return "Basic " + new String(Base64.encodeBase64(enStr.getBytes()));
	}

	public static String genPoolFlagsPath(String poolName) {
		StringBuilder sb = new StringBuilder();
		sb.append(InternalConstants.BASE_ROOT_FLAGS).append("/").append(replaceSlash(poolName));
		return sb.toString();
	}

}
