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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.magictunnel.R;
import net.magictunnel.Utils;
import net.magictunnel.settings.DnsProtocol;
import net.magictunnel.settings.DnsRawConnection;
import net.magictunnel.settings.Profile;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * This class interfaces with the Iodine client.
 * @author Vitaly
 *
 */
public class Iodine {
    /**
     * Pattern to extract the address of the remote DNS tunnel server
     * in case direct connection is possible.
     */
    private static final Pattern IODINE_RAW_PATTERN =
        Pattern.compile("directly to\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)");

    /** Pattern to extract the address of the DNS tunnel server. */
    private static final Pattern IODINE_SERVER_TUNNEL_PATTERN =
        Pattern.compile("Server tunnel IP is\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)");


    /** How many lines to display at once in the progress dialog. */
    private static final int MAX_PROGRESS_LINES = 5;

    /** How much time to wait for certain commands. */
    private static final int SLEEP_TIME = 1000;

    /** The name of the profile currently running. */
    private Profile mActiveProfile;

    /** For issuing commands. */
    private Commands mCmds;

    /** The current Android context. */
    private Context mContext;

    //TODO clear the saved routes when connectivity changes
    /** Routes that were active before Ioding was enabled. */
    private List<RouteEntry> mSavedRoutes;

    /** The log output of the connection process. */
    private StringBuffer mLog = new StringBuffer();


    /**
     * All the folks interested in being notified
     * of connectivity changes.
     */
    private ArrayList<ITunnelStatusListener> mListeners =
        new ArrayList<ITunnelStatusListener>();

    /**
     * Create the interface with the Ioding client.
     */
    public Iodine() {
        mCmds = new Commands();
    }

    /**
     * Initialize the Android context.
     * @param ctx The context.
     */
    public final void setContext(final Context ctx) {
        mContext = ctx;
    }

    /**
     * @return The log buffer.
     */
    public final StringBuffer getLog() {
        return mLog;
    }

    /**
     * @return The current active profile.
     */
    public final Profile getActiveProfile() {
        return mActiveProfile;
    }

    /**
     * @return Whether the Iodine client is running or not.
     */
    public final boolean isIodineRunning() {
        return Commands.isProgramRunning("iodine");
    }

    /** Reset all saved routes (e.g., when they become invalid). */
    public final void resetSavedRoutes() {
        mSavedRoutes = null;
    }

    /**
     * Reroutes the traffic through the tunnel.
     * @param transportInterface is either wifi or cellular NIC.
     *
     * @param tunnelEntry is the address where data
     * gets tunneled (e.g., ISP's DNS server
     * or Iodine server in case of raw tunnel).
     *
     * @param serverTunnelIp is the IP address of the tunnels
     * server's endpoint (e.g., 192.168.123.3)
     *
     * @return success status.
     */
    public final boolean setupRoute(
            final String transportInterface,
            final InetAddress tunnelEntry,
            final InetAddress serverTunnelIp) {

        NetworkInterface ni;

        try {
            ni = NetworkInterface.getByName("dns0");
        } catch (SocketException e) {
            return false;
        }

        Enumeration<InetAddress> addresses = ni.getInetAddresses();
        if (!addresses.hasMoreElements()) {
            return false;
        }

        mSavedRoutes = NetworkUtils.getRoutes();
        RouteEntry oldDefaultRoute =
            NetworkUtils.getDefaultRoute(mSavedRoutes, transportInterface);

        if (oldDefaultRoute == null) {
            return false;
        }

        InetAddress oldDefaultGateway =
            NetworkUtils.intToInetAddress(oldDefaultRoute.getGateway());

        NetworkUtils.removeDefaultRoute(transportInterface);

        NetworkUtils.addHostRoute(transportInterface,
                tunnelEntry, oldDefaultGateway);

        NetworkUtils.addDefaultRoute("dns0", serverTunnelIp);

        return true;
    }

