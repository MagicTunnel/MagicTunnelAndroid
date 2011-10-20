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
