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

import java.util.ArrayList;

import net.magictunnel.settings.DnsProtocol;
import net.magictunnel.settings.DnsRawConnection;
import net.magictunnel.settings.Profile;
import net.magictunnel.settings.Settings;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Activity for changing tunnel settings.
 * @author Vitaly
 *
 */
public class TunnelPreferences extends PreferenceActivity {

    /** Save menu ID. */
    private static final int MENU_SAVE = Menu.FIRST;

    /** Cancel menu ID. */
    private static final int MENU_CANCEL = Menu.FIRST + 1;

    /** Confirmation dialog ID. */
    private static final int CONFIRM_DIALOG_ID = 0;

    /** The recorded profile name. */
    private String mName = "";

    /** The recorded domain name. */
    private String mDomain = "";

    /** The recorded password. */
    private String mPassword = "";

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
    private DnsRawConnection mDoRawConnectionDetection = DnsRawConnection.AUTODETECT;

    /**
     * Specifies which protocol to use for tunneling data
     * over the DNS tunnel.
     */
    private DnsProtocol mDnsProtocol = DnsProtocol.AUTODETECT;

    /** The text box for the profile name. */
    private EditTextPreference mPrefName;

    /** The text box for the domain name. */
    private EditTextPreference mPrefDomain;

    /** The text box for the password. */
    private EditTextPreference mPrefPassword;

    /** The text box for maximum packet size. */
    private EditTextPreference mPrefMaxPacketSize;

    /** The list for raw connection detection. */
    private ListPreference mPrefDoRawDetection;

    /** The list of supported protocols. */
    private ListPreference mPrefDnsProtocol;

    /** Specifies whether the profile being edited already exists or not. */
    private boolean mNew;

    /** The settings repository. */
    private Settings mSettings;

    /** The profile being edited. */
    private Profile mProfile;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tunnelsettings);

        populatePreferenceScreen();
        fillInProperties();
    }

    /**
     * Retrieves the profile from the settings and fills in
     * the edit fields.
     */
    private void fillInProperties() {
        MagicTunnel app = (MagicTunnel) getApplication();
        mSettings = app.getSettings();
        String curProfile = mSettings.getCurrentSettingsProfile();
        if (curProfile.equals("")) {
            mNew = true;
            mProfile = new Profile();
            return;
        }

        Profile prof = mSettings.getProfile(curProfile);
        if (prof == null) {
            throw new RuntimeException("Could not retrive profile");
        }

        /********************/
        mName = prof.getName();
        mPrefName.getEditText().setText(prof.getName());
        mPrefName.setSummary(prof.getName());

        /********************/
        mDomain = prof.getDomainName();
        mPrefDomain.getEditText().setText(prof.getDomainName());
        mPrefDomain.setSummary(prof.getDomainName());

        /********************/
        mPassword = prof.getPassword();
        mPrefPassword.getEditText().setText(prof.getPassword());

        if (mPassword.length() == 0) {
            mPrefPassword.setSummary(R.string.profile_password_not_set);
            mPrefPassword.getEditText()
            .setHint(R.string.profile_password_not_set);
        } else {
            mPrefPassword.setSummary(R.string.profile_password_unchanged);
            mPrefPassword.getEditText()
            .setHint(R.string.profile_password_unchanged);
        }

        /********************/
        mPacketSize = prof.getPacketSize();
        mPrefMaxPacketSize.getEditText().setText(Integer.toString(mPacketSize));

        if (mPacketSize == 0) {
            mPrefMaxPacketSize.setSummary(R.string.autodetect);
        } else {
            mPrefMaxPacketSize.setSummary(Integer.toString(mPacketSize) + " bytes");
        }

        /********************/
        mDoRawConnectionDetection = prof.getRawConnection();
        mPrefDoRawDetection.setValue(mDoRawConnectionDetection.toString());
        String summary = mapListKeyToValue(mPrefDoRawDetection, mDoRawConnectionDetection.toString());
        mPrefDoRawDetection.setSummary(summary);


        /********************/
        mDnsProtocol = prof.getDnsProtocol();
        mPrefDnsProtocol.setValue(mDnsProtocol.toString());
        summary = mapListKeyToValue(mPrefDnsProtocol, mDnsProtocol.toString());
        mPrefDnsProtocol.setSummary(summary);

        mProfile = prof;
    }

    /**
     * Checks whether all fields are properly set.
     * @return The message in case of an error.
     */
    private String validate() {
        if (!mName.equals(mProfile.getName())) {
            Profile prof = mSettings.getProfile(mName);
            if (prof != null) {
                return getString(R.string.profile_exists);
            }
        }

        if (mName.equals("")) {
            return getString(R.string.profile_enter_name);
        }

        if (mDomain.equals("")) {
            return getString(R.string.profile_enter_domain);
        }

        return null;
    }

    /**
     * Saves the modification into the settings store.
     */
    private void saveProperties() {
        mProfile.setDomainName(mDomain);
        mProfile.setPassword(mPassword);
        mProfile.setRawConnection(mDoRawConnectionDetection);
        mProfile.setPacketSize(mPacketSize);
        mProfile.setDnsProtocl(mDnsProtocol);

        if (mNew) {
            mProfile.setName(mName);
            mSettings.addProfile(mProfile);
            mProfile.saveProfile(this);
        } else if (!mProfile.getName().equals(mName)) {
            mSettings.rename(this, mProfile.getName(), mName);
        } else {
            mProfile.saveProfile(this);
        }
    }

    /**
     *
     * @return Whether the user made any changes to the profile.
     */
    private boolean profileChanged() {
        return !mDomain.equals(mProfile.getDomainName())
        || !mPassword.equals(mProfile.getPassword())
        || !mName.equals(mProfile.getName())
        || mPacketSize != mProfile.getPacketSize()
        || !mDoRawConnectionDetection.equals(mProfile.getRawConnection())
        || !mDnsProtocol.equals(mProfile.getDnsProtocol());
    }

    /**
     * Creates the edit fields on the screen.
     */
    private void populatePreferenceScreen() {
        PreferenceScreen screen = getPreferenceScreen();

        PreferenceCategory cat = new PreferenceCategory(this);
        String catName = getString(R.string.dns_tunnel_settings);
        cat.setTitle(catName);
        screen.addPreference(cat);

        mPrefName = createNamePreference();
        screen.addPreference(mPrefName);

        mPrefDomain = createDomainPreference();
        screen.addPreference(mPrefDomain);

        mPrefPassword = createPasswordPreference();
        screen.addPreference(mPrefPassword);
        mPrefPassword.setSummary(R.string.profile_password_not_set);
        mPrefPassword.getEditText().setHint(R.string.profile_password_not_set);

        mPrefDnsProtocol = createProtocolPreference();
        screen.addPreference(mPrefDnsProtocol);

        mPrefMaxPacketSize = createPacketSizePreference();
        screen.addPreference(mPrefMaxPacketSize);

        mPrefDoRawDetection = createConnectionDetectionPreference();
        screen.addPreference(mPrefDoRawDetection);
    }

    /**
     * @return The profile name edit field.
     */
    private EditTextPreference createNamePreference() {
        EditTextPreference prefName = new EditTextPreference(this);
        prefName.setTitle(R.string.profile_name);
        prefName.setDialogTitle(R.string.profile_name);
        prefName.getEditText().setInputType(
                prefName.getEditText().getInputType()
                & ~InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        prefName.setOnPreferenceChangeListener(
            new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                    mName = ((String) newValue).trim();
                    preference.setSummary(mName);
                    return true;
                }
            });
        return prefName;
    }

    /**
     * @return The domain name edit field.
     */
    private EditTextPreference createDomainPreference() {
        EditTextPreference prefDomain = new EditTextPreference(this);
        prefDomain.setTitle(R.string.domain_name);
        prefDomain.setDialogTitle(R.string.domain_name);

        prefDomain.getEditText().setInputType(
                prefDomain.getEditText().getInputType()
                & ~InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        prefDomain.setOnPreferenceChangeListener(
            new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                    mDomain = ((String) newValue).trim();
                    preference.setSummary(mDomain);
                    return true;
                }
            });
        return prefDomain;
    }

    /**
     * @return The password edit field.
     */
    private EditTextPreference createPasswordPreference() {
        EditTextPreference prefPassword = new EditTextPreference(this);
        prefPassword.setTitle(R.string.password);
        prefPassword.setDialogTitle(R.string.password);
        prefPassword.getEditText().setInputType(
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        prefPassword.getEditText().setTransformationMethod(
                new PasswordTransformationMethod());
        prefPassword.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                mPassword = ((String) newValue);
                EditTextPreference prefPassword = (EditTextPreference) preference;
                mPrefPassword.setSummary(R.string.profile_password_changed);
                prefPassword.getEditText().setHint("");
                return true;
            }
        });

        return prefPassword;
    }

    /**
     * @return The packet size field.
     */
    private EditTextPreference createPacketSizePreference() {
        EditTextPreference packetSizePreference = new EditTextPreference(this);

        packetSizePreference.setTitle(R.string.packet_size);
        packetSizePreference.setDialogTitle(R.string.packet_size);
        packetSizePreference.getEditText().setInputType(
                InputType.TYPE_CLASS_NUMBER);
        packetSizePreference.setSummary(R.string.autodetect);

        packetSizePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                try {
                    mPacketSize = Integer.parseInt((String) newValue);
                } catch (NumberFormatException e) {
                    mPacketSize = 0;
                }

                if (mPacketSize == 0) {
                    mPrefMaxPacketSize.setSummary(R.string.autodetect);
                } else {
                    mPrefMaxPacketSize.setSummary(Integer.toString(mPacketSize) + " bytes");
                }
                return true;
            }
        });

        return packetSizePreference;
    }

    /**
     * Maps a key to a displayable value.
     * @param pref The preference list.
     * @param key The key corresponding to the value we want.
     * @return The value.
     */
    private static String mapListKeyToValue(ListPreference pref, String key) {
        CharSequence[] entryValues = pref.getEntryValues();
        for (int index = 0; index < entryValues.length; ++index) {
            if (entryValues[index].toString().equals(key)) {
                return pref.getEntries()[index].toString();
            }
        }
        return null;
    }

    /**
     * @return The raw connection detection check box.
     */
    private ListPreference createConnectionDetectionPreference() {
        ListPreference detectionPreference = new ListPreference(this);

        detectionPreference.setTitle(R.string.raw_detection_short);
        detectionPreference.setDialogTitle(R.string.raw_detection_title);

        detectionPreference.setEntries(R.array.direct_connection_values);
        detectionPreference.setEntryValues(R.array.direct_connection_keys);

        String summary = mapListKeyToValue(detectionPreference, mDoRawConnectionDetection.toString());
        detectionPreference.setSummary(summary);

        detectionPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                ListPreference listPref = (ListPreference) preference;
                CharSequence value = (CharSequence) newValue;
                mDoRawConnectionDetection = DnsRawConnection.valueOf(value.toString());

                String summary = mapListKeyToValue(listPref, value.toString());
                preference.setSummary(summary);
                return true;
            }
        });

        return detectionPreference;
    }

    /**
     * @return The raw connection detection check box.
     */
    private ListPreference createProtocolPreference() {
        ListPreference protocolPreference = new ListPreference(this);

        protocolPreference.setTitle(R.string.protocol_type);
        protocolPreference.setDialogTitle(R.string.protocol_type);

        protocolPreference.setEntries(R.array.protocol_values);
        protocolPreference.setEntryValues(R.array.protocol_keys);

        String summary = mapListKeyToValue(protocolPreference, mDnsProtocol.toString());
        protocolPreference.setSummary(summary);

        protocolPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                ListPreference listPref = (ListPreference) preference;
                CharSequence value = (CharSequence) newValue;
                mDnsProtocol = DnsProtocol.valueOf(value.toString());

                String summary = mapListKeyToValue(listPref, value.toString());
                preference.setSummary(summary);
                return true;
            }
        });

        return protocolPreference;
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SAVE, 0, R.string.save).setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, MENU_CANCEL, 0, R.string.cancel).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    @Override
    public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return doCancel();
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Checks whether the changes are valid and saves them.
     * @return true if successfully saved.
     */
    public final boolean validateAndSaveResult() {
        String error = validate();
        if (error != null) {
            Utils.showErrorMessage(this, error);
            return false;
        } else {
            saveProperties();
        }
        return true;
    }

    /**
     * Cancels the changes after showing a confirmation dialog
     * in case something was modified.
     * @return true.
     */
    public final boolean doCancel() {
        if (profileChanged()) {
            showDialog(CONFIRM_DIALOG_ID);
        } else {
            finish();
        }
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE:
                if (validateAndSaveResult()) {
                    finish();
                }
                return true;

            case MENU_CANCEL:
                return doCancel();

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected final Dialog onCreateDialog(final int id) {

        if (id == CONFIRM_DIALOG_ID) {
            int messageId = R.string.confirm_edit_profile_cancellation;
            if (mNew) {
                messageId = R.string.confirm_add_profile_cancellation;
            }

            return new AlertDialog.Builder(this)
            .setTitle(android.R.string.dialog_alert_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(messageId)
            .setPositiveButton(
                    R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int w) {
                            finish();
                        }
                    })
            .setNegativeButton(R.string.no, null)
            .create();
        }

        return super.onCreateDialog(id);
    }
}
