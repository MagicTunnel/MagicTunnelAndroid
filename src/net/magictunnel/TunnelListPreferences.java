package net.magictunnel;

import java.util.List;

import net.magictunnel.settings.Profile;
import net.magictunnel.settings.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.Toast;


public class TunnelListPreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.tunnellist);
		populate();
	}

	private void showTunnelPreferences(String name) {
		MagicTunnel app = (MagicTunnel)getApplication();
		Settings s = app.getSettings();
		s.setCurrentSettingsProfile(name);
		
		Intent intent = new Intent().setClass(TunnelListPreferences.this, TunnelPreferences.class);
		startActivity(intent);
	}

	private Preference createCategory(int titleId) {
		Preference pref = new PreferenceCategory(this);
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

	@Override
	protected void onResume() {
		super.onResume();
		populate();
	}

	private void populate() {
		PreferenceScreen screen = getPreferenceScreen();
		screen.removeAll();

		screen.addPreference(createCategory(R.string.tunnel_mgmt));

		Preference pref = createAddNewTunnel();
		screen.addPreference(pref);

		screen.addPreference(createCategory(R.string.available_tunnels));

		MagicTunnel app = (MagicTunnel)getApplication();
		Settings s = app.getSettings();
		List<String> profiles = s.getProfileNames();

		for (String p:profiles) {
			pref = new Preference(this);
			pref.setTitle(p);
			pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					showTunnelPreferences(preference.getTitle().toString());
					return true;
				}
			});

			screen.addPreference(pref);
		}
		
	}

}
