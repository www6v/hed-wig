package btrace;/* BTrace Script Template */
import com.sun.btrace.annotations.*;
import com.yhd.arch.laserbeak.client.LogedAckActor;
import static com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.aggregation.*;
import com.yhd.arch.photon.emitter.AckActor;

import java.lang.System;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@BTrace(unsafe=true)
public class ActorCostMonitor {
    private static Map<String,Long> totalMap = new HashMap<String, Long>();
    private static Map<String,Long> onReceiveMap = new HashMap<String, Long>();
    private static Map<String,Aggregation> aggMap = new HashMap<String, Aggregation>();
    private static Aggregation onReceiveDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static Aggregation totalDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static String[]  whiteList = new String[]{"AkkaConnectorActor","endpointWriter","endpointReader","Photon","WorkerActor"};
    //--------------------------------------------------------------------------


    @OnMethod(
            clazz="com.yhd.arch.laserbeak.client.LogedAckActor",
            method="<init>"
    )
    public static void onCreate(@Self LogedAckActor obj) {
        String name =obj.self().path().toStringWithoutAddress();
        totalMap.put(name, System.nanoTime());
    }

    @OnMethod(
            clazz="com.yhd.arch.laserbeak.client.LogedAckActor",
            method="onReceive",
            location = @Location(Kind.ENTRY)
    )
    public static void onReceive(@Self LogedAckActor obj) {
        String name =obj.self().path().toStringWithoutAddress();
        onReceiveMap.put(name,System.nanoTime());
    }


    @OnMethod(
            clazz="com.yhd.arch.laserbeak.client.LogedAckActor",
            method="onReceive",
            location = @Location(Kind.RETURN)
    )
    public static void onReturnReceive(@Self LogedAckActor obj) {
        String name =obj.self().path().toStringWithoutAddress();
        long now = System.nanoTime();
        if(onReceiveMap.containsKey(name)){
            long od = ( now-onReceiveMap.remove(name))/1000;
            Aggregations.addToAggregation(onReceiveDuration, od);
        }

        if(totalMap.containsKey(name)){
            long td = (now - totalMap.remove(name))/1000;
            Aggregations.addToAggregation(totalDuration,td);
        }

    }

    @OnTimer(value = 10000)
    public static void printActorInfo() {
        println("-----------------------------------");
        Aggregations.printAggregation("ack onReceive cost, scale:us", onReceiveDuration);
        Aggregations.printAggregation("request total cost, scale:us", totalDuration);
        onReceiveDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
        totalDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    }

}