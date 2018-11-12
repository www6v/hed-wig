package com.yihaodian.architecture.hedwig.balancer.special;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yihaodian.architecture.hedwig.common.util.HedwigJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.balancer.BalancerUtil;
import com.yihaodian.architecture.hedwig.balancer.Circle;
import com.yihaodian.architecture.hedwig.client.jmx.GrayWRRBalancerMXBean;
import com.yihaodian.architecture.hedwig.client.jmx.GrayWRRBalancerJMXRegistration;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.common.util.StringUtils;


public class GrayWRRBalancer implements DynamicLoadBalancer<ServiceProfile, String>, GrayWRRBalancerMXBean {

	private static Logger logger = LoggerFactory.getLogger(GrayWRRBalancer.class);
	protected volatile Circle<Integer, ServiceProfile> normalCircle = new Circle<Integer, ServiceProfile>();
	protected volatile Circle<Integer, ServiceProfile> grayCircle = new Circle<Integer, ServiceProfile>();
	protected Collection<String> groupWhiteList = null;
	protected Collection<String> graySet = null;
	protected volatile int tokenStart = 0;
	protected volatile int tokenEnd = 0;
	protected int MAXTOKEN = ProperitesContainer.client().getIntProperty(PropKeyConstants.HEDWIG_TOKEN_MAX,
			InternalConstants.DEFAULT_MAX_TOKEN);
	protected volatile Circle<Integer, Boolean> grayWindow = new Circle<Integer, Boolean>();
	protected Random random = new Random();
	protected AtomicInteger npos = new AtomicInteger(random.nextInt(InternalConstants.INTEGER_BARRIER));
	protected AtomicInteger gpos = new AtomicInteger(random.nextInt(InternalConstants.INTEGER_BARRIER));
	protected Lock lock = new ReentrantLock();

	public GrayWRRBalancer() {
		super();
		grayWindow.put(0, false);
	}

	@Override
	public ServiceProfile select() {
		String token = HedwigContextUtil.getString(PropKeyConstants.HEDWIG_TOKEN_GRAY, "0");
		if (logger.isDebugEnabled()) {
			logger.debug("###[hedwig-client:GrayWRRBalancer.select()] token.gray=" + token);
			logger.debug("####----normalCircle=" + HedwigJsonUtil.toJSONString(getNormalCircle()) + ",grayCircle=" + HedwigJsonUtil.toJSONString(grayCircle)
					+ ",grayWindow=" + HedwigJsonUtil.toJSONString(grayWindow) + "----####----");

		}
		return select(token);
	}

