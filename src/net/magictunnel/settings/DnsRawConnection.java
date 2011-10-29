package net.magictunnel.settings;

/**
 * Whether or not to use raw connection for Dns tunneling or
 * to use automatic detection instead.
 * @author Vitaly
 *
 */
public enum DnsRawConnection {
    /** Autodetect optimal setting. */
    AUTODETECT,

    /** Use raw connection. */
    YES,

    /** Do not use raw connection. */
    NO
}
