package net.magictunnel.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.magictunnel.MagicTunnel;
import net.magictunnel.R;
import net.magictunnel.Utils;
import net.magictunnel.settings.Profile;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class Iodine {
	private Profile m_activeProfile;
	
	private Commands m_cmds;
	private Context m_context;
	
	//TODO: clear the saved routes when connectivity changes
	List<RouteEntry> m_savedRoutes;
	
	private StringBuffer m_log = new StringBuffer();
	private final Pattern m_rawPattern = Pattern.compile("directly to\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)");
	private final Pattern m_tunnelIp = Pattern.compile("Server tunnel IP is\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)");
	
	private ArrayList<ITunnelStatusListener> m_listeners = new ArrayList<ITunnelStatusListener>(); 
	
	public Iodine() {
		m_cmds = new Commands();
	}
	
	public void setContext(Context ctx) {
		m_context = ctx;
	}
	

	
	public StringBuffer getLog() {
		return m_log;
	}
	
	public Profile getActiveProfile() {
		return m_activeProfile;
	}
	
	public boolean isIodineRunning() {
		return Commands.isProgramRunning("iodine");
	}
	
	public void resetSavedRoutes() {
		m_savedRoutes = null;
	}
	
	/**
	 * Reroutes the traffic through the tunnel
	 * @param transportInterface is either wifi or cellular NIC.
	 * @param tunnelEntry is the address where data gets tunneled (e.g., ISP's DNS server
	 * or Iodine server in case of raw tunnel).
	 * @param serverTunnelIp is the IP address of the tunnels server's endpoint (e.g., 192.168.123.3)
	 */
	public boolean setupRoute(String transportInterface, InetAddress tunnelEntry,
			InetAddress serverTunnelIp) {
		
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

		m_savedRoutes = NetworkUtils.getRoutes();
		RouteEntry oldDefaultRoute = NetworkUtils.getDefaultRoute(m_savedRoutes, transportInterface);
		if (oldDefaultRoute == null) {
			return false;
		}

		InetAddress oldDefaultGateway = NetworkUtils.intToInetAddress(oldDefaultRoute.getGateway());
		
		NetworkUtils.removeDefaultRoute(transportInterface);
		NetworkUtils.addHostRoute(transportInterface, tunnelEntry, oldDefaultGateway);
		NetworkUtils.addDefaultRoute("dns0", serverTunnelIp);
		
		return true;
	}
	
	/**
	 * Sets up the most optimal route depending on whether or not
	 * raw connections are accepted by the network.
	 * @param transportInterface
	 * @return
	 */
	public boolean setupRoute(String transportInterface) {
		InetAddress serverTunnelIp = getServerTunnelIp(m_log);
		if (serverTunnelIp == null) {
			return false;
		}

		InetAddress raw = getRawEndpoint(m_log);
		if (raw != null) {
			return setupRoute(transportInterface, raw, serverTunnelIp);
		}else {
			InetAddress dnsIspServer = NetworkUtils.getDns();
			if (dnsIspServer == null) {
				return false;
			}
			return setupRoute(transportInterface, dnsIspServer, serverTunnelIp);
		}
	}
	
	/**
	 * Extracts the raw endpoint from the Iodine log messages.
	 * @param log
	 * @return
	 */
	InetAddress extractIpFromLog(StringBuffer log, Pattern p) {
		Matcher m = p.matcher(log.toString());

		if (!m.find()) {
			return null;
		}
		String addr = m.group(1);
		try {
			return InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
		}
		return null;
	}

	InetAddress getRawEndpoint(StringBuffer log) {
		return extractIpFromLog(log, m_rawPattern);
	}

	InetAddress getServerTunnelIp(StringBuffer log) {
		return extractIpFromLog(log, m_tunnelIp);
	}
	
	private StringBuilder buildCommandLine(Profile p) throws IodineException {
		StringBuilder cmdBuilder = new StringBuilder();
		cmdBuilder.append("iodine");
		
		if (p.getPassword() != null) {
			cmdBuilder.append(" -P ");
			cmdBuilder.append(p.getPassword());
		}
		
		cmdBuilder.append(" -d dns0 ");
		cmdBuilder.append(p.getDomainName());
		return cmdBuilder;
	}
	
	
	public void disconnect() {
		if (m_activeProfile == null) {
			return;
		}

		if (m_savedRoutes != null) {
			NetworkUtils.removeAllRoutes();
			NetworkUtils.restoreRoutes(m_savedRoutes);
			m_savedRoutes = null;
		}
		
		m_cmds.runCommandAsRoot("killall -9 iodine > /dev/null");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		showConnectionToast(R.string.iodine_notify_disconnected);
		broadcastOnTunnelDisconnect(m_activeProfile.getName());
		m_activeProfile = null;
	}

	private void showConnectionToast(int messageId) {
		String text = m_context.getString(messageId);
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(m_context, text, duration);
		toast.show();
	}

	
	public void registerListener(ITunnelStatusListener listener) {
		if (m_listeners.contains(listener)) {
			return;
		}
		m_listeners.add(listener);
	}
	
	public void unregisterListener(ITunnelStatusListener listener) {
		m_listeners.remove(listener);
	}
	
	private void broadcastOnTunnelConnect(String name) {
		for (ITunnelStatusListener l:m_listeners) {
			l.onTunnelConnect(name);
		}
	}

	private void broadcastOnTunnelDisconnect(String name) {
		for (ITunnelStatusListener l:m_listeners) {
			l.onTunnelDisconnet(name);
		}
	}
	
	
	/**
	 * An AsyncTask can be used only once. Therefore, we have
	 * to recreate a new one on each use.
	 * @return
	 */
	public LauncherTask getLauncher(Profile p) {
		return new LauncherTask(p);
	}
	
	public class LauncherTask extends AsyncTask<Void, String, Boolean> {
		private ArrayList<String> m_messages = new ArrayList<String>();
		private ProgressDialog m_progress;
		private Profile m_profile;
		
		private LauncherTask(Profile p) {
			m_profile = p;
		}
		
		private void launch() throws IodineException, InterruptedException {
			publishProgress("Killing previous instance of iodine...");
			m_cmds.runCommandAsRoot("killall -9 iodine > /dev/null");
			Thread.sleep(500);

			m_cmds.runCommandAsRoot(buildCommandLine(m_profile).toString());
			pollProgress(m_cmds.getProcess().getErrorStream());
		}

		
		private void pollProgress(InputStream is) {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			try {
				String l;
				while ((l = in.readLine()) != null) {
					publishProgress(l);
				}
			}catch(Exception e) {
				return;
			}		
		}

		private String getActiveInterface() {
			return NetworkUtils.getDefaultRouteIface();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			if (!NetworkUtils.checkConnectivity(m_context)) {
				Utils.showErrorMessage(m_context, R.string.iodine_no_connectivity,
						R.string.iodine_enable_wifi_or_mobile);
				cancel(true);
				return;
			}
			
			if (!NetworkUtils.checkRoutes(getActiveInterface())) {
				Utils.showErrorMessage(m_context,
						R.string.iodine_no_route,
						R.string.iodine_cycle_connection);
				cancel(true);
				return;
			}

			m_log = new StringBuffer();
			m_progress = new ProgressDialog(m_context);
			m_progress.setCancelable(false);
			m_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			m_progress.setMessage("Connecting...");
			m_progress.show();		
		}
	
		@Override
		protected Boolean doInBackground(Void... arg0) {
			try {
				launch();
				Thread.sleep(1000);
			}catch(IodineException e) {
				Utils.showErrorMessage(m_context, m_context.getString(e.getMessageId()));
			}catch(Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			m_progress.cancel();
			
			if (!NetworkUtils.interfaceExists("dns0")) {
				Utils.showErrorMessage(m_context, R.string.iodine_no_connectivity,
						R.string.iodine_check_dns);
				return;
			}
			
			String iface = getActiveInterface();

			if (!setupRoute(iface)) {
				Utils.showErrorMessage(m_context, R.string.iodine_no_connectivity,
						R.string.iodine_routing_error);
				return;
			}
			
			showConnectionToast(R.string.iodine_notify_connected);
			m_activeProfile = m_profile;
			broadcastOnTunnelConnect(m_profile.getName());
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			m_log.append(values[0]);
			m_log.append("\n");
			m_messages.add(values[0]);
			
			if (m_messages.size() > 5) {
				m_messages.remove(0);
			}
			
			StringBuffer buf = new StringBuffer();
			for (String s:m_messages) {
				buf.append(s + "\n");
			}
			
			m_progress.setMessage(buf.toString());
		}
	}
}
