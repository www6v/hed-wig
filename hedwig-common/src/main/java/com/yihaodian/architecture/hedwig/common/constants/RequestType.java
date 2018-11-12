package com.yihaodian.architecture.hedwig.common.constants;

public enum RequestType {
	Direct(0, "direct"), 
	SyncInner(1, "syncInner"), 
	SyncPool(2, "syncPool"), 
	ASync(3, "async"), 
	ASyncReliable(4, "asyncReliable"), 
	OneWay(5, "oneWay");

	private int index;
	private String name;

	RequestType(int index, String name) {
		this.index = index;
		this.name = name;

	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static RequestType getByIndex(int index) {
		if (index < 0)
			return RequestType.SyncPool;
		for (RequestType type : RequestType.values()) {
			if (type.getIndex() == index) {
				return type;
			}
		}
		return RequestType.SyncPool;
	}

	public static RequestType getByName(String name) {
		if (name == null)
			return RequestType.SyncPool;
		for (RequestType type : RequestType.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return RequestType.SyncPool;
	}
}
