package net.magictunnel.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class Tunnel {
	public static final String CONFIG_TUN = "CONFIG_TUN";
	public static final String CONFIG = "/proc/config.gz";
	
	/**
	 * Checks the presence of TAP device using various methods.
	 * @return Whether the TAP device is present or not.
	 */
	public static boolean checkTap() {
		if (checkTunDevice()) {
			return true;
		}

		if (checkConfigGz()) {
			return true;
		}

		return false;
	}

	/**
	 * Looks for /dev/tun
	 * @return if /dev/tun exists
	 */
	private static boolean checkTunDevice() {
		File f = new File("/dev/tun");
		return f.exists();
	}

	/**
	 * Look for CONFIG_TUN=y or CONFIG_TUN=m in /proc/config.gz
	 * @return
	 */
	public static boolean checkConfigGz() {
		File proc = new File(CONFIG);
		if (!proc.exists()) {
			return false;
		}
		
		try {
			GZIPInputStream procFile = new GZIPInputStream(new FileInputStream(proc));
			BufferedReader buf = new BufferedReader(new InputStreamReader(procFile));
			String line;
			
			while ((line = buf.readLine()) != null) {
				System.out.println(line);
				if (!line.contains(CONFIG_TUN)) {
					continue;
				}
				
				String s[] = line.split("=");
				if (s.length<2) {
					continue;
				}
				
				if (s[1].equals("y") || s[1].equals("m")) {
					return true;
				}
			}
		}catch (Exception e) {
			return false;
		}
		
		return false;
	}
}
