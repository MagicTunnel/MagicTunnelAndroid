package net.magictunnel.core;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteEntry {
	public int destination;
	public int gateway;
	public int mask;
	public String iface;
	
	
	public int getDestination() {
		return destination;
	}
	public void setDestination(int destination) {
		this.destination = destination;
	}
	public int getGateway() {
		return gateway;
	}
	public void setGateway(int gateway) {
		this.gateway = gateway;
	}
	public int getMask() {
		return mask;
	}
	public void setMask(int mask) {
		this.mask = mask;
	}
	public String getIface() {
		return iface;
	}
	public void setIface(String iface) {
		this.iface = iface;
	}
	
	@Override
	public String toString() {
		InetAddress strDest = NetworkUtils.intToInetAddress(destination);
		InetAddress strGw = NetworkUtils.intToInetAddress(gateway);
		InetAddress strMask = NetworkUtils.intToInetAddress(mask);
		
		return strDest + " " + strMask + " via " + strGw + " dev " + iface;
	}


	/**
	 * Parses the following types of entries:
	 * 172.20.2.39 via 10.50.242.169 dev rmnet0
	 * 10.50.242.168/29 dev rmnet0  proto kernel  scope link  src 10.50.242.171
	 * 192.168.233.0/24 dev dns0  proto kernel  scope link  src 192.168.233.2
	 * default dev dns0  scope link
	 *
	 * This is equivalent to the following netstat -nrt
	 * Destination     Gateway         Genmask         Flags   MSS Window  irtt Iface
     * 172.20.2.39     10.50.242.169   255.255.255.255 UGH       0 0          0 rmnet0
     * 10.50.242.168   0.0.0.0         255.255.255.248 U         0 0          0 rmnet0
     * 192.168.233.0   0.0.0.0         255.255.255.0   U         0 0          0 dns0
     * 0.0.0.0         0.0.0.0         0.0.0.0         U         0 0          0 dns0
     *
     * However, we don't use netstat -nrt because its output is not the same on all
     * devices.
	 * @param s Stripped line output from ip route command
	 * @return
	 */
	public static RouteEntry fromIpRouteCommand(String s) {
		RouteEntry re = new RouteEntry();

		//Parse the destination, which is the column
		if (s.startsWith("default")) {
			re.destination = 0;
			re.gateway = 0;
		}else {
			//final Pattern REGEX_IP = Pattern.compile("\\d+\\.\\d+.\\d+.\\d+");
			final Pattern REGEX_DESTINATION = Pattern.compile("^(\\d+\\.\\d+\\.\\d+\\.\\d+)(?:/(\\d+))?");
			Matcher m = REGEX_DESTINATION.matcher(s);

			if (m.find()) {
				re.destination = NetworkUtils.v4StringToInt(m.group(1));
				if (m.group(2) != null) {
					re.mask =  NetworkUtils.prefixLengthToMask(Integer.valueOf(m.group(2)));
				}else {
					re.mask = 32;
				}
			}else {
				return null;
			}
		}

		//Parse the gateway
		final Pattern REGEX_GW = Pattern.compile("via\\s+(\\d+\\.\\d+.\\d+.\\d+)");
		Matcher m = REGEX_GW.matcher(s);
		if (m.find()) {
			re.gateway = NetworkUtils.v4StringToInt(m.group(1));
		}

		//Parse the device
		final Pattern REGEX_DEV = Pattern.compile("dev\\s+(\\w+)");
		m = REGEX_DEV.matcher(s);
		if (m.find()) {
			re.iface = m.group(1);
		}

		return re;
	}
}
