package com.yhd.arch.laserbeak;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import com.yhd.arch.laserbeak.client.LogedAckActor;
import com.yhd.arch.photon.core.PackagedMessage;
import com.yhd.arch.photon.core.RemoteResponse;
import com.yhd.arch.photon.invoker.DefaultRequest;
import com.yhd.arch.photon.plugin.IThrottler;
import com.yhd.arch.photon.plugin.MethodActorAtomicThrottler;
import com.yhd.arch.photon.plugin.MethodActorThrottler;
import scala.concurrent.Promise;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by root on 8/11/15.
 */
public class SuperviserActor extends UntypedActor {
    IThrottler methodThrottler;
    public SuperviserActor() {
        methodThrottler =  new MethodActorAtomicThrottler(this.getContext());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        DefaultRequest req = new DefaultRequest();
        req.setThrottleEnable(true);
        req.setThrottleCapacity(1000);
        Promise<RemoteResponse> promise = Futures.promise();
        PackagedMessage pm = new PackagedMessage(req,promise);
        long start = System.currentTimeMillis();
        if(!methodThrottler.isThrottle(req)){
            System.out.println("throttl cost:"+(System.currentTimeMillis()-start));
            start = System.currentTimeMillis();
            getContext().actorOf(Props.create(SuperviserActor.class));
            System.out.println("create cost:" + (System.currentTimeMillis() - start));
        }

    }
}
