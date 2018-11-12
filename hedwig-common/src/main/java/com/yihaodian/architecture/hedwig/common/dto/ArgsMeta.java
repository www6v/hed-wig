/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author root
 * 
 */
public class ArgsMeta implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8950704891888406880L;

	List<ArgMeta> argsMeta = new ArrayList<ArgMeta>();

	public List<ArgMeta> getArgsMeta() {
		return argsMeta;
	}

	public void setArgsMeta(List<ArgMeta> argsMeta) {
		this.argsMeta = argsMeta;
	}

	public void addArgMeta(ArgMeta argMeta) {
		this.argsMeta.add(argMeta);
	}

}
