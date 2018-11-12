package com.yihaodian.architecture.hedwig.common.util.concurrent;

import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.common.util.InvocationContext;

import java.util.concurrent.Callable;

/**
 * @author Hikin Yao
 * @version 1.0
 */
class HedwigCallable<V> implements Callable<V> {
    private InvocationContext invocationContext;
    private Callable<V> task;

    public HedwigCallable(InvocationContext invocationContext, Callable<V> task) {
        if (invocationContext != null) {
            this.invocationContext = invocationContext.clone();//注意对象复制
        }
        this.task = task;
    }

    @Override
    public V call() throws Exception {
        try {
            HedwigContextUtil.setInvocationContext(this.invocationContext);
            return this.task.call();
        } finally {
            HedwigContextUtil.cleanGlobal();
        }
    }
}
