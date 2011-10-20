package net.magictunnel.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Gathers all profile settings and provides
 * mechanisms to store these settings in the Android's
 * configuration store.
 *
 * Profile configuration entries are of the form:
 * profile_name_configuration
 *
 * For example: profile_mytunnel_domain.
 * @author Vitaly
 *
 */
public class Profile implements Comparable<Profile> {
    /** Prefix for every profile entry. */
    public static final String PROFILE_PREFIX = "profile_";

    /** Type configuration suffix. */
    public static final String PROFILE_TYPE = "_type";

    /** Domain configuration suffix. */
    public static final String PROFILE_DOMAIN = "_domain";

    /** Password configuration suffix. */
    public static final String PROFILE_PASSWORD = "_password";

    /** The value of PROFILE_TYPE for DNS tunneling. */
    public static final String PROFILE_TYPE_DNSTUNNEL = "dnstunnel";

    /** Profile name. */
    private String mName;

    /** Profile type (e.g., PROFILE_TYPE_DNSTUNNEL). */
    private String mType;

    /** Domain name of the tunnel. */
    private String mDomainName;

    /** Password to access the tunnel. */
    private String mPassword;

    /**
     * Creates a default DNS tunneling profile.
     */
    public Profile() {
        mType = PROFILE_TYPE_DNSTUNNEL;
    }

    /**
     * Get the name of the profile.
     * @return The profile name.
     */
    public final String getName() {
        return mName;
    }

    /**
     * Set the name of the profile.
     * @param name The profile name.
     */
    public final void setName(final String name) {
        mName = name;
    }

    /**
     * Get the domain name of the tunnel.
     * @return The domain name.
     */
    public final String getDomainName() {
        return mDomainName;
    }

    /**
     * Set the tunnel's domain name.
     * @param name The domain name.
     */
    public final void setDomainName(final String name) {
        mDomainName = name;
    }

    /**
     * Get the tunnel's password.
     * @return The password.
     */
    public final String getPassword() {
        return mPassword;
    }

    /**
     * Set the tunnel password.
     * @param password The password.
     */
    public final void setPassword(final String password) {
        mPassword = password;
    }

    @Override
    public final int compareTo(final Profile another) {
        return mName.compareTo(another.mName);
    }

    /**
     * Creates a Profile object from the data stored in
     * the Android's configuration store.
     * @param context The Android context where the configuration is stored.
     * @param name The name of the configuration entry.
     * @return The associated profile object.
     */
    public static Profile retrieveProfile(
            final Context context,
            final String name) {

        Profile prof = new Profile();
        prof.mName = name;

        String prefixedName = PROFILE_PREFIX + name;

        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);

        if (!prefs.contains(prefixedName + PROFILE_TYPE)) {
            return null;
        }

        prof.mType = prefs.getString(prefixedName + PROFILE_TYPE, "dnstunnel");
        if (!prof.mType.equals("dnstunnel")) {
            return null;
        }


        prof.mDomainName = prefs.getString(prefixedName + PROFILE_DOMAIN, "");
        prof.mPassword = prefs.getString(prefixedName + PROFILE_PASSWORD, "");
        return prof;
    }

    /**
     * Stores this profile into the Android's configuration store.
     * @param context The Android context.
     */
    public final void saveProfile(final Context context) {
        String prefixedName = PROFILE_PREFIX + mName;
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);

        Editor edit = prefs.edit();
        edit.putString(prefixedName + PROFILE_TYPE, mType);
        edit.putString(prefixedName + PROFILE_DOMAIN, mDomainName);
        edit.putString(prefixedName + PROFILE_PASSWORD, mPassword);
        edit.commit();
    }

    /**
     * Removes all profile entries from the Android's configuration store.
     * @param context The Android context.
     */
    public final void deleteProfile(final Context context) {
        String prefixedName = PROFILE_PREFIX + mName;
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);

        Editor edit = prefs.edit();
        edit.remove(prefixedName + PROFILE_TYPE);
        edit.remove(prefixedName + PROFILE_DOMAIN);
        edit.remove(prefixedName + PROFILE_PASSWORD);
        edit.commit();
    }
}
