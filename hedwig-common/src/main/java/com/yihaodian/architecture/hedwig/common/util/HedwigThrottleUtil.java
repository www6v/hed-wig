/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.codehaus.jackson.map.ObjectMapper;

import com.caucho.hessian.io.HessianOutput;
import com.yhd.arch.zone.Zone;

/**
 * @author pengrongxin 2014年11月5日 下午2:52:07
 */
public class HedwigThrottleUtil {

	/**
	 * 计算Object的按某种序列化协议后的字节大小 协议支持 JAVA缺省的Serializer，Hessian，JSON三种
	 * 
	 * @param object
	 * @param protocolType
	 * @return
	 * @throws Exception
	 */
	public static long getObjectSize(Object object, ProtocolType protocolType) throws Exception {
		HedwigAssert.isNull(object, "can't compute throttle size of null object");
		if (protocolType == ProtocolType.DEFAULT_JAVA || protocolType == ProtocolType.HESSIAN) {
			if (!(object instanceof Serializable)) {
				throw new Exception("instance of " + object.getClass() + " not implements Serializable,not support serialize");
			}
		}
		OutputStreamProxy osp = new OutputStreamProxy();
		ComputeOutputStream cos = new ComputeOutputStream();
		ObjectOutputStream oos = null;
		HessianOutput hos = null;
		if (protocolType == ProtocolType.DEFAULT_JAVA) {
			oos = new ObjectOutputStream(cos);
			osp.setOos(oos);
		} else if (protocolType == ProtocolType.HESSIAN) {
			hos = new HessianOutput(cos);
			osp.setHos(hos);
		} else if (protocolType == ProtocolType.JSON) {
			ObjectMapper om = new ObjectMapper();
			try {
				om.writeValue(cos, object);
			} finally {

			}
		}
		try {
			osp.computeLength(object);
			return cos.size;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (osp != null) {
					osp.close();
				}
			} catch (Exception e) {
			}
		}
		return -1;
	}

	static class ComputeOutputStream extends OutputStream {
		int size;

		@Override
		public void write(int c) {
			size++;
		}
	}

	static class OutputStreamProxy {
		ObjectOutputStream oos;
		HessianOutput hos;

		public ObjectOutputStream getOos() {
			return oos;
		}

		public void setOos(ObjectOutputStream oos) {
			this.oos = oos;
		}

		public HessianOutput getHos() {
			return hos;
		}

		public void setHos(HessianOutput hos) {
			this.hos = hos;
		}

		public void computeLength(Object object) throws IOException {
			if (this.oos != null) {
				this.oos.writeObject(object);
				return;
			} else if (this.hos != null) {
				this.hos.writeObject(object);
			}
		}

		public void close() {
			if (this.oos != null) {
				try {
					this.oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			if (this.hos != null) {
				try {
					this.hos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		Zone zone = new Zone();
		zone.setName("nanhui");
		zone.setPlatform("soa");
		zone.setBandwidthIn(100l);
		zone.setBandwidthOut(200l);
		zone.setDesc("test");

		try {
			long size1 = getObjectSize(zone, ProtocolType.DEFAULT_JAVA);
			long size2 = getObjectSize(zone, ProtocolType.HESSIAN);
			long size3 = getObjectSize(zone, ProtocolType.JSON);
			System.out.println("java:" + size1 + ",hessian:" + size2 + ",json:" + size3);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
