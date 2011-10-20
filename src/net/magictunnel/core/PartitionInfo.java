package net.magictunnel.core;

/**
 * Models a partition table entry.
 * @author Vitaly
 *
 */
public class PartitionInfo {
    /**
     * Name of the device (e.g., /dev/partition).
     */
    private String mDevice;

    /**
     * Mount point location (e.g., /mnt/partition).
     */
    private String mMountPoint;

    /**
     * File system type.
     */
    private String mType;



    /**
     * Get the name of the device.
     * @return The device name.
     */
    public final String getDevice() {
        return mDevice;
    }

    /**
     * Set the name of the device.
     * @param device The device name.
     */
    public final void setDevice(final String device) {
        this.mDevice = device;
    }

    /**
     * Get the mount point.
     * @return The mount point.
     */
    public final String getMountPoint() {
        return mMountPoint;
    }

    /**
     * Set the mount point.
     * @param mountPoint The mount point.
     */
    public final void setMountPoint(final String mountPoint) {
        this.mMountPoint = mountPoint;
    }

    /**
     * Get the partition type.
     * @return The partition type.
     */
    public final String getType() {
        return mType;
    }

    /**
     * Set the partition type.
     * @param type The partition type.
     */
    public final void setType(final String type) {
        this.mType = type;
    }
}
