package com.yhd.arch.laserbeak;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.yhd.arch.laserbeak.client.LogedAckActor;
import com.yhd.arch.photon.plugin.IThrottler;
import com.yhd.arch.photon.plugin.MethodActorThrottler;
import com.yhd.arch.photon.util.ClientSystem;
import junit.framework.TestCase;

/**
 * Created by root on 8/11/15.
 */
public class TestCreateCost extends TestCase {
    private static String envPath = "/Users/root/Applications/work/envConfig";
    public void testCost() throws InterruptedException {
        System.setProperty("global.config.path", envPath);
        ClientSystem client = ClientSystem.getInstance();
        ActorRef ref =client.getActorSystem().actorOf(Props.create(SuperviserActor.class));
        for(int i=0;i<1000;i++){
            ref.tell("1",ActorRef.noSender());
        }
        Thread.sleep(1000000);
    }
}
