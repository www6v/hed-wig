package com.yihaodian.architecture.hedwig.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import junit.framework.TestCase;

/**
 * Created by root on 8/24/16.
 */
public class GetLocalHost extends TestCase {

	public void testGetIpByJDK() throws UnknownHostException {
		System.out.println("getByJDK:" + InetAddress.getLocalHost().getHostAddress());
	}

	public void testGetIpByNI() throws SocketException {
		//System.setProperty("sys.ni.name", "en4"); //Mac
        //System.setProperty("sys.ni.name", "eth4"); //Windows
        //System.setProperty("sys.ni.name", "eth0"); //Linux
		String ifName = System.getProperty("sys.ni.name");
        String ip = "";
		for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
			NetworkInterface iface = ifaces.nextElement();
			String name = iface.getName();
			if (name.equals(ifName)) {
				for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress address = inetAddrs.nextElement();
					if (!address.isLoopbackAddress()) {
						if (address.isSiteLocalAddress()) {
							ip = address.getHostAddress();
						}
					}
				}
			}
		}
        System.out.println("getByNI:" + ip);
	}

	public void testGetIpBySystemUtil() {
		System.out.println("getBySystemUtil:" + SystemUtil.getLocalhostIp());
	}

    public void testGetAllNI() throws SocketException {
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
            NetworkInterface iface = ifaces.nextElement();
            System.out.print(iface.getName() + ":");
            for(Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses.hasMoreElements();){
                String s = addresses.nextElement().getHostAddress();
                System.out.print(s+",   ");
            }
            System.out.println();
        }
    }
}
