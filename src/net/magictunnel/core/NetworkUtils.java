/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.magictunnel.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkUtils {
	private static final String TAG = "NetworkUtils";

	/**
     * Add a route to the routing table.
     *
     * @param interfaceName the interface to route through.
     * @param dst the network or host to route to. May be IPv4 or IPv6, e.g.
     * "0.0.0.0" or "2001:4860::".
     * @param prefixLength the prefix length of the route.
     * @param gw the gateway to use, e.g., "192.168.251.1". If null,
     * indicates a directly-connected route.
     */
    public static int addRoute(String interfaceName, String dst,
          int prefixLength, String gw) {
    	String cmd = "ip route add " + dst + "/" + prefixLength;
    	if (gw != null) {
    		cmd = cmd + " via " + gw;
    	}
    	cmd = cmd + " dev " + interfaceName;
    	
    	Commands cmds = new Commands();
    	return cmds.runCommandAsRootAndWait(cmd);
    }

    /**
     * Add a default route through the specified gateway.
     * @param interfaceName interface on which the route should be added
     * @param gw the IP address of the gateway to which the route is desired,
     * @return {@code true} on success, {@code false} on failure
     */
    public static boolean addDefaultRoute(String interfaceName, InetAddress gw) {
        String dstStr;
        String gwStr = gw.getHostAddress();

        if (gw instanceof Inet4Address) {
            dstStr = "0.0.0.0";
        } else if (gw instanceof Inet6Address) {
            dstStr = "::";
        } else {
            Log.w(TAG, "addDefaultRoute failure: address is neither IPv4 nor IPv6" +
                       "(" + gwStr + ")");
            return false;
        }
        return addRoute(interfaceName, dstStr, 0, gwStr) == 0;
    }
    
	
	/**
     * Add a host route.
     * @param interfaceName interface on which the route should be added
     * @param dst the IP address of the host to which the route is desired,
     * this should not be null.
     * @param gw the IP address of the gateway to which the route is desired,
     * if null, indicates a directly-connected route.
     * @return {@code true} on success, {@code false} on failure
     */
    public static boolean addHostRoute(String interfaceName, InetAddress dst,
          InetAddress gw) {
        if (dst == null) {
            Log.w(TAG, "addHostRoute: dst should not be null");
            return false;
        }

        int prefixLength;
        String dstStr = dst.getHostAddress();
        String gwStr = (gw != null) ? gw.getHostAddress() : null;

        if (dst instanceof Inet4Address) {
            prefixLength = 32;
        } else if (dst instanceof Inet6Address) {
            prefixLength = 128;
        } else {
            Log.w(TAG, "addHostRoute failure: address is neither IPv4 nor IPv6" +
                       "(" + dst + ")");
            return false;
        }
        return addRoute(interfaceName, dstStr, prefixLength, gwStr) == 0;
    }

    /** Remove the default route for the named interface. */
    public static int removeDefaultRoute(String interfaceName) {
    	String cmd = "ip route delete default dev " + interfaceName;
    	Commands cmds = new Commands();
    	return cmds.runCommandAsRootAndWait(cmd);
    	
    }
    
    public static void removeAllRoutes() {
    	Commands cmds = new Commands();	
    	String cmd = "ip route flush table main";
    	cmds.runCommandAsRootAndWait(cmd);
    }

    public static int prefixLengthToMask(int length) {
    	int mask = 0;
    	for (int i=0; i<length; ++i) {
    		mask |= (1<<(31-i));
    	}

    	//Swap the bytes
    	int result = 0;
    	result |= (mask >>> 24) & 0xFF;
    	result |= ((mask >>> 16) & 0xFF) << 8;
    	result |= ((mask >>> 8) & 0xFF) << 16;
    	result |= ((mask) & 0xFF) << 24;

    	return result;
    }
    
    public static int maskToPrefixLength(int mask) {
    	int length = 0;
    	while (mask != 0) {
    		if ((mask & 1) != 0) {
    			length++;
    		}
    		mask = mask >>> 1;
    	}
    	return length;
    }
    
    public static void restoreRoutes(List<RouteEntry> routes) {
    	for (RouteEntry re:routes) {
    		String dst = intToInetAddress(re.getDestination()).getHostAddress();
    		String gw = null;
    		if (re.getGateway() != 0) {
    			gw = intToInetAddress(re.getGateway()).getHostAddress();
    		}
    		
    		addRoute(re.getIface(), dst, maskToPrefixLength(re.getMask()), gw);    		
    	}
    }

    
	/**
     * Convert a IPv4 address from an integer to an InetAddress.
     * @param hostAddr is an Int corresponding to the IPv4 address in network byte order
     * @return the IP address as an {@code InetAddress}, returns null if
     * unable to convert or if the int is an invalid address.
     */
    public static InetAddress intToInetAddress(int hostAddress) {
        InetAddress inetAddress;
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                                (byte)(0xff & (hostAddress >> 8)),
                                (byte)(0xff & (hostAddress >> 16)),
                                (byte)(0xff & (hostAddress >> 24)) };

        try {
           inetAddress = InetAddress.getByAddress(addressBytes);
        } catch(UnknownHostException e) {
           return null;
        }

        return inetAddress;
    }

	
	public static int v4StringToInt(String str) {
        int result = 0;
        String[] array = str.split("\\.");
        if (array.length != 4) return 0;
        try {
            result = Integer.parseInt(array[3]);
            result = (result << 8) + Integer.parseInt(array[2]);
            result = (result << 8) + Integer.parseInt(array[1]);
            result = (result << 8) + Integer.parseInt(array[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
        return result;
    }

	public static void dumpRoutes(StringBuffer log) {
		String line;
		Commands cmds = new Commands();
		cmds.runCommandAsRoot("ip route");
		BufferedReader in = new BufferedReader(new InputStreamReader(cmds.getProcess().getInputStream()));

		try {
			while ((line = in.readLine()) != null) {
				log.append(line+"\n");
			}
		} catch (IOException e) {

		}
	}

	public static List<RouteEntry> getRoutes() {
		List<RouteEntry> routes = new ArrayList<RouteEntry>();
		Commands cmds = new Commands();
		cmds.runCommandAsRoot("ip route");

		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(cmds.getProcess().getInputStream()));

		final String ip =  "\\d+\\.\\d+.\\d+.\\d+";

		try {
			while ((line = in.readLine()) != null) {
				//Get the destination
				RouteEntry re = RouteEntry.fromIpRouteCommand(line);
				if (re != null) {
					routes.add(re);
				}
			}
		} catch (IOException e) {

		}
		return routes;
	}

	/*
	public static List<RouteEntry> getRoutes() {
		List<RouteEntry> routes = new ArrayList<RouteEntry>();
		Commands cmds = new Commands();
		cmds.runCommandAsRoot("netstat -nrt");
		
		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(cmds.getProcess().getInputStream()));
		
		try {
			while ((line = in.readLine()) != null) {
				final String ip =  "\\d+\\.\\d+.\\d+.\\d+";
				String [] data = line.split("\\s+");
				if (data.length < 8 || !data[0].matches(ip)) {
					continue;
				}
				RouteEntry re = new RouteEntry();
				re.destination = v4StringToInt(data[0]);
				re.gateway = v4StringToInt(data[1]);
				re.mask = v4StringToInt(data[2]);
				re.iface = data[7];
				routes.add(re);
			}
		} catch (IOException e) {
			
		}
		return routes;
	}*/
	
	public static RouteEntry getDefaultRoute(List<RouteEntry> entries, String iface) {
		for (RouteEntry e:entries) {
			if (e.getDestination() == 0 && iface.equals(e.getIface())) {
				return e;
			}
		}
		return null;
	}
	
	public static RouteEntry getDefaultRoute(List<RouteEntry> entries) {
		for (RouteEntry e:entries) {
			if (e.getDestination() == 0) {
				return e;
			}
		}
		return null;
	}
	
	/**
	 * XXX: find other standard ways of doing it.
	 * @return
	 */
	public static InetAddress getDns() {
		String cmd = "getprop net.dns1";

		Commands cmds = new Commands();
    	cmds.runCommandAsRoot(cmd);
    	Process p = cmds.getProcess();

		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		try {
			line = in.readLine();
			return InetAddress.getByName(line);
		} catch (Exception e) {

		}
		return null;
		
	}
	
	public static String getWifiInterface(Context ctx) {
		//Get the ip of the Wifi interface
		WifiManager wifi = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		int ip = wifi.getConnectionInfo().getIpAddress();
		if (ip == 0) {
			return null;
		}
		
		InetAddress addr = intToInetAddress(ip);
		NetworkInterface ni;
		try {
			ni = NetworkInterface.getByInetAddress(addr);
			return ni.getName();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getDefaultRouteIface() {
		List<RouteEntry> routes = getRoutes();
		RouteEntry re = getDefaultRoute(routes);
		if (re == null) {
			return null;
		}
		
		return re.getIface();
	}
	
	public static boolean interfaceExists(String iface) {
		try {
			if (NetworkInterface.getByName(iface) != null) {
				return true;
			}
		} catch (SocketException e) {
		}
		return false;
	}

	/**
	 * Verifies that Internet is reachable
	 * @param transportInterface
	 * @return whether the default route on transportInterface exists
	 */
	public static boolean checkRoutes(String transportInterface) {
		List<RouteEntry> routes = getRoutes();
		RouteEntry oldDefaultRoute = getDefaultRoute(routes, transportInterface);
		if (oldDefaultRoute == null) {
			return false;
		}
		return true;
	}

	/**
	 *
	 * @return Whether WIFI or Data connection is enabled
	 */
	public static boolean checkConnectivity(Context ctx) {
		ConnectivityManager mgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo infoWifi, infoMobile;

		infoWifi = mgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (infoWifi != null) {
			if (infoWifi.isConnected()) {
				return true;
			}
		}

		infoMobile = mgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (infoMobile != null) {
			if (infoMobile.isConnected()) {
				return true;
			}
		}

		return false;
	}

}
