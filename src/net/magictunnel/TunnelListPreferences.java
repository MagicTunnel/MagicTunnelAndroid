package net.magictunnel;

import java.util.List;

import net.magictunnel.core.ITunnelStatusListener;
import net.magictunnel.core.Iodine;
import net.magictunnel.settings.Profile;
import net.magictunnel.settings.Settings;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TunnelListPreferences extends PreferenceActivity implements
		ITunnelStatusListener {
	private static final int MENU_LOG = Menu.FIRST;
	private static final int MENU_DONATE = Menu.FIRST + 1;
	private static final int MENU_ABOUT = Menu.FIRST + 2;

	private static final int CONFIRM_DELETE_DIALOG_ID = 0;
	private int m_firstTunnelIndex = 0;

	private String m_profileToDelete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.tunnellist);
		registerForContextMenu(getListView());

		MagicTunnel mt = ((MagicTunnel) getApplication());
		Iodine iod = mt.getIodine();
		iod.registerListener(this);

		populateScreen();
	}

	@Override
	protected void onDestroy() {
		MagicTunnel mt = ((MagicTunnel) getApplication());
		Iodine iod = mt.getIodine();
		iod.unregisterListener(this);
		super.onDestroy();
	}

	private void populateScreen() {
		PreferenceScreen screen = getPreferenceScreen();
		screen.removeAll();

		screen.addPreference(createCategory(R.string.tunnel_mgmt));

		Preference pref = createAddNewTunnel();
		screen.addPreference(pref);

		pref = createCategory(R.string.available_tunnels);
		screen.addPreference(pref);
		m_firstTunnelIndex = pref.getOrder() + 1;
	}

	private void showTunnelPreferences(String name) {
		MagicTunnel app = (MagicTunnel) getApplication();
		Settings s = app.getSettings();
		s.setCurrentSettingsProfile(name);

		Intent intent = new Intent().setClass(TunnelListPreferences.this,
				TunnelPreferences.class);
		startActivity(intent);
	}

	private PreferenceCategory createCategory(int titleId) {
		PreferenceCategory pref = new PreferenceCategory(this);
		pref.setTitle(titleId);
		return pref;
	}

	private Preference createAddNewTunnel() {
		Preference pref = new Preference(this);
		pref.setTitle(R.string.add_new_tunnel);

		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				showTunnelPreferences("");
				return true;
			}

		});
		return pref;
	}

	/**
	 * Shows a menu when the user long-pressed a tunnel entry
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);

		String profile = getSelectedProfile((AdapterContextMenuInfo) menuInfo);
		if (profile == null) {
			return;
		}

		MenuInflater inflater = getMenuInflater();

		if (getConnectedProfile().equals(profile)) {
			inflater.inflate(R.menu.tunnellistpref_disconnect, menu);
		} else {
			inflater.inflate(R.menu.tunnellistpref_context, menu);
		}
		menu.setHeaderTitle(profile);
	}

	private String getSelectedProfile(AdapterContextMenuInfo menuInfo) {
		// excludes mVpnListContainer and the preferences above it
		int position = menuInfo.position - m_firstTunnelIndex;
		if (position < 0) {
			return null;
		}
		PreferenceScreen prefs = getPreferenceScreen();
		Preference pref = prefs.getPreference(menuInfo.position);
		return pref.getTitle().toString();
	}

	private void doDisconnect() {
		MagicTunnel mt = ((MagicTunnel) getApplication());
		Iodine iod = mt.getIodine();
		iod.disconnect();
	}

	private void doConnect(String profileName) {
		MagicTunnel mt = ((MagicTunnel) getApplication());
		Settings s = mt.getSettings();
		Profile p = s.getProfile(profileName);

		if (p == null) {
			return;
		}

		Iodine iod = mt.getIodine();
		if (!getConnectedProfile().equals("")) {
			iod.disconnect();
		}
		iod.setContext(this);
		iod.getLauncher(p).execute(null);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		PreferenceScreen prefs = getPreferenceScreen();
		Preference pref = prefs.getPreference(info.position);
		String profileName = pref.getTitle().toString();

		switch (item.getItemId()) {

		case R.id.cfg_menu_disconnect:
			doDisconnect();
			return true;

		case R.id.cfg_menu_connect:
			doConnect(profileName);
			return true;

		case R.id.cfg_menu_delete:
			m_profileToDelete = profileName;
			showDialog(CONFIRM_DELETE_DIALOG_ID);
			return true;

		case R.id.cfg_menu_change:
			showTunnelPreferences(profileName);
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateTunnels();
	}

	private String getConnectedProfile() {
		MagicTunnel app = (MagicTunnel) getApplication();
		Iodine iod = app.getIodine();

		boolean isConnected = iod.isIodineRunning();
		Profile activeProfile = iod.getActiveProfile();

		if (!isConnected || activeProfile == null) {
			return "";
		}
		return activeProfile.getName();
	}

	private void populateTunnels() {
		Preference pref;
		PreferenceScreen screen = getPreferenceScreen();
		MagicTunnel app = (MagicTunnel) getApplication();
		String activeProfile = getConnectedProfile();

		// Remove old tunnels from the list
		while (screen.getPreferenceCount() > m_firstTunnelIndex) {
			screen.removePreference(screen.getPreference(m_firstTunnelIndex));
		}

		Settings s = app.getSettings();
		List<String> profiles = s.getProfileNames();

		int position = m_firstTunnelIndex;
		for (String p : profiles) {
			pref = new Preference(this);
			pref.setTitle(p);

			if (activeProfile.equals(p)) {
				pref.setSummary(R.string.connected_tunnel);
			} else {
				pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						doConnect(preference.getTitle().toString());
						return true;
					}
				});
			}

			pref.setOrder(position);
			screen.addPreference(pref);
			++position;
		}

	}

	private void doDeleteProfile(String profileName) {
		MagicTunnel app = (MagicTunnel) getApplication();
		Settings s = app.getSettings();
		Profile profile = s.getProfile(profileName);
		s.deleteProfile(profile, TunnelListPreferences.this);
		populateTunnels();
	}

	/**
	 * Confirm the profile deletion
	 */
	@Override
	protected Dialog onCreateDialog(int id) {

		if (id == CONFIRM_DELETE_DIALOG_ID) {
			return new AlertDialog.Builder(this)
					.setTitle(android.R.string.dialog_alert_title)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(R.string.confirm_profile_deletion)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int w) {
									doDeleteProfile(m_profileToDelete);
									m_profileToDelete = null;
								}
							}).setNegativeButton(R.string.no, null).create();
		}

		return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_LOG, 0, R.string.main_menu_log).setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(0, MENU_ABOUT, 0, R.string.main_menu_about).setIcon(
				android.R.drawable.ic_menu_help);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case MENU_LOG:
			intent = new Intent().setClass(TunnelListPreferences.this,
					Log.class);
			startActivity(intent);
			break;

		case MENU_ABOUT:
			Utils.showAboutBox(this);
			break;
		default:
			return false;
		}

		return true;
	}

	@Override
	public void onTunnelConnect(String name) {
		populateTunnels();
	}

	@Override
	public void onTunnelDisconnet(String name) {
		populateTunnels();
	}

}
