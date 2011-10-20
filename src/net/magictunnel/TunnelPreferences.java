package net.magictunnel;

import net.magictunnel.settings.Profile;
import net.magictunnel.settings.Settings;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
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

    /** The text box for the profile name. */
    private EditTextPreference mPrefName;

    /** The text box for the domain name. */
    private EditTextPreference mPrefDomain;

    /** The text box for the password. */
    private EditTextPreference mPrefPassword;

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

        mName = prof.getName();
        mPrefName.getEditText().setText(prof.getName());
        mPrefName.setSummary(prof.getName());

        mDomain = prof.getDomainName();
        mPrefDomain.getEditText().setText(prof.getDomainName());
        mPrefDomain.setSummary(prof.getDomainName());

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
        || !mName.equals(mProfile.getName());
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
