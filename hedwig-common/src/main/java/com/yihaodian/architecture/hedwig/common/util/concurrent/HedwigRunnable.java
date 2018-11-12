package com.yihaodian.architecture.hedwig.common.util.concurrent;

import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.common.util.InvocationContext;

/**
 * @author Hikin Yao
 * @version 1.0
 */
class HedwigRunnable implements Runnable {
    private InvocationContext invocationContext;
    private Runnable task;

    public HedwigRunnable(InvocationContext invocationContext, Runnable task) {
        if (invocationContext != null) {
            this.invocationContext = invocationContext.clone();//注意对象复制
        }
        this.task = task;
    }

    @Override
    public void run() {
        try {
            HedwigContextUtil.setInvocationContext(this.invocationContext);
            this.task.run();
        }finally {
            HedwigContextUtil.cleanGlobal();
        }
    }
}
