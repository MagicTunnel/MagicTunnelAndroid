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
import java.util.HashMap;
import java.util.Map;

/**
 * Utility functions to remount system partition.
 * @author vitaly
 *
 */
public class Partition {

    /**
     * The place where the kernel stores info about mounted partitions.
     */
    private static final String PARTITIONS_FILE = "/proc/mounts";

    /**
     * The number of fields in each line of PARTITIONS_FILE.
     */
    private static final int FIELDS_PER_ENTRY = 3;

    /**
     * The mapping from the mount point to the partition table entry.
     */
    private Map<String, PartitionInfo> mPartitions;

    /**
     * Constructs and initializes the partition manager with
     * the partitions found in the system.
     */
    public Partition() {
        BufferedReader partFile = getPartitionFile();
        if (partFile == null) {
            return;
        }

        mPartitions = readPartitions(partFile);
    }

    /**
     * Opens the partition file.
     * @return A means of reading the contents of the partition file.
     */
    private BufferedReader getPartitionFile() {
        try {
            File file = new File(PARTITIONS_FILE);
            FileInputStream fis = new FileInputStream(file);
            BufferedReader buf = new BufferedReader(new InputStreamReader(fis));
            return buf;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Transforms the text contained in the partition file into
     * PartitionInfo objects.
     * @param file The partition file.
     * @return A mapping from mount point to PartitionInfo.
     */
    private Map<String, PartitionInfo> readPartitions(
            final BufferedReader file) {

        Map<String, PartitionInfo> partitions =
            new HashMap<String, PartitionInfo>();

        String line;

        try {
            while ((line = file.readLine()) != null) {
                String[] s = line.split(" ");
                if (s.length < FIELDS_PER_ENTRY) {
                    continue;
                }
                PartitionInfo info = new PartitionInfo();
                info.setDevice(s[0]);
                info.setMountPoint(s[1]);
                info.setType(s[2]);
                partitions.put(s[1], info);
            }
        } catch (Exception e) {
            return partitions;
        }

        return partitions;
    }


    /**
     * Generates a command that remounts the specified partition with
     * the desired attributes.
     * @param mountPoint The partition to remount.
     * @param readOnly Whether to remount the partition in readOnly mode.
     * @return The command.
     */
    public final String remountPartition(
            final String mountPoint,
            final boolean readOnly) {

        PartitionInfo info = mPartitions.get(mountPoint);
        if (info == null) {
            return null;
        }

        String mountType;

        if (readOnly) {
            mountType = "ro";
        } else {
            mountType = "rw";
        }

        String command = "mount -o " + mountType + ",remount -t "
        + info.getType()
        + " " + info.getDevice() + " " + info.getMountPoint();

        return command;
    }
}
