package com.yhd.arch.photon.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;

public class PhotonBeanFactory {

	private static PhotonBeanFactory beanFactory = new PhotonBeanFactory();
	private static Map<String, Object> beanMap = new HashMap<String, Object>();

	private PhotonBeanFactory() {

	}

	public static PhotonBeanFactory getInstance() {
		return beanFactory;
	}

	/**
	 * Guarantee single instance for className.
	 * 
	 * @param className
	 * @return
	 */
	public synchronized Object getSingleton(String clazzName, Object... parameters) {
		Object o = null;
		if (!HedwigUtil.isBlankString(clazzName)) {
			if (!beanMap.containsKey(clazzName)) {
				try {
					Class clazz = Class.forName(clazzName);
					Constructor c = clazz.getDeclaredConstructor(getParamTypes(parameters));
					if (c.isAccessible()) {
						o = c.newInstance(parameters);
					} else {
						Method m = clazz.getMethod("getInstance");
						o = m.invoke(null);
					}
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (o != null) {
					beanMap.put(clazzName, o);
				}
			} else {
				o = beanMap.get(clazzName);
			}
		}
		return o;
	}

	private Class[] getParamTypes(Object[] parameters) {
		Class[] paramTypes = null;
		if (parameters != null && parameters.length > 0) {
			int len = parameters.length;
			paramTypes = new Class[len];
			for (int i = 0; i < len; i++) {
				if (parameters[i] != null) {
					Class clz = parameters[i].getClass();
					if (!parameters[i].getClass().isPrimitive()) {
						Class[] ifaces = clz.getInterfaces();
						if (ifaces != null && ifaces.length > 0) {
							clz = ifaces[0];
						}
					}
					paramTypes[i] = clz;

				} else {
					paramTypes[i] = null;
				}
			}
		}
		return paramTypes;
	}

	/**
	 * Guarantee return new instance for each invoke.
	 * 
	 * @param className
	 * @return
	 */
	public Object newInstance(String clazzName, Object... parameters) {
		Object o = null;
		if (!HedwigUtil.isBlankString(clazzName)) {
			try {
				Class clazz = Class.forName(clazzName);
				Constructor c = clazz.getDeclaredConstructor(getParamTypes(parameters));
				if (c.isAccessible()) {
					o = c.newInstance(parameters);
				} else {
					c.setAccessible(true);
					o = c.newInstance(parameters);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return o;
	}

}
