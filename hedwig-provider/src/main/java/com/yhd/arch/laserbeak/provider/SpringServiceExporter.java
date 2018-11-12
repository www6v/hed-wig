/**
 * 
 */
package com.yhd.arch.laserbeak.provider;

import org.springframework.beans.factory.BeanNameAware;

import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

/**
 * @author root
 *
 */
public class SpringServiceExporter extends BaseServiceExporter implements BeanNameAware {

	public SpringServiceExporter() {
		super();
	}

	public SpringServiceExporter(ServiceProfile profile, Class serviceInterface) throws Exception {
		super(profile, serviceInterface);
		this.afterPropertiesSet();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
	}

	@Override
	public void destroy() throws Exception {
		super.destroy();
	}

	public void setAppProfile(AppMeta appMeta) {
		this.appMeta = appMeta;
	}

	public void setDefaultStatus(boolean isEnable) {
		this.initStart = isEnable;
	}

	@Override
	public void setBeanName(String name) {
		if (name.startsWith("/")) {
			this.serviceName = name.replaceFirst("/", "");
		} else {
			this.serviceName = name;
		}
	}
}
