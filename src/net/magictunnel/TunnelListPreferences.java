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

package net.magictunnel;

import java.util.List;

import net.magictunnel.core.ITunnelStatusListener;
import net.magictunnel.core.Iodine;
import net.magictunnel.settings.Profile;
import net.magictunnel.settings.Settings;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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

/**
 * List of available tunnels with options to create new tunnels.
 * @author Vitaly
 *
 */
public class TunnelListPreferences extends PreferenceActivity implements
ITunnelStatusListener {

    /** Log menu ID. */
    private static final int MENU_LOG = Menu.FIRST;

    /** Donate menu ID. */
    //private static final int MENU_DONATE = Menu.FIRST + 1;

    /** About menu ID. */
    private static final int MENU_ABOUT = Menu.FIRST + 2;

    /** Delete profile confirmation id. */
    private static final int CONFIRM_DELETE_DIALOG_ID = 0;

    /** The index of the first tunnel in the list of items. */
    private int mFirstTunnelIndex = 0;

    /** Profile name to delete. */
    private String mProfileToDelete;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tunnellist);
        registerForContextMenu(getListView());

        MagicTunnel mt = ((MagicTunnel) getApplication());
        Iodine iod = mt.getIodine();
        iod.registerListener(this);

        populateScreen();
    }

    @Override
    protected final void onDestroy() {
        MagicTunnel mt = ((MagicTunnel) getApplication());
        Iodine iod = mt.getIodine();
        iod.unregisterListener(this);
        super.onDestroy();
    }

    /**
     * Fills in the screen with items.
     * The upper part of the screen is a special entry
     * that opens an activity for adding a new tunnel.
     */
    private void populateScreen() {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        screen.addPreference(createCategory(R.string.tunnel_mgmt));

        Preference pref = createAddNewTunnel();
        screen.addPreference(pref);

        pref = createCategory(R.string.available_tunnels);
        screen.addPreference(pref);
        mFirstTunnelIndex = pref.getOrder() + 1;
    }

    /**
     * Show the tunnel preferences activity.
     * @param name The tunnel that we want to edit.
     */
    private void showTunnelPreferences(final String name) {
        MagicTunnel app = (MagicTunnel) getApplication();
        Settings s = app.getSettings();
        s.setCurrentSettingsProfile(name);

        Intent intent = new Intent().setClass(TunnelListPreferences.this,
                TunnelPreferences.class);
        startActivity(intent);
    }

    /**
     * Creates a new preference category.
     * @param titleId The resource id of the title.
     * @return The preference category.
     */
    private PreferenceCategory createCategory(final int titleId) {
        PreferenceCategory pref = new PreferenceCategory(this);
        pref.setTitle(titleId);
        return pref;
    }

    /**
     * Creates a list entry for adding new tunnels.
     * @return The preference entry.
     */
    private Preference createAddNewTunnel() {
        Preference pref = new Preference(this);
        pref.setTitle(R.string.add_new_tunnel);

        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(final Preference preference) {
                showTunnelPreferences("");
                return true;
            }

        });
        return pref;
    }

    /**
     * Shows a menu when the user long-pressed a tunnel entry.
     * @param menu The menu
     * @param v The view
     * @param menuInfo The menu info.
     */
    @Override
    public final void onCreateContextMenu(
            final ContextMenu menu,
            final View v,
            final ContextMenuInfo menuInfo) {

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

    /**
     * Determines the name of the selected profile based on the
     * given menu information.
     * @param menuInfo The menu information.
     * @return The name of the selected profile, null if no profile selected.
     */
    private String getSelectedProfile(final AdapterContextMenuInfo menuInfo) {
        // excludes mVpnListContainer and the preferences above it
        int position = menuInfo.position - mFirstTunnelIndex;
        if (position < 0) {
            return null;
        }
        PreferenceScreen prefs = getPreferenceScreen();
        Preference pref = prefs.getPreference(menuInfo.position);
        return pref.getTitle().toString();
    }

    /** Disconnects iodine. */
    private void doDisconnect() {
        MagicTunnel mt = ((MagicTunnel) getApplication());
        Iodine iod = mt.getIodine();
        iod.disconnect();
    }

    /**
     * Connects the tunnel.
     * @param profileName The profile name where to take the settings from.
     */
    private void doConnect(final String profileName) {
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
        iod.getLauncher(p).execute((Void) null);
    }

    @Override
    public final boolean onContextItemSelected(final MenuItem item) {
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
            mProfileToDelete = profileName;
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
    protected final void onResume() {
        super.onResume();
        populateTunnels();
    }

    /**
     *
     * @return The name of the currently connected profile.
     */
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

    /**
     * Adds all the existing tunnels to the list of
     * available tunnels.
     */
    private void populateTunnels() {
        Preference pref;
        PreferenceScreen screen = getPreferenceScreen();
        MagicTunnel app = (MagicTunnel) getApplication();
        String activeProfile = getConnectedProfile();

        // Remove old tunnels from the list
        while (screen.getPreferenceCount() > mFirstTunnelIndex) {
            screen.removePreference(screen.getPreference(mFirstTunnelIndex));
        }

        Settings s = app.getSettings();
        List<String> profiles = s.getProfileNames();

        int position = mFirstTunnelIndex;
        for (String p : profiles) {
            pref = new Preference(this);
            pref.setTitle(p);

            if (activeProfile.equals(p)) {
                pref.setSummary(R.string.connected_tunnel);
            } else {
                pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(final Preference preference) {
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

    /**
     * Deletes the specified profile, then refreshes
     * the list of existing profiles.
     * @param profileName The profile to delete.
     */
    private void doDeleteProfile(final String profileName) {
        MagicTunnel app = (MagicTunnel) getApplication();
        Settings s = app.getSettings();
        Profile profile = s.getProfile(profileName);
        s.deleteProfile(profile, TunnelListPreferences.this);
        populateTunnels();
    }

    /**
     * Confirm the profile deletion.
     * @param id The dialog id.
     * @return The create confirm dialog.
     */
    @Override
    protected final Dialog onCreateDialog(final int id) {

        if (id == CONFIRM_DELETE_DIALOG_ID) {
            return new AlertDialog.Builder(this)
            .setTitle(android.R.string.dialog_alert_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.confirm_profile_deletion)
            .setPositiveButton(
                    R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int w) {
                            doDeleteProfile(mProfileToDelete);
                            mProfileToDelete = null;
                        }
                    })
             .setNegativeButton(R.string.no, null).create();
        }

        return super.onCreateDialog(id);
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_LOG, 0, R.string.main_menu_log)
            .setIcon(android.R.drawable.ic_menu_info_details);

        menu.add(0, MENU_ABOUT, 0, R.string.main_menu_about)
            .setIcon(android.R.drawable.ic_menu_help);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
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
    public final void onTunnelConnect(final String name) {
        populateTunnels();
    }

    @Override
    public final void onTunnelDisconnect(final String name) {
        populateTunnels();
    }

}
