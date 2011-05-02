package net.magictunnel.core;

/**
 * Interface for tunnel-related callbacks.
 * They must be all called in the UI thread context.
 * @author vitaly
 *
 */
public interface ITunnelStatusListener {
	public void onTunnelConnect(String name);
	public void onTunnelDisconnet(String name);
}
