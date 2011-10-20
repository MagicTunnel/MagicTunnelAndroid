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

package net.magictunnel.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This class manages configuration settings for all tunnels. There is only one
 * instance of this class in the system.
 *
 * @author Vitaly
 *
 */
public final class Settings {
    /** The unique instance of the settings class. */
    private static Settings sSettings = null;

    /** Mapping from profile names to profile objects. */
    private HashMap<String, Profile> mProfiles = new HashMap<String, Profile>();

    /** The active profile. */
    private String mCurrentSettingsProfile;

    /**
     * This class is not supposed to be instantiated by other clients.
     */
    private Settings() {

    }

    /**
     * Set the current active profile.
     * XXX: Move this elsewhere.
     * @param profile the profile name.
     */
    public void setCurrentSettingsProfile(final String profile) {
        mCurrentSettingsProfile = profile;
    }

    /**
     * Get the current active profile.
     * XXX: Move this elsewhere.
     * @return the profile name.
     */
    public String getCurrentSettingsProfile() {
        return mCurrentSettingsProfile;
    }


    /**
     * Fetches the settings of all profiles stored in the
     * Android's configuration store.
     * @param context The Android context.
     * @return A new settings object.
     */
    private static Settings retrieveSettings(final Context context) {
        Settings s = new Settings();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        Map<String, ?> settings = prefs.getAll();
        Set<String> keys = settings.keySet();
        Set<String> profiles = new HashSet<String>();

        // Get all profile names from the list of settings
        for (String k : keys) {
            String[] components = k.split("_");
            if (components.length > 1) {
                profiles.add(components[1]);
            }
        }

        for (String k : profiles) {
            Profile p = Profile.retrieveProfile(context, k);
            if (p != null) {
                s.mProfiles.put(k, p);
            }
        }
        return s;
    }

    /**
     * Gets the settings for all profiles.
     * @param context The Android context.
     * @return The unique instance of the settings object.
     */
    public static Settings get(final Context context) {
        if (sSettings != null) {
            return sSettings;
        }

        sSettings = retrieveSettings(context);
        return sSettings;
    }

    /**
     * Retrieves the list of all available profile names.
     * @return The profile names.
     */
    public List<String> getProfileNames() {
        ArrayList<String> ret = new ArrayList<String>(mProfiles.keySet());
        return ret;
    }

    /**
     * Get the profile object that corresponds to the specified
     * profile name.
     * @param name The profile name to retrieve.
     * @return The profile object.
     */
    public Profile getProfile(final String name) {
        return mProfiles.get(name);
    }

    /**
     * Renames a profile.
     * @param ctx The Android context.
     * @param oldName The old profile name.
     * @param newName The new profile name.
     * @return true if managed to rename the profile successfully.
     */
    public boolean rename(
            final Context ctx,
            final String oldName,
            final String newName) {

        Profile profile = mProfiles.get(oldName);
        if (profile == null) {
            return false;
        }
        if (oldName.equals(newName)) {
            return false;
        }
        profile.deleteProfile(ctx);
        mProfiles.remove(oldName);
        mProfiles.put(newName, profile);
        profile.setName(newName);
        profile.saveProfile(ctx);
        return true;
    }

    /**
     * Adds a new profile to the settings.
     * @param profile The profile to add.
     */
    public void addProfile(final Profile profile) {
        if (mProfiles.containsKey(profile)) {
            throw new RuntimeException("Profile already exists");
        }
        mProfiles.put(profile.getName(), profile);
    }

    /**
     * Deletes the specified profile from the settings.
     * @param profile The profile to delete.
     * @param ctx The Android context.
     */
    public void deleteProfile(final Profile profile, final Context ctx) {
        profile.deleteProfile(ctx);
        mProfiles.remove(profile.getName());
    }
}
