package net.magictunnel.core;

/**
 * Interface for tunnel-related callbacks.
 * They must be all called in the UI thread context.
 * @author vitaly
 *
 */
public interface ITunnelStatusListener {
    /**
     * Triggered when a tunnel is set up.
     * @param name The name of the tunnel.
     */
    void onTunnelConnect(String name);

    /**
     * Triggered when a tunnel is disconnected.
     * @param name The name of the tunnel.
     */
    void onTunnelDisconnect(String name);
}
