package net.magictunnel.settings;

/**
 * Type of protocol to use for data transfer.
 * @author Vitaly
 *
 */
public enum DnsProtocol {
    /** Automatically detect the optimal protocol. */
    AUTODETECT,

    /** Use the NULL protocol. */
    NULL,

    /** Pack data in TXT records. */
    TXT,

    /** Pack data in SRV records. */
    SRV,

    /** Pack data in MX records. */
    MX,

    /** Pack data in CNAME records. */
    CNAME,

    /** Pack data in A records. */
    A
}
