package com.yihaodian.architecture.hedwig.register;

import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.InvalidMappingException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;

/**
 * @author root
 * 
 */
public interface IServiceProviderRegister<P> {

	public void regist(P profile) throws InvalidParamException, InvalidMappingException;

	public void updateProfile(P newProfile);

	public void unRegist(P profile);
}
