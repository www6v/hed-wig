import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.aggregation.*;

@BTrace
public class ClientMonitor {
    private static long engineStart;
    private static long handleStart;

    private static Aggregation engineDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static Aggregation handleDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    //--------------------------------------------------------------------------
    @OnMethod(
            clazz="com.yihaodian.architecture.hedwig.client.event.engine.HedwigEventEngine",
            method="syncPoolExec"
    )
    public static void setEngineStart() {
// println is defined in BTraceUtils
        engineStart= timeNanos();
    }

    @OnMethod(
            clazz="com.yihaodian.architecture.hedwig.client.event.engine.HedwigEventEngine",
            method="syncPoolExec",
            location = @Location(Kind.RETURN)
    )
    public static void setEngineDuration() {
// println is defined in BTraceUtils
        int duration = (int)(timeNanos()-engineStart)/1000;
        Aggregations.addToAggregation(engineDuration,duration);
    }

    //--------------------------------------------------------------------------
    @OnMethod(
            clazz="com.yihaodian.architecture.hedwig.client.event.handle.BaseHandler",
            method="handle"
    )
    public static void setHandleStart() {
// println is defined in BTraceUtils
        handleStart= timeNanos();
    }

    @OnMethod(
            clazz="com.yihaodian.architecture.hedwig.client.event.handle.BaseHandler",
            method="handle",
            location = @Location(Kind.RETURN)
    )
    public static void setHandleDuration() {
// println is defined in BTraceUtils
        int duration = (int)(timeNanos()-handleStart)/1000;
        Aggregations.addToAggregation(handleDuration,duration);
    }

    @OnTimer(value = 10000)
    public static void printAvgDecodeDuration() {
        Aggregations.truncateAggregation(engineDuration, 5);
        Aggregations.truncateAggregation(handleDuration, 5);
        Aggregations.printAggregation("engine duration (us)", engineDuration);
        Aggregations.printAggregation("handle duration (us)", handleDuration);
        println("----------------------------------------------------------------");

    }
}