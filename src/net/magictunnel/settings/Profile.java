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

    /** Packet size configuration suffix. */
    public static final String PROFILE_MAX_PACKET_SIZE = "_packet_size";

    /** Packet size configuration suffix. */
    public static final String PROFILE_DO_RAW_DETECTION = "_raw_detection";

    /** Tunnel encoding type configuration suffix. */
    public static final String PROFILE_ENCODING_TYPE = "_encoding";

    /** The value of PROFILE_TYPE for DNS tunneling. */
    public static final String PROFILE_TYPE_DNSTUNNEL = "dnstunnel";

    /** Profile name. */
    private String mName;

    /** Profile type (e.g., PROFILE_TYPE_DNSTUNNEL). */
    private String mType = PROFILE_TYPE_DNSTUNNEL;

    /** Domain name of the tunnel. */
    private String mDomainName;

    /** Password to access the tunnel. */
    private String mPassword;

    /**
     * Packet size to use when transmitting queries.
     * Zero for auto-detection.
     */
    private int mPacketSize = 0;

    /**
     * Speed up connection by skipping the detection
     * of whether it is possible to connect to the DNS
     * server directly. Since most of the time it is not
     * possible, this option speeds up connection.
     */
    private DnsRawConnection mRawConnection = DnsRawConnection.AUTODETECT;

    /**
     * Specifies which protocol to use for tunneling data
     * over the DNS tunnel.
     */
    private DnsProtocol mDnsProtocol = DnsProtocol.AUTODETECT;

    /**
     * Creates a default DNS tunneling profile.
     */
    public Profile() {

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

    /**
     * Set the maximum size of DNS packets.
     * @param packetSize The packet size
     */
    public final void setPacketSize(final int packetSize) {
        mPacketSize = packetSize;

        if (mPacketSize < 0) {
            mPacketSize = 0;
        }
    }

    /**
     * Get the current maximum packet size.
     * @return The packet size.
     */
    public final int getPacketSize() {
        return mPacketSize;
    }

    /**
     * Set raw connection detection.
     * @param b Whether to perform detection or not.
     */
    public final void setRawConnection(final DnsRawConnection b) {
        mRawConnection = b;
    }

    /**
     *
     * @return Whether to perform raw detection or not.
     */
    public final DnsRawConnection getRawConnection() {
        return mRawConnection;
    }

    /**
     *
     * @return Which protocol is currently used for encoding.
     */
    public final DnsProtocol getDnsProtocol() {
        return mDnsProtocol;
    }

    /**
     * Set the tunnel protocol.
     * @param protocol The protocol.
     */
    public final void setDnsProtocl(final DnsProtocol protocol) {
        mDnsProtocol = protocol;
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
        prof.mPacketSize = prefs.getInt(prefixedName + PROFILE_MAX_PACKET_SIZE, 0);
        prof.mRawConnection = DnsRawConnection.valueOf(prefs.getString(
                prefixedName + PROFILE_DO_RAW_DETECTION,
                DnsRawConnection.AUTODETECT.toString()));

        prof.mDnsProtocol = DnsProtocol.valueOf(prefs.getString(
                prefixedName + PROFILE_ENCODING_TYPE,
                DnsProtocol.AUTODETECT.toString()));
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
        edit.putString(prefixedName + PROFILE_ENCODING_TYPE, mDnsProtocol.toString());
        edit.putInt(prefixedName + PROFILE_MAX_PACKET_SIZE, mPacketSize);
        edit.putString(prefixedName + PROFILE_DO_RAW_DETECTION, mRawConnection.toString());
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
        edit.remove(prefixedName + PROFILE_ENCODING_TYPE);
        edit.remove(prefixedName + PROFILE_MAX_PACKET_SIZE);
        edit.remove(prefixedName + PROFILE_DO_RAW_DETECTION);
        edit.commit();
    }
}
