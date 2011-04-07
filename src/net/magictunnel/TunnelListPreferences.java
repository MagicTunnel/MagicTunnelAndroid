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
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;


public class TunnelListPreferences extends PreferenceActivity {
	private static final int CONFIRM_DELETE_DIALOG_ID = 0;
	private int m_firstTunnelIndex = 0;

	private String m_profileToDelete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.tunnellist);
		registerForContextMenu(getListView());
		populateScreen();
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
		MagicTunnel app = (MagicTunnel)getApplication();
		Settings s = app.getSettings();
		s.setCurrentSettingsProfile(name);
		
		Intent intent = new Intent().setClass(TunnelListPreferences.this, TunnelPreferences.class);
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

	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      
	  super.onCreateContextMenu(menu, v, menuInfo);
	  
	  String profile = getSelectedProfile((AdapterContextMenuInfo)menuInfo);
	  if (profile == null) {
		  return;
	  }
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.settingsmanager, menu);
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

    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

    	PreferenceScreen prefs = getPreferenceScreen();
        Preference pref = prefs.getPreference(info.position);
        String profileName = pref.getTitle().toString();
    	
    	  switch (item.getItemId()) {
    	  
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

	private void populateTunnels() {
		Preference pref;
		PreferenceScreen screen = getPreferenceScreen();
		MagicTunnel app = (MagicTunnel)getApplication();
		
		//Remove old tunnels from the list
		while (screen.getPreferenceCount() > m_firstTunnelIndex) {
			screen.removePreference(screen.getPreference(m_firstTunnelIndex));
		}
		
		Settings s = app.getSettings();
		List<String> profiles = s.getProfileNames();
		
		int position = m_firstTunnelIndex;
		for (String p:profiles) {
			pref = new Preference(this);
			pref.setTitle(p);
			pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					//showTunnelPreferences(preference.getTitle().toString());
					return true;
				}
			});

			pref.setOrder(position);
			screen.addPreference(pref);
			++position;
		}
		
	}

	private void doDeleteProfile(String profileName) {
		MagicTunnel app = (MagicTunnel)getApplication();
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
                                public void onClick(DialogInterface dialog, int w) {
                                	doDeleteProfile(m_profileToDelete);
                                	m_profileToDelete = null;
                                }
                            })
                    .setNegativeButton(R.string.no, null)
                    .create();
        }

        return super.onCreateDialog(id);
    }
	
}
