package com.yihaodian.architecture.hedwig.hessian.client;

import java.io.Serializable;
import java.util.Map;

public class Parameters implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 819114208298643096L;
	String strParam;
	Integer intParam;
	Map<Object, Object> map;

	public String getStrParam() {
		return strParam;
	}

	public void setStrParam(String strParam) {
		this.strParam = strParam;
	}

	public Integer getIntParam() {
		return intParam;
	}

	public void setIntParam(Integer intParam) {
		this.intParam = intParam;
	}

	public Map<Object, Object> getMap() {
		return map;
	}

	public void setMap(Map<Object, Object> map) {
		this.map = map;
	}

	@Override
	public String toString() {
		return "Parameters [strParam=" + strParam + ", intParam=" + intParam + ", map=" + map.toString() + "]";
	}

}
