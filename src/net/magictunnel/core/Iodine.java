package net.magictunnel.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import net.magictunnel.MagicTunnel;
import net.magictunnel.R;
import net.magictunnel.Utils;
import net.magictunnel.settings.Interfaces;
import net.magictunnel.settings.Profile;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.widget.Toast;

public class Iodine {
	private Profile m_activeProfile;
	
	private Commands m_cmds;
	private Context m_context;
	
	//TODO: clear the saved routes when connectivity changes
	List<RouteEntry> m_savedRoutes;
	
	private StringBuffer m_log = new StringBuffer();
	
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
	
	/**
	 * Reroutes the traffic through the tunnel
	 * @param transportInterface is either wifi or cellular NIC.
	 */
	public boolean setupRoute(String transportInterface) {
		
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

		InetAddress dnsIspServer = NetworkUtils.getDns();
		if (dnsIspServer == null) {
			return false;
		}
		
		
		m_savedRoutes = NetworkUtils.getRoutes();
		RouteEntry oldDefaultRoute = NetworkUtils.getDefaultRoute(m_savedRoutes, transportInterface);
		if (oldDefaultRoute == null) {
			return false;
		}

		InetAddress oldDefaultGateway = NetworkUtils.intToInetAddress(oldDefaultRoute.getGateway());
		
		NetworkUtils.removeDefaultRoute(transportInterface);
		NetworkUtils.addHostRoute(transportInterface, dnsIspServer, oldDefaultGateway);
		NetworkUtils.addDefaultRoute("dns0");
		
		return true;
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
			if (m_profile.getInterface().equals(Interfaces.CELLULAR)) {
				return NetworkUtils.getMobileInterface(m_context);
			}else {
				return NetworkUtils.getWifiInterface(m_context);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Interfaces iface = m_profile.getInterface();
			
			if (!NetworkUtils.checkConnectivity(m_context, iface)) {
				Utils.showErrorMessage(m_context, R.string.iodine_no_connectivity,
						iface.equals(Interfaces.WIFI) ?
						R.string.iodine_enable_wifi:R.string.iodine_enable_mobile);
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
