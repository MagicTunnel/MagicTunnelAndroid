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
		// Get the custom preference
		Preference addNewTunnel = (Preference) findPreference("addNewTunnel");
		addNewTunnel
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						showTunnelPreferences("");
						return true;
					}

				});
		populate();
	}
	
	private void showTunnelPreferences(String name) {
		MagicTunnel app = (MagicTunnel)getApplication();
		Settings s = app.getSettings();
		s.setCurrentSettingsProfile(name);
		
		Intent intent = new Intent().setClass(TunnelListPreferences.this, TunnelPreferences.class);
		startActivity(intent);
	}
	
	private void promptNewProfileName() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.create_profile);
		alert.setMessage(R.string.enter_profile_name);
		
		final EditText input = new EditText(this);
		input.setMaxLines(1);
		input.setInputType(input.getInputType() & ~InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		alert.setView(input);
		
		
		//XXX: Check if profile name already exists
		alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  String value = input.getText().toString().trim();
			  if (value.length() != 0) {
				  showTunnelPreferences(value);				  
			  }
			  }
			});

		alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    dialog.cancel();
			  }
			});

		alert.show();
	}

	private void populate() {
		MagicTunnel app = (MagicTunnel)getApplication();
		Settings s = app.getSettings();
		List<String> profiles = s.getProfileNames();
		
		PreferenceScreen screen = getPreferenceScreen();
		
		for (String p:profiles) {
			Preference pref = new Preference(this);
			pref.setTitle(p);
			pref.setKey(Profile.PROFILE_PREFIX + p);
			screen.addPreference(pref);
		}
		
	}
	
}
