/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;

/**
 * @author root
 *
 */
public class HedwigCallerRunPolicy implements RejectedExecutionHandler {

	private Logger logger = LoggerFactory.getLogger(HedwigAbortPolicy.class);

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		if (logger.isDebugEnabled()) {
			logger.debug(InternalConstants.ENGINE_LOG_PROFIX + "Hedwig Executor queue size:" + executor.getQueue().size());
		}
		if (!executor.isShutdown()) {
			r.run();
		}

	}

}
