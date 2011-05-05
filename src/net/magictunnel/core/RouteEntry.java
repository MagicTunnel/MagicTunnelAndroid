package net.magictunnel.core;

import java.net.InetAddress;

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
}
