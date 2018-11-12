/**
 * 
 */
package com.yhd.arch.photon.constants;

/**
 * @author root
 * 
 */
public enum PhotonStatus {

	ENABLE(1), DISABLE(-1), TEMPORARY_DISABLE(0), UNKNOWN(-2);

	private int code;

	private PhotonStatus(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static PhotonStatus getStatusByCode(int code) {
		for (PhotonStatus ps : PhotonStatus.values()) {
			if (ps.getCode() == code) {
				return ps;
			}
		}
		return PhotonStatus.ENABLE;
	}

}
