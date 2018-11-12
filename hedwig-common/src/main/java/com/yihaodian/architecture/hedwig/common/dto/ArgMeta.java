/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.dto;

import java.io.Serializable;

/**
 * @author root
 * 
 */
public class ArgMeta implements Serializable {

	private static final long serialVersionUID = 2786907400458670384L;
	int pos = 0;
	String type = "";
	int size = 0;
	String subType = "";

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

}