	@Override
	public void updateProfiles(Collection<ServiceProfile> serviceSet) {
		lock.lock();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("####----updateProfiles.serviceSet=" + HedwigJsonUtil.toJSONString(serviceSet) + "----####----");
			}
			Circle<Integer, ServiceProfile> ncircle = new Circle<Integer, ServiceProfile>();
			Circle<Integer, ServiceProfile> gcircle = new Circle<Integer, ServiceProfile>();
			int nsize = 0;
			int gsize = 0;
			Collection<ServiceProfile> realServiceSet = BalancerUtil.filte(serviceSet, groupWhiteList);
			for (ServiceProfile sp : realServiceSet) {
				int weight = sp.getWeighted();
				if (this.graySet != null && this.graySet.size() > 0) {
					if (isInGrayHostSet(graySet, sp)) {
						for (int i = 0; i < weight; i++) {
							gcircle.put(gsize++, sp);
						}
					} else {
						for (int i = 0; i < weight; i++) {
							ncircle.put(nsize++, sp);
						}
					}
				} else {
					for (int i = 0; i < weight; i++) {
						ncircle.put(nsize++, sp);
					}
				}

			}
			setNormalCircle(ncircle);
			grayCircle = gcircle;
			grayWindow = getGrayWindow();
			if (logger.isDebugEnabled()) {
				logger.debug("####----normalCircle=" + HedwigJsonUtil.toJSONString(getNormalCircle()) + ",grayCircle=" + HedwigJsonUtil.toJSONString(grayCircle)
						+ ",grayWindow=" + HedwigJsonUtil.toJSONString(grayWindow) + "----####----");
			}
		} finally {
			lock.unlock();
		}

	}

	private boolean isInGrayHostSet(Collection<String> graySet, ServiceProfile sp) {
		boolean result = false;
		if (graySet != null && graySet.size() > 0 && sp != null) {
			for (String gh : graySet) {
				if (StringUtils.isIpPortAddress(gh)) {
					result = gh.equals(sp.getHostString());
				} else {
					result = gh.equals(sp.getHostIp());
				}
				if (result == true) {
					break;
				}
			}
		}
		return result;
	}

	private Circle<Integer, Boolean> getGrayWindow() {
		Circle<Integer, Boolean> circle = new Circle<Integer, Boolean>();
		int ns = normalCircle.size();
		int gs = grayCircle.size();
		if (ns * gs > 0) {
			int offset = getTokenStart() > 0 ? (getTokenStart() - 1) : MAXTOKEN;
			int pivot = MAXTOKEN * gs / (gs + ns) + offset;
			pivot = pivot > MAXTOKEN ? (pivot - MAXTOKEN) : pivot;
			circle.put(offset, true);
			circle.put(pivot, false);
			if (logger.isDebugEnabled()) {
				logger.debug("window:" + getTokenStart() + "-" + pivot);
			}
		} else {
			if (ns == 0) {
				circle.put(Integer.valueOf(0), new Boolean(true));
			}
			if (gs == 0) {
				circle.put(Integer.valueOf(0), new Boolean(false));
			}
		}
		return circle;
	}

	private boolean isGrayToken(int itoken) {
		return grayWindow.lowerValue(itoken);
	}

	@Override
	public ServiceProfile select(String userToken) {
		ServiceProfile sp = null;
		int itoken = StringUtils.isNumeric(userToken) ? Integer.valueOf(userToken) : 0;
		if (itoken <= 0) {
			sp = specifyLogicTrigger(itoken);
		} else {
			if (isGrayToken(itoken)) {
				if (logger.isDebugEnabled()) {
					logger.debug("gray:" + userToken);
				}
				sp = getGraySP();
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("norm:" + userToken);
				}
				sp = getNormalSP();
			}
		}
		if (sp != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("###[hedwig-client:GrayWRRBalancer.select()] target serviceUrl=" + sp.getServiceUrl());
			}
		}
		if(sp==null) {
			logger.error("#### ServiceProfile is NULL!! ----normalCircle=" + HedwigJsonUtil.toJSONString(getNormalCircle())
					+ ",grayCircle=" + HedwigJsonUtil.toJSONString(grayCircle)
					+ ",grayWindow=" + HedwigJsonUtil.toJSONString(grayWindow)
					+ "----####----");
		}
		return sp;
	}

	private ServiceProfile getGraySP() {
		ServiceProfile sp = null;
		if (grayCircle != null && grayCircle.size() > 0) {
			int pos = gpos.incrementAndGet();
			int totalSize = grayCircle.size();
			if (totalSize > 0) {
				int realPos = pos % totalSize;
				sp = getProfileFromCircle(grayCircle, realPos);
				if (pos > InternalConstants.INTEGER_BARRIER) {
					gpos.set(0);
				}
			}
		}
		return sp;
	}

	private ServiceProfile getNormalSP() {
		ServiceProfile sp = null;
		int pos = npos.incrementAndGet();
		if (getNormalCircle() != null && normalCircle.size() > 0) {
			int totalSize = normalCircle.size();
			if (totalSize > 0) {
				int realPos = pos % totalSize;
				sp = getProfileFromCircle(normalCircle, realPos);
				if (pos > InternalConstants.INTEGER_BARRIER) {
					npos.set(0);
				}
			}
		}

		return sp;
	}

	private ServiceProfile specifyLogicTrigger(int itoken) {
		ServiceProfile sp = null;
		if (itoken == -1) {
			sp = getGraySP();
			if (sp == null) {
				sp = getNormalSP();
			}
		}
		if (itoken == 0) {
			int nSize = normalCircle.size();
			int gSize = grayCircle.size();
			if (nSize * gSize > 0) {
				int rint = random.nextInt((nSize + gSize));
				if (rint > nSize) {
					sp = getGraySP();
				} else {
					sp = getNormalSP();
				}
			} else {
				if (nSize == 0) {
					sp = getGraySP();
				}
				if (gSize == 0) {
					sp = getNormalSP();
				}
			}

		}
		return sp;
	}

	protected ServiceProfile getProfileFromCircle(Circle<Integer, ServiceProfile> circle, int code) {
		int size = circle.size();
		ServiceProfile sp = null;
		if (size > 0) {
			int tmp = code;
			while (size > 0) {
				tmp = circle.higherKey(tmp);
				sp = circle.get(tmp);
				if (sp != null && sp.isAvailable()) {
					break;
				} else {
					sp = null;
				}
				size--;
			}
		}
		return sp;
	}

	@Override
	public void setWhiteList(Collection<String> serviceSet) {
		this.groupWhiteList = serviceSet;

	}

	@Override
	public void setSpecialInfo(ISpecialInfo info) {
		if (info != null) {
			GrayInfo grayInfo = (GrayInfo) info;
			graySet = grayInfo.getGraySet();
			setTokenStart(grayInfo.getStart());
			tokenEnd = grayInfo.getEnd();
		} else {
			graySet = null;
			setTokenStart(0);
			tokenEnd = 0;
		}
	}

	@Override
	public String dumpBalancerCircleInfo() {
        String resultMsg="";
        try {
             resultMsg="####----normalCircle=" + HedwigJsonUtil.toJSONString(getNormalCircle()) + ",grayCircle=" + HedwigJsonUtil.toJSONString(grayCircle)
                    + ",grayWindow=" + HedwigJsonUtil.toJSONString(grayWindow) + "----####----";
        }catch (Exception e){
            e.printStackTrace();
        }
		return resultMsg;
	}
    
	/////
//	public Map getNormalCircle() {
////		Map map = new HashMap();
////		return (java.util.TreeMap<Integer, ServiceProfile>) normalCircle;
//		return (Map) normalCircle;
//	}
//	public String getNormalCircle() {
//		return normalCircle.toString();
//	}
	public void setNormalCircle(Circle<Integer, ServiceProfile> normalCircle) {
		this.normalCircle = normalCircle;
	}

	public int getTokenStart() {
		return tokenStart;
	}
	public void setTokenStart(int tokenStart) {
		this.tokenStart = tokenStart;
	}
	

	@Override
	public Circle<Integer, ServiceProfile> getNormalCircle() {
         return this.normalCircle;
	}  
}
