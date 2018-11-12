package com.yhd.arch.laserbeak.client.meta;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigClientConfigConstants {
    public static final String FLAG_HEDWIG_CLIENT_CONFIG_PATH = "/client_config_hedwig";
    public static final String DEFAULT_ALL_CLIENT_POOL="DEFAULT_ALL_CLIENT_POOL";
    public static final String DEFAULT_ALL_SERVICE_NAME="DEFAULT_ALL_SERVICE_NAME";
    public static final String DEFAULT_ALL_METHOD_NAME="DEFAULT_ALL_METHOD_NAME";
    public static final Integer CONFIG_DATA_STATUS_NOT_AUDITED=0;//未审核
    public static final Integer CONFIG_DATA_STATUS_UNDER_AUDIT=1;//审核中
    public static final Integer CONFIG_DATA_STATUS_AUDITED=2;//已审核

    public static final Integer CONFIG_DATA_APPLY_TYPE_DELETE = -1;//删除
    public static final Integer CONFIG_DATA_APPLY_TYPE_INSERT = 0;//新增
    public static final Integer CONFIG_DATA_APPLY_TYPE_UPDATE = 1;//修改

}
