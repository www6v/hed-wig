/* BTrace Script Template */
import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.aggregation.*;

@BTrace
public class ClientMonitor {

    private static long requestStart;
    private static long transEncodeStart;
    private static long transDecodeStart;
    private static long encodeStart;
    private static long decodeStart;
    private static long writeStart;
    private static long ackStart;

    private static Aggregation encodeDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static Aggregation decodeDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static Aggregation requestDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static Aggregation writeDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static Aggregation transEncodeDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static Aggregation transDecodeDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static Aggregation ackDuration = Aggregations.newAggregation(AggregationFunction.QUANTIZE);



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
            clazz="com.yhd.arch.laserbeak.client.RequestHandler",
            method="invoke"
    )
    public static void requestStart() {
// println is defined in BTraceUtils
        requestStart= timeNanos();
    }

    @OnMethod(
            clazz="com.yhd.arch.laserbeak.client.RequestHandler",
            method="invoke",
            location = @Location(Kind.RETURN)
    )
    public static void requestEnd() {
// println is defined in BTraceUtils
        int duration = (int)(timeNanos()-requestStart)/1000;
        Aggregations.addToAggregation(requestDuration,duration);
    }

//--------------------------------------------------------------------------

    @OnMethod(
            clazz="com.yhd.arch.photon.codec.CodecCenter",


            method="requestEncode"
    )
    public static void encodeStart() {
// println is defined in BTraceUtils
        encodeStart= timeNanos();
    }

    @OnMethod(
            clazz="com.yhd.arch.photon.codec.CodecCenter",
            method="requestEncode",
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
            method="responseDecode"
    )
    public static void decodeStart() {
        decodeStart= timeNanos();

    }

    @OnMethod(
            clazz="com.yhd.arch.photon.codec.CodecCenter",
            method="responseDecode",
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
            clazz="com.yhd.arch.laserbeak.client.LogedAckActor",
            method="onReceive"
    )
    public static void ackStart() {
        ackStart= timeNanos();

    }
    @OnMethod(
            clazz="com.yhd.arch.laserbeak.client.LogedAckActor",
            method="onReceive",
            location = @Location(Kind.RETURN)
    )
    public static void ackCost() {
        int duration = (int)(timeNanos()-ackStart)/1000;
        Aggregations.addToAggregation(ackDuration,duration);

    }

//--------------------------------------------------------------------------


    @OnTimer(value = 10000)
    public static void printAvgDecodeDuration() {
        Aggregations.truncateAggregation(ackDuration, 5);
        Aggregations.printAggregation("Average encode duration (us)", encodeDuration);
        Aggregations.printAggregation("Average transData request encode duration (us)", transEncodeDuration);
        Aggregations.printAggregation("Average transData response decode duration (us)", transDecodeDuration);
        Aggregations.printAggregation("Average responseDecode duration (us)", decodeDuration);
        Aggregations.printAggregation("Average write duration (us)", writeDuration);
        Aggregations.printAggregation("Average request duration (us)", requestDuration);
        Aggregations.printAggregation("ACK duration (us)", ackDuration);
        println("----------------------------------------------------------------");

    }

}


