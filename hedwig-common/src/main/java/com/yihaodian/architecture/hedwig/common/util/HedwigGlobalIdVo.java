package com.yihaodian.architecture.hedwig.common.util;

import java.io.Serializable;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigGlobalIdVo implements Serializable {
	private static final long serialVersionUID = -3449736799079134897L;
	/**
	 * hedwig全局ID
	 */
	private String globalId;
	/**
	 * 是否是新生成的
	 */
	private boolean isNewCreated = false;

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}

	public boolean isNewCreated() {
		return isNewCreated;
	}

	public void setNewCreated(boolean isNewCreated) {
		this.isNewCreated = isNewCreated;
	}
}
