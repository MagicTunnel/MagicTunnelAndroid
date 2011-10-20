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

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class models a routing table entry.
 * @author Vitaly
 *
 */
public class RouteEntry {

    /**
     * Regular expression that represents an address of the form:
     * 192.168.1.2/24, at a line start.
     */
    private static final Pattern REGEX_DESTINATION =
        Pattern.compile("^(\\d+\\.\\d+\\.\\d+\\.\\d+)(?:/(\\d+))?");

    /**
     * Regular expression that represents a gateway entry of the form:
     * via 192.168.1.2.
     */
    private static final Pattern REGEX_GW =
        Pattern.compile("via\\s+(\\d+\\.\\d+.\\d+.\\d+)");

    /**
     * Regex of the form:
     * dev eth0.
     */
    private static final Pattern REGEX_DEV =
        Pattern.compile("dev\\s+(\\w+)");

    /**
     * The maximum length of a network mask.
     */
    private static final int MAX_MASK_LENGTH = 32;

    /**
     * The destination network.
     */
    private int destination;

    /**
     * The gateway through which the destination
     * can be reached.
     */
    private int gateway;

    /**
     * The subnet mask.
     */
    private int mask;

    /**
     * The name of the interface.
     */
    private String iface;


    /**
     * Returns the 32-bit address of the destination network.
     * @return The destination network.
     */
    public final int getDestination() {
        return destination;
    }

    /**
     * Sets the 32-bit address of the destination network.
     * @param destinationNetwork The destination network.
     */
    public final void setDestination(final int destinationNetwork) {
        this.destination = destinationNetwork;
    }

    /**
     * Returns the 32-bit address of the gateway.
     * @return The gateway address.
     */
    public final int getGateway() {
        return gateway;
    }

    /**
     * Sets the 32-bit address of the gateway.
     * @param gatewayAddress The gateway address.
     */
    public final void setGateway(final int gatewayAddress) {
        this.gateway = gatewayAddress;
    }

    /**
     * Gets the 32-bit network mask.
     * @return The network mask.
     */
    public final int getMask() {
        return mask;
    }

    /**
     * Sets the 32-bit network mask.
     * @param networkMask The network mask.
     */
    public final void setMask(final int networkMask) {
        this.mask = networkMask;
    }


    /**
     * Retrieves the name of the network interface.
     * For example, /dev/eth0.
     * @return The interface name.
     */
    public final String getInterfaceName() {
        return iface;
    }


    /**
     * Sets the name of the network interface.
     * @param interfaceName The interface name.
     */
    public final void setInterfaceName(final String interfaceName) {
        this.iface = interfaceName;
    }

    @Override
    public final String toString() {
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
     * Destination   Gateway       Genmask         Flags MSS Window irtt Iface
     * 172.20.2.39   10.50.242.169 255.255.255.255 UGH     0 0         0 rmnet0
     * 10.50.242.168 0.0.0.0       255.255.255.248 U       0 0         0 rmnet0
     * 192.168.233.0 0.0.0.0       255.255.255.0   U       0 0         0 dns0
     * 0.0.0.0       0.0.0.0       0.0.0.0         U       0 0         0 dns0
     *
     * However, we don't use netstat -nrt because its output is not
     * the same on all devices.
     * @param cmdOutput Stripped line output from ip route command
     * @return A RouteEntry object that represents the output of the
     * ip route command.
     */
    public static RouteEntry fromIpRouteCommand(final String cmdOutput) {
        RouteEntry re = new RouteEntry();

        //Parse the destination, which is the column
        if (cmdOutput.startsWith("default")) {
            re.destination = 0;
            re.gateway = 0;
        } else {
            Matcher m = REGEX_DESTINATION.matcher(cmdOutput);

            if (m.find()) {
                re.destination = NetworkUtils.v4StringToInt(m.group(1));
                if (m.group(2) != null) {
                    re.mask = NetworkUtils.prefixLengthToMask(
                                Integer.valueOf(m.group(2))
                              );
                } else {
                    re.mask = MAX_MASK_LENGTH;
                }
            } else {
                return null;
            }
        }

        //Parse the gateway
        Matcher m = REGEX_GW.matcher(cmdOutput);
        if (m.find()) {
            re.gateway = NetworkUtils.v4StringToInt(m.group(1));
        }

        //Parse the device
        m = REGEX_DEV.matcher(cmdOutput);
        if (m.find()) {
            re.iface = m.group(1);
        }

        return re;
    }
}
