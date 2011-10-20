/**
 * MagicTunnel DNS tunnel GUI for Android.
 * Copyright (C) 2011 Vitaly Chipounov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * This class gathers network-related functions,
 * such as getting/setting routes, checking connectivity, etc.
 * @author Vitaly
 *
 */
public final class NetworkUtils {
    /** Logging tag. */
    private static final String TAG = "NetworkUtils";

    /** Size of the host route prefix. */
    private static final int HOST_ROUTE_PREFIX_IPV4 = 32;

    /** Size of the host route prefix. */
    private static final int HOST_ROUTE_PREFIX_IPV6 = 128;

    /** Mask for extracting the lowest byte. */
    private static final int BYTE_MASK = 0xFF;

    /** This class is not supposed to be instantiated. */
    private NetworkUtils() {

    }

    /**
     * Add a route to the routing table.
     *
     * @param interfaceName the interface to route through.
     * @param dst the network or host to route to. May be IPv4 or IPv6, e.g.
     * "0.0.0.0" or "2001:4860::".
     * @param prefixLength the prefix length of the route.
     * @param gw the gateway to use, e.g., "192.168.251.1". If null,
     * indicates a directly-connected route.
     * @return the status code of the command.
     */
    public static int addRoute(
            final String interfaceName,
            final String dst,
            final int prefixLength, final String gw) {

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
    public static boolean addDefaultRoute(
            final String interfaceName,
            final InetAddress gw) {
        String dstStr;
        String gwStr = gw.getHostAddress();

        if (gw instanceof Inet4Address) {
            dstStr = "0.0.0.0";
        } else if (gw instanceof Inet6Address) {
            dstStr = "::";
        } else {
            Log.w(TAG, "addDefaultRoute failure: "
                    + "address is neither IPv4 nor IPv6"
                    + "(" + gwStr + ")");
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
    public static boolean addHostRoute(
            final String interfaceName,
            final InetAddress dst,
            final InetAddress gw) {

        if (dst == null) {
            Log.w(TAG, "addHostRoute: dst should not be null");
            return false;
        }

        int prefixLength;
        String dstStr = dst.getHostAddress();
        String gwStr = null;

        if (gw != null) {
            gwStr = gw.getHostAddress();
        }

        if (dst instanceof Inet4Address) {
            prefixLength = HOST_ROUTE_PREFIX_IPV4;
        } else if (dst instanceof Inet6Address) {
            prefixLength = HOST_ROUTE_PREFIX_IPV6;
        } else {
            Log.w(TAG, "addHostRoute failure: "
                    + "address is neither IPv4 nor IPv6"
                    + "(" + dst + ")");
            return false;
        }
        return addRoute(interfaceName, dstStr, prefixLength, gwStr) == 0;
    }

    /**
     * Remove the default route for the named interface.
     * @param interfaceName The name of the interface.
     * @return the status of the command.
     */
    public static int removeDefaultRoute(final String interfaceName) {
        String cmd = "ip route delete default dev " + interfaceName;
        Commands cmds = new Commands();
        return cmds.runCommandAsRootAndWait(cmd);

    }

    /**
     * Delete all routes from the system.
     */
    public static void removeAllRoutes() {
        Commands cmds = new Commands();
        String cmd = "ip route flush table main";
        cmds.runCommandAsRootAndWait(cmd);
    }

    /**
     * Converts a prefix length to a network mask.
     * For example, 24 would return 255.255.255.0.
     * @param length The prefix length.
     * @return The network mask.
     */
    public static int prefixLengthToMask(final int length) {
        int mask = 0;
        for (int i = 0; i < length; ++i) {
            mask |= (1 << ((HOST_ROUTE_PREFIX_IPV4 - 1) - i));
        }

        //Swap the bytes
        int result = 0;
        result |= (mask >>> 24) & BYTE_MASK;
        result |= ((mask >>> 16) & BYTE_MASK) << 8;
        result |= ((mask >>> 8) & BYTE_MASK) << 16;
        result |= ((mask) & BYTE_MASK) << 24;

        return result;
    }

    /**
     * Transforms a mask into a prefix length.
     * @param subnetMask The mask
     * @return The prefix length
     */
    public static int maskToPrefixLength(final int subnetMask) {
        int mask = subnetMask;
        int length = 0;
        while (mask != 0) {
            if ((mask & 1) != 0) {
                length++;
            }
            mask = mask >>> 1;
        }
        return length;
    }

    /**
     * Activate the specified routes.
     * @param routes The list of routes to add.
     */
    public static void restoreRoutes(final List<RouteEntry> routes) {
        for (RouteEntry re : routes) {
            String dst = intToInetAddress(re.getDestination()).getHostAddress();
            String gw = null;
            if (re.getGateway() != 0) {
                gw = intToInetAddress(re.getGateway()).getHostAddress();
            }

            addRoute(re.getInterfaceName(), dst,
                    maskToPrefixLength(re.getMask()), gw);
        }
    }


    /**
     * Convert a IPv4 address from an integer to an InetAddress.
     * @param hostAddress is an Int corresponding to the IPv4
     * address in network byte order
     * @return the IP address as an {@code InetAddress}, returns null if
     * unable to convert or if the int is an invalid address.
     */
    public static InetAddress intToInetAddress(final int hostAddress) {
        InetAddress inetAddress;
        byte[] addressBytes = { (byte)(BYTE_MASK & hostAddress),
                (byte)(BYTE_MASK & (hostAddress >> 8)),
                (byte)(BYTE_MASK & (hostAddress >> 16)),
                (byte)(BYTE_MASK & (hostAddress >> 24)) };

        try {
            inetAddress = InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            return null;
        }

        return inetAddress;
    }


    /**
     * Converts a string representation of an IPv4 address
     * into the equivalent integer.
     * @param str The string IP address.
     * @return The 32-bits address.
     */
    public static int v4StringToInt(final String str) {
        int result = 0;
        String[] array = str.split("\\.");

        if (array.length != 4) {
            return 0;
        }

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


    

    /**
     * For the given interface name, look for the default route
     * in the list of routes.
     * @param entries The list of routes.
     * @param iface The insterface name.
     * @return The default route, if it exists.
     */
    public static RouteEntry getDefaultRoute(
            final List<RouteEntry> entries,
            final String iface) {

        for (RouteEntry e : entries) {
            if (e.getDestination() == 0 && iface.equals(e.getInterfaceName())) {
                return e;
            }
        }
        return null;
    }

    /**
     * Get the system-wide default route.
     * @param entries The list of routes.
     * @return The default route, if it exists.
     */
    public static RouteEntry getDefaultRoute(final List<RouteEntry> entries) {
        for (RouteEntry e : entries) {
            if (e.getDestination() == 0) {
                return e;
            }
        }
        return null;
    }

    /**
     * XXX: find other standard ways of doing it.
     * @return The DNS server address.
     */
    public static InetAddress getDns() {
        String cmd = "getprop net.dns1";

        Commands cmds = new Commands();
        cmds.runCommandAsRoot(cmd);
        Process p = cmds.getProcess();

        String line;
        BufferedReader in =
            new BufferedReader(new InputStreamReader(p.getInputStream()));

        try {
            line = in.readLine();
            return InetAddress.getByName(line);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Find the interface name of the Wifi interface.
     * @param ctx The Android context.
     * @return The name of the Wifi interface.
     */
    public static String getWifiInterface(final Context ctx) {
        //Get the ip of the Wifi interface
        WifiManager wifi =
            (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

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

    /**
     * Retrieves all the network routes from the system.
     * @return The list of routes.
     */
    public static List<RouteEntry> getRoutes() {
        List<RouteEntry> routes = new ArrayList<RouteEntry>();
        Commands cmds = new Commands();
        cmds.runCommandAsRoot("ip route");

        String line;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(cmds.getProcess().getInputStream()));

        try {
            while ((line = in.readLine()) != null) {
                //Get the destination
                RouteEntry re = RouteEntry.fromIpRouteCommand(line);
                if (re != null) {
                    routes.add(re);
                }
            }
        } catch (IOException e) {
            return routes;
        }
        return routes;
    }

    /**
     * Get the interface name associated with the default route.
     * @return The interface name.
     */
    public static String getDefaultRouteIface() {
        List<RouteEntry> routes = getRoutes();
        RouteEntry re = getDefaultRoute(routes);
        if (re == null) {
            return null;
        }

        return re.getInterfaceName();
    }

    /**
     * Check whether the specified interface exists.
     * @param iface The name of the interface.
     * @return true if the interface exists.
     */
    public static boolean interfaceExists(final String iface) {
        boolean exists = false;
        try {
            exists = NetworkInterface.getByName(iface) != null;
        } catch (SocketException e) {
            return false;
        }
        return exists;
    }

    /**
     * Verifies that Internet is reachable.
     * @param interfaceName The name of the network interface.
     * @return whether the default route on interfaceName exists
     */
    public static boolean checkRoutes(final String interfaceName) {
        List<RouteEntry> routes = getRoutes();
        RouteEntry oldDefaultRoute =
            getDefaultRoute(routes, interfaceName);

        if (oldDefaultRoute == null) {
            return false;
        }
        return true;
    }

    /**
     * @param ctx The Android context.
     * @return Whether WIFI or Data connection is enabled
     */
    public static boolean checkConnectivity(final Context ctx) {
        ConnectivityManager mgr = (ConnectivityManager) ctx.getSystemService(
                Context.CONNECTIVITY_SERVICE);


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
