package net.magictunnel.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.magictunnel.settings.Profile;

/**
 * Utility functions to remount system partition
 * @author vitaly
 *
 */
public class Partition {
	public static final String PARTITIONS = "/proc/mounts";
	private Map<String, PartitionInfo> m_partitions;
	
	public Partition() {
		BufferedReader partFile = getPartitionFile();
		if (partFile == null) {
			return;
		}
		
		m_partitions = readPartitions(partFile);	
	}
	
	private BufferedReader getPartitionFile() {
		try {
			File file = new File(PARTITIONS);
			FileInputStream fis = new FileInputStream(file); 
			BufferedReader buf = new BufferedReader(new InputStreamReader(fis));
			return buf;
		}catch(Exception e) {
			return null;
		}
	}
	
	private Map<String, PartitionInfo> readPartitions(BufferedReader file) {
		Map<String, PartitionInfo> partitions = new HashMap<String, PartitionInfo>();
		
		String line;
		
		try {
			while ((line = file.readLine()) != null) {
				String s[] = line.split(" ");
				if (s.length < 3) {
					continue;
				}
				PartitionInfo info = new PartitionInfo();
				info.setDevice(s[0]);
				info.setMountPoint(s[1]);
				info.setType(s[2]);
				partitions.put(s[1], info);
			}
		}catch (Exception e) {
			
		}
		
		return partitions;
	}
	
	
	public String remountPartition(String mountPoint, boolean readOnly) {
		 PartitionInfo info = m_partitions.get(mountPoint);
		 if (info == null) {
			 return null;
		 }
		 
		 String mountType = readOnly ? "ro" : "rw";
		 String command = "mount -o " + mountType + ",remount -t " + info.getType() + 
		 " " + info.getDevice() + " " + info.getMountPoint(); 
		 
		 return command;
	}
}
