/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInvocation;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.RequestType;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.engine.event.EventState;
import com.yihaodian.architecture.hedwig.engine.event.IScheduledEvent;

/**
 * @author root
 * @param <T>
 * 
 */
public abstract class BaseEvent implements IScheduledEvent<Object> {

	private static final long serialVersionUID = -3268122380332784050L;
	private static final int CALLER_POSITION = 9;
	protected long id;
	protected String reqestId;
	protected long expireTime = InternalConstants.DEFAULT_REQUEST_TIMEOUT;
	protected TimeUnit expireTimeUnit = TimeUnit.MILLISECONDS;
	protected boolean retryable = false;
	protected int execCount = 0;
	protected int maxRedoCount = 0;
	protected long start;
	protected Object result;
	protected MethodInvocation invocation;
	protected List<EventState> states = new ArrayList<EventState>();
	protected List<String> errorMessages = new ArrayList<String>();
	protected List<String> tryHosts = new ArrayList<String>();
	protected RequestType requestType;
	protected String serviceMethod;
	protected String callerMethod;
	protected long execDely = 0;
	protected TimeUnit delyUnit = TimeUnit.MICROSECONDS;
	protected Throwable remoteException;
	protected Date requestTime;
	protected String providerZone;
	protected String providerIDC;
	protected String providerLevel;

	public BaseEvent(MethodInvocation invocation) {
		super();
		this.invocation = invocation;
		this.start = HedwigUtil.getCurrentTime();
		this.serviceMethod = HedwigUtil.getClassName(invocation) + "." + HedwigUtil.getMethodName(invocation);
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		int len = stackTraceElements.length;
		if (len > CALLER_POSITION) {
			StackTraceElement el = stackTraceElements[CALLER_POSITION];
			callerMethod = HedwigUtil.getShortClassName(el.getClassName()) + "." + el.getMethodName();
		}
		this.requestTime = new Date();
	}

	public Date getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}

	public Throwable getRemoteException() {
		return remoteException;
	}

	public void setRemoteException(Throwable remoteException) {
		this.remoteException = remoteException;
	}

	public long getDelay() {
		return execDely;
	}

	public TimeUnit getDelayUnit() {
		return delyUnit;
	}

	@Override
	public long getStart() {
		return start;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setReqestId(String reqestId) {
		this.reqestId = reqestId;
	}

	public String getReqestId() {
		return reqestId;
	}

	@Override
	public boolean isRetryable() {
		return retryable && !isExpire() && !isReachMaxRedoCount();
	}

	protected boolean isExpire() {
		return (HedwigUtil.getCurrentTime() - start) > expireTime;
	}

	protected boolean isReachMaxRedoCount() {
		return execCount > maxRedoCount || execCount > InternalConstants.MAX_REDO_THRESHOLD;
	}

	@Override
	public void increaseExecCount() {
		execCount++;
	}

	@Override
	public Object getResult() {
		return result;
	}

	@Override
	public MethodInvocation getInvocation() {
		return invocation;
	}

	@Override
	public long getExpireTime() {
		return expireTime;
	}

	@Override
	public TimeUnit getExpireTimeUnit() {
		return expireTimeUnit;
	}

	@Override
	public void setResult(Object result) {
		this.result = result;

	}

	public int getMaxRedoCount() {
		return maxRedoCount;
	}

	public void setMaxRedoCount(int maxRedoCount) {
		this.maxRedoCount = maxRedoCount;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public void setExpireTimeUnit(TimeUnit expireTimeUnit) {
		this.expireTimeUnit = expireTimeUnit;
	}

	public void setRetryable(boolean retryable) {
		this.retryable = retryable;
	}

	@Override
	public int getExecCount() {
		return execCount;
	}

	@Override
	public String toString() {
		return "BaseEvent [id=" + id + ", expireTime=" + expireTime + ", expireTimeUnit=" + expireTimeUnit + ", retryable=" + retryable
				+ ", execCount=" + execCount + ", maxRedoCount=" + maxRedoCount + ", start=" + start + ", result=" + result
				+ ", invocation=" + invocation + ", state=" + list2String(states) + ", errorMessages=" + getErrorMessages() + "]";
	}

	public static String list2String(List<EventState> list) {
		StringBuilder sb = new StringBuilder("[");
		for (EventState o : list) {
			sb.append(o.toString()).append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public EventState getState() {
		return states.get((states.size() - 1));
	}

	@Override
	public void setState(EventState state) {
		this.states.add(state);
	}

	public String getErrorMessages() {
		return HedwigUtil.list2String(errorMessages);
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessages.add(errorMessage);
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public String getServiceMethod() {
		return serviceMethod;
	}

	public void setServiceMethod(String serviceMethod) {
		this.serviceMethod = serviceMethod;
	}

	public String getCallerMethod() {
		return callerMethod;
	}

	public void setCallerMethod(String callerMethod) {
		this.callerMethod = callerMethod;
	}

	public List<String> getTryHosts() {
		return tryHosts;
	}

	public void setTryHosts(List<String> tryHosts) {
		this.tryHosts = tryHosts;
	}

	public void setTryHost(String host) {
		this.tryHosts.add(host);
	}

	public String getTryHostList() {
		return HedwigUtil.list2String(this.tryHosts);
	}

	public String getLastTryHost() {
		String value = null;
		if (tryHosts != null && tryHosts.size() > 0) {
			value = tryHosts.get(tryHosts.size() - 1);
		}
		return value == null ? "unknowProviderHost" : value;

	}

	public  String getProviderZone(){
		return providerZone;
	}
	public void setProviderZone(String zone){
		this.providerZone=zone;
	}

	public  String getProviderIDC(){
		return providerIDC;
	}
	public void setProviderIDC(String idc){
		this.providerIDC=idc;
	}

	public  String getProviderLevel(){
		return providerLevel;
	}
	public void setProviderLevel(String providerLevel){
		this.providerLevel=providerLevel;
	}
}
