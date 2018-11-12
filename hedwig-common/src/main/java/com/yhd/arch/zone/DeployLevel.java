package com.yhd.arch.zone;

import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;

/**
 * Created by root on 07/03/2017.
 */
public enum DeployLevel {

    ZONE("zone"),IDC("idc"),SITE("site");

    private String code;

    private DeployLevel(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DeployLevel getByCode(String c) {
        DeployLevel level = DeployLevel.ZONE;
        if(!HedwigUtil.isBlankString(c)){
            for (DeployLevel d : DeployLevel.values()) {
                if (c.equalsIgnoreCase(d.getCode())) {
                    level = d;
                    break;
                }
            }
        }
        return level;
    }
}
