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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * This class contains methods for detection of the TAP driver.
 *
 * @author Vitaly
 *
 */
public final class Tunnel {

    /**
     * The kernel configuration key to search for.
     */
    private static final String CONFIG_TUN = "CONFIG_TUN";

    /**
     * The location of the kernel configuration file.
     */
    private static final String CONFIG = "/proc/config.gz";

    /**
     * The location of the TAP driver's device file.
     */
    private static final String DEV_TUN_FILE = "/dev/tun";

    /**
     * This class is not supposed to be instantiated.
     */
    private Tunnel() {

    }

    /**
     * Checks the presence of TAP device using various methods.
     *
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
     * Looks for /dev/tun.
     *
     * @return if /dev/tun exists
     */
    private static boolean checkTunDevice() {
        File f = new File(DEV_TUN_FILE);
        return f.exists();
    }

    /**
     * Look for CONFIG_TUN=y or CONFIG_TUN=m in /proc/config.gz.
     *
     * @return the presence of the kernel configuration option.
     */
    public static boolean checkConfigGz() {
        File proc = new File(CONFIG);
        if (!proc.exists()) {
            return false;
        }

        try {
            GZIPInputStream procFile = new GZIPInputStream(new FileInputStream(
                    proc));
            BufferedReader buf = new BufferedReader(new InputStreamReader(
                    procFile));
            String line;

            while ((line = buf.readLine()) != null) {
                System.out.println(line);
                if (!line.contains(CONFIG_TUN)) {
                    continue;
                }

                String [] s = line.split("=");
                if (s.length < 2) {
                    continue;
                }

                if (s[1].equals("y") || s[1].equals("m")) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}
