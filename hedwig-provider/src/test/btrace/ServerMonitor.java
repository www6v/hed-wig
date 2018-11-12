/* BTrace Script Template */
import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.aggregation.*;

@BTrace
public class ServerMonitor {

    private static long workerStart;
    private static long transEncodeStart;
    private static long transDecodeStart;
    private static long encodeStart;
    private static long decodeStart;
    private static long writeStart;
    private static long dobusinessStart;

    private static Aggregation workerDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation encodeDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation decodeDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation writeDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation transEncodeDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation transDecodeDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation doBusinessDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    
    
 //--------------------------------------------------------------------------
    @OnMethod(
            clazz="com.yhd.arch.photon.codec.TransDataSerializer",
            method="toBinary"
    )
    public static void transEncodeStart() {
// println is defined in BTraceUtils
        transEncodeStart= timeNanos();
    }

    @OnMethod(
            clazz="com.yhd.arch.photon.codec.TransDataSerializer",
            method="toBinary",
            location = @Location(Kind.RETURN)
    )
    public static void transEncodeEnd() {
// println is defined in BTraceUtils
        int duration = (int)(timeNanos()-transEncodeStart)/1000;
        Aggregations.addToAggregation(transEncodeDuration,duration);
    }

    @OnMethod(
            clazz="com.yhd.arch.photon.codec.TransDataSerializer",
            method="fromBinaryJava"
    )
    public static void transDecodeStart() {
// println is defined in BTraceUtils
        transDecodeStart= timeNanos();
    }

    @OnMethod(
            clazz="com.yhd.arch.photon.codec.TransDataSerializer",
            method="fromBinaryJava",
            location = @Location(Kind.RETURN)
    )
    public static void transDecodeEnd() {
// println is defined in BTraceUtils
        int duration = (int)(timeNanos()-transDecodeStart)/1000;
        Aggregations.addToAggregation(transDecodeDuration,duration);
    }
    //--------------------------------------------------------------------------
    @OnMethod(
            clazz="akka.remote.transport.AkkaProtocolHandle",
            method="write"
    )
    public static void writeStart() {
// println is defined in BTraceUtils
        writeStart= timeNanos();
    }

    @OnMethod(
            clazz="akka.remote.transport.AkkaProtocolHandle",
            method="write",
            location = @Location(Kind.RETURN)
    )
    public static void writeEnd() {
// println is defined in BTraceUtils
        int duration = (int)(timeNanos()-writeStart)/1000;
        Aggregations.addToAggregation(writeDuration,duration);
    }

//--------------------------------------------------------------------------
    @OnMethod(
            clazz="com.yhd.arch.laserbeak.provider.LogedWorkerActor",
            method="onReceive"
    )
    public static void start() {
// println is defined in BTraceUtils
        workerStart= timeNanos();
    }

    @OnMethod(
            clazz="com.yhd.arch.laserbeak.provider.LogedWorkerActor",
            method="onReceive",
            location = @Location(Kind.RETURN)
    )
    public static void end() {
// println is defined in BTraceUtils
        int duration = (int)(timeNanos()-workerStart)/1000;
        Aggregations.addToAggregation(workerDuration,duration);
    }
//--------------------------------------------------------------------------

    @OnMethod(
            clazz="com.yhd.arch.photon.codec.CodecCenter",
            method="responseEncode"
    )
    public static void encodeStart() {
// println is defined in BTraceUtils
        encodeStart= timeNanos();
    }

    @OnMethod(
            clazz="com.yhd.arch.photon.codec.CodecCenter",
            method="responseEncode",
            location = @Location(Kind.RETURN)
    )
    public static void encodeCost() {
// println is defined in BTraceUtils
        int duration = (int)(timeNanos()-encodeStart)/1000;
        Aggregations.addToAggregation(encodeDuration,duration);
    }

    //--------------------------------------------------------------------------
    @OnMethod(
            clazz="com.yhd.arch.photon.codec.CodecCenter",
            method="requestDecode"
    )
    public static void decodeStart() {
        decodeStart= timeNanos();

    }

    @OnMethod(
            clazz="com.yhd.arch.photon.codec.CodecCenter",
            method="requestDecode",
            location = @Location(Kind.RETURN)
    )
    public static void decodeCost() {
// println is defined in BTraceUtils
//println(strcat("Decode:",str(duration/1000)));
        int duration = (int)(timeNanos()-decodeStart)/1000;
        Aggregations.addToAggregation(decodeDuration,duration);

    }

//--------------------------------------------------------------------------
    @OnMethod(
            clazz="com.yhd.arch.laserbeak.provider.LogedWorkerActor",
            method="doBusiness"
    )
    public static void doBusinessStart() {
    	dobusinessStart= timeNanos();

    }

    @OnMethod(
    		clazz="com.yhd.arch.laserbeak.provider.LogedWorkerActor",
            method="doBusiness",
            location = @Location(Kind.RETURN)
    )
    public static void doBusinessCost() {
// println is defined in BTraceUtils
//println(strcat("Decode:",str(duration/1000)));
        int duration = (int)(timeNanos()-dobusinessStart)/1000;
        Aggregations.addToAggregation(doBusinessDuration,duration);

    }

//--------------------------------------------------------------------------
    @OnTimer(value = 10000)
    public static void printAvgDuration() {
        Aggregations.printAggregation("Average transData decode duration (us)", transDecodeDuration);
        Aggregations.printAggregation("Average request decode duration (us)", decodeDuration);
        Aggregations.printAggregation("Average response encode duration (us)", encodeDuration);
        Aggregations.printAggregation("Average transData encode duration (us)", transEncodeDuration);        
        Aggregations.printAggregation("Average write duration (us)", writeDuration);
        Aggregations.printAggregation("Average doBusiness duration (us)", doBusinessDuration);
        Aggregations.printAggregation("Average worker process duration (us)", workerDuration);
        
        println("----------------------------------------------------------------");
    }

}


