package com.yhd.arch.photon.constants;

public interface PhotonPropKeys {

	public static final String KEY_HOST = "akka.remote.netty.tcp.hostname";
	public static final String KEY_AKKAPORT = "akka.remote.netty.tcp.port";
	public static final String KEY_AKKA_ACTOR_SERIALIZERS_HESSIAN = "akka.actor.serializers.hessian";
	public static final String KEY_RECIEVER_COUNT = "photon.endpoint.receiver.count";
	public static final String KEY_THROTTLE_ENABLE = "photon.endpoint.throttle.enable";
	public static final String KEY_THROTTLE_TPS = "photon.endpoint.throttle.tps";
	public static final String KEY_THROTTLE_INTERVAL = "photon.endpoint.throttle.interval";
	public static final String KEY_ENDPOINT_THROTTLER = "photon.endpoint.throttler";
	public static final String KEY_CHANNEL_COUNT = "photon.emmitor.channel.count";
	public static final String KEY_EMMITTOR_COUNT = "photon.emmitor.count";
	public static final String KEY_ACTOR_COUNT = "photon.emmitor.actor.count";
	public static final String KEY_SYNC_MAX_CHANNEL = "photon.emmitor.channel.max";
	public static final String KEY_EMITTER_THROTTLER = "photon.emitter.throttler";
	public static final String KEY_METHOD_LIMIT = "photon.emmitor.method.limit";
	public static final String KEY_RELIVE_INTERVAL = "photon.emmitor.relive.interval";
	public static final String KEY_RELIVE_INTERVAL_LIMIT = "photon.emmitor.relive.interval.max";
	public static final String KEY_RELIVE_COUNT = "photon.emmitor.relive.count";
	public static final String KEY_RELIVE_COUNT_LIMIT = "photon.emmitor.relive.count.max";
	public static final String KEY_TICK_DURATION = "photon.tick.duration";
	public static final String KEY_SERIALIZE_TRIGGER = "p.s.t";// photon.serialize.trigger
	public static final String KEY_SERIALIZE_CODEC = "p.s.c";// photon.serialize.codec

}
