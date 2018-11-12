package com.yihaodian.architecture.hedwig.common.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.util.concurrent.HedwigScheduledThreadPoolExecutor;
import com.yihaodian.architecture.hedwig.common.util.concurrent.HedwigThreadPoolExecutor;

/**
 * @author root
 * 
 */
public class HedwigExecutors {

	public static ThreadPoolExecutor newCachedThreadPool(int side) {
		ProperitesContainer pc = getPropContainer(side);
		int coreSize = pc.getIntProperty(PropKeyConstants.HEDWIG_POOL_CORESIZE, InternalConstants.DEFAULT_POOL_CORESIZE);
		int maxSize = pc.getIntProperty(PropKeyConstants.HEDWIG_POOL_MAXSIZE, InternalConstants.DEFAULT_POOL_MAXSIZE);
		long idleTime = pc.getLongProperty(PropKeyConstants.HEDWIG_POOL_IDLETIME, InternalConstants.DEFAULT_POOL_IDLETIME);
		int capacity = pc.getIntProperty(PropKeyConstants.HEDWIG_POOL_QUEUESIZE, InternalConstants.DEFAULT_POOL_QUEUESIZE);
		BlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<Runnable>(capacity);
		ThreadPoolExecutor tpe = new HedwigThreadPoolExecutor(coreSize, maxSize, idleTime, TimeUnit.SECONDS, eventQueue,
				new HedwigThreadFactory(), new HedwigDiscardOldestPolicy<Object>());
		return tpe;
	}

	public static ScheduledThreadPoolExecutor newSchedulerThreadPool(int side) {
		ProperitesContainer pc = getPropContainer(side);
		int coreSize = pc
				.getIntProperty(PropKeyConstants.HEDWIG_SCHEDULER_POOL_CORESIZE, InternalConstants.DEFAULT_SCHEDULER_POOL_CORESIZE);
		int maxSize = pc.getIntProperty(PropKeyConstants.HEDWIG_SCHEDULER_POOL_MAXSIZE, InternalConstants.DEFAULT_SCHEDULER_POOL_MAXSIZE);
		long idleTime = pc.getIntProperty(PropKeyConstants.HEDWIG_SCHEDULER_POOL_IDLETIME,
				InternalConstants.DEFAULT_SCHEDULER_POOL_IDLETIME);
		ScheduledThreadPoolExecutor tpe = new HedwigScheduledThreadPoolExecutor(coreSize, new HedwigThreadFactory(),
				new HedwigDiscardOldestPolicy<Object>());
		tpe.setMaximumPoolSize(maxSize);
		tpe.setKeepAliveTime(idleTime, TimeUnit.SECONDS);

		return tpe;
	}

	public static ProperitesContainer getPropContainer(int side) {
		ProperitesContainer pc = null;
		switch (side) {
		case InternalConstants.HEDWIG_PROVIDER:
			pc = ProperitesContainer.provider();
			break;
		case InternalConstants.HEDWIG_CLIENT:

		default:
			pc = ProperitesContainer.client();
			break;
		}
		return pc;
	}

}
