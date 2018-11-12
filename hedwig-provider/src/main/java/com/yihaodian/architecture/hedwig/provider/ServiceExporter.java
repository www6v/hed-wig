/**
 * 
 */
package com.yihaodian.architecture.hedwig.provider;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author root
 *
 */
public interface ServiceExporter extends InitializingBean, DisposableBean {

	public void changeServWeight(int weight);
	
	public void changeServEnable(boolean isEnable);

}