    /**
     * Sets up the most optimal route depending on whether or not
     * raw connections are accepted by the network.
     * @param transportInterface Where to route the traffic through.
     * @return the success status.
     */
    public final boolean setupRoute(final String transportInterface) {
        InetAddress serverTunnelIp = getServerTunnelIp(mLog);
        if (serverTunnelIp == null) {
            return false;
        }

        InetAddress raw = getRawEndpoint(mLog);
        if (raw != null) {
            return setupRoute(transportInterface, raw, serverTunnelIp);
        } else {
            InetAddress dnsIspServer = NetworkUtils.getDns();
            if (dnsIspServer == null) {
                return false;
            }
            return setupRoute(transportInterface, dnsIspServer, serverTunnelIp);
        }
    }

    /**
     * Extracts the raw endpoint from the Iodine log messages.
     * @param log The output generated by the Iodine client.
     * @param p The pattern for IP extraction.
     * @return The IP address.
     */
    final InetAddress extractIpFromLog(
            final StringBuffer log,
            final Pattern p) {
        Matcher m = p.matcher(log.toString());

        if (!m.find()) {
            return null;
        }
        String addr = m.group(1);
        try {
            return InetAddress.getByName(addr);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Extract the raw endpoint of the DNS tunnel, in case the ISP allows
     * direct UDP traffic to the DNS tunnel server.
     * @param log The output generated by the Iodine client.
     * @return The IP address.
     */
    final InetAddress getRawEndpoint(final StringBuffer log) {
        return extractIpFromLog(log, IODINE_RAW_PATTERN);
    }

    /**
     * Extract IP address of the DNS tunnel server.
     * @param log The output generated by the Iodine client.
     * @return The IP address.
     */
    final InetAddress getServerTunnelIp(final StringBuffer log) {
        return extractIpFromLog(log, IODINE_SERVER_TUNNEL_PATTERN);
    }

    /**
     * Build a command line to launch the Iodine client.
     * This command line is determined by the profile settings.
     * @param p The profile.
     * @return The command string.
     */
    private StringBuilder buildCommandLine(final Profile p) {
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append("iodine");

        if (p.getPassword() != null) {
            cmdBuilder.append(" -P ");
            cmdBuilder.append(p.getPassword());
        }

        if (p.getPacketSize() > 0) {
            cmdBuilder.append(" -m");
            cmdBuilder.append(Integer.toString(p.getPacketSize()));
        }

        if (!p.getDnsProtocol().equals(DnsProtocol.AUTODETECT)) {
            cmdBuilder.append(" -T");
            cmdBuilder.append(p.getDnsProtocol().toString());
        }

        if (p.getRawConnection().equals(DnsRawConnection.NO)) {
            cmdBuilder.append(" -r");
        }

        cmdBuilder.append(" -d dns0 ");
        cmdBuilder.append(p.getDomainName());
        return cmdBuilder;
    }


    /**
     * Disconnects the tunnel.
     */
    public final void disconnect() {
        if (mActiveProfile == null) {
            return;
        }

        if (mSavedRoutes != null) {
            NetworkUtils.removeAllRoutes();
            NetworkUtils.restoreRoutes(mSavedRoutes);
            mSavedRoutes = null;
        }

        mCmds.runCommandAsRoot("killall -9 iodine > /dev/null");
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        showConnectionToast(R.string.iodine_notify_disconnected);
        broadcastOnTunnelDisconnect(mActiveProfile.getName());
        mActiveProfile = null;
    }

    /**
     * Show a message toast on the screen.
     * @param messageId The ID of the message to show.
     */
    private void showConnectionToast(final int messageId) {
        String text = mContext.getString(messageId);
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();
    }


    /**
     * Register a listener for connect/disconnect events.
     * @param listener The listener.
     */
    public final void registerListener(final ITunnelStatusListener listener) {
        if (mListeners.contains(listener)) {
            return;
        }
        mListeners.add(listener);
    }

    /**
     * Unregister a listener for connect/disconnect events.
     * @param listener The listener.
     */
    public final void unregisterListener(final ITunnelStatusListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Notifies all listeners that the tunnel got connected.
     * @param name The name of the profile.
     */
    private void broadcastOnTunnelConnect(final String name) {
        for (ITunnelStatusListener l : mListeners) {
            l.onTunnelConnect(name);
        }
    }

    /**
     * Notifies all listeners that the tunnel got disconnected.
     * @param name The name of the profile.
     */
    private void broadcastOnTunnelDisconnect(final String name) {
        for (ITunnelStatusListener l : mListeners) {
            l.onTunnelDisconnect(name);
        }
    }


    /**
     * An AsyncTask can be used only once. Therefore, we have
     * to recreate a new one on each use.
     * @param p The profile to activate.
     * @return The launcher task.
     */
    public final LauncherTask getLauncher(final Profile p) {
        return new LauncherTask(p);
    }

    /**
     * Asynchronous task to launch the Iodine client.
     * @author Vitaly
     *
     */
    public final class LauncherTask extends AsyncTask<Void, String, Boolean> {
        /** The progress dialog. */
        private ProgressDialog mProgress;

        /** List of messages to display in the progress dialog. */
        private ArrayList<String> mMessages = new ArrayList<String>();

        /** The profile to activate. */
        private Profile mProfile;

        /**
         * Create a new launcher task.
         * @param p The profile to activate.
         */
        private LauncherTask(final Profile p) {
            mProfile = p;
        }

        /**
         * Launch the iodine process.
         * @throws InterruptedException if the task is interrupted.
         */
        private void launch() throws InterruptedException {
            publishProgress("Killing previous instance of iodine...");
            mCmds.runCommandAsRoot("killall -9 iodine > /dev/null");
            Thread.sleep(SLEEP_TIME);

            mCmds.runCommandAsRoot(buildCommandLine(mProfile).toString());
            pollProgress(mCmds.getProcess().getErrorStream());
        }


        /**
         * Check if Iodine generated additional output and display it
         * on the progress status dialog.
         * @param is The input stream of the process.
         */
        private void pollProgress(final InputStream is) {
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            try {
                String l;
                while ((l = in.readLine()) != null) {
                    publishProgress(l);
                }
            } catch (Exception e) {
                return;
            }
        }

        /**
         * Determines the interface through which all traffic goes.
         * @return the interface name.
         */
        private String getActiveInterface() {
            return NetworkUtils.getDefaultRouteIface();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!NetworkUtils.checkConnectivity(mContext)) {
                Utils.showErrorMessage(mContext,
                        R.string.iodine_no_connectivity,
                        R.string.iodine_enable_wifi_or_mobile);
                cancel(true);
                return;
            }

            if (!NetworkUtils.checkRoutes(getActiveInterface())) {
                Utils.showErrorMessage(mContext,
                        R.string.iodine_no_route,
                        R.string.iodine_cycle_connection);
                cancel(true);
                return;
            }

            mLog = new StringBuffer();
            mProgress = new ProgressDialog(mContext);
            mProgress.setCancelable(false);
            mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgress.setMessage("Connecting...");
            mProgress.show();
        }

        @Override
        protected Boolean doInBackground(final Void... arg0) {
            try {
                launch();
                Thread.sleep(SLEEP_TIME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            super.onPostExecute(result);
            mProgress.cancel();

            if (!NetworkUtils.interfaceExists("dns0")) {
                Utils.showErrorMessage(mContext,
                        R.string.iodine_no_connectivity,
                        R.string.iodine_check_dns);
                return;
            }

            String iface = getActiveInterface();

            if (!setupRoute(iface)) {
                Utils.showErrorMessage(mContext,
                        R.string.iodine_no_connectivity,
                        R.string.iodine_routing_error);
                return;
            }

            showConnectionToast(R.string.iodine_notify_connected);
            mActiveProfile = mProfile;
            broadcastOnTunnelConnect(mProfile.getName());
        }

        @Override
        protected void onProgressUpdate(final String... values) {
            super.onProgressUpdate(values);
            mLog.append(values[0]);
            mLog.append("\n");
            mMessages.add(values[0]);

            if (mMessages.size() > MAX_PROGRESS_LINES) {
                mMessages.remove(0);
            }

            StringBuffer buf = new StringBuffer();
            for (String s : mMessages) {
                buf.append(s + "\n");
            }

            mProgress.setMessage(buf.toString());
        }
    }
}
