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

import net.magictunnel.core.Commands;
import net.magictunnel.core.Installer;
import net.magictunnel.core.Tunnel;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * The very first activity the user sees.
 * It displays the checklist of installed/missing components
 * and proceeds with the installation if all dependencies are met.
 * @author Vitaly
 *
 */
public class SystemComponentChecklist extends PreferenceActivity {

    /** Whether the user can obtain root access. */
    private boolean mHasRoot = false;

    /** Whether there is a TAP driver installed. */
    private boolean mHasTun = false;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.checklist);

        if (Installer.iodineInstalled()) {
            Intent intent = new Intent().setClass(this, TunnelListPreferences.class);
            startActivity(intent);
            finish();
        }

        populateScreen();
    }

    /** Creates the actual screen. */
    private void populateScreen() {
        PreferenceScreen screen = getPreferenceScreen();

        PreferenceCategory catChecklist = new PreferenceCategory(this);
        catChecklist.setTitle(R.string.checklist_category);

        CheckBoxPreference prefRootAccess = createCustomCheckBox(R.string.checklist_root);
        mHasRoot = Commands.checkRoot();
        prefRootAccess.setChecked(mHasRoot);


        CheckBoxPreference prefTun = createCustomCheckBox(R.string.checklist_tun);
        mHasTun = Tunnel.checkTap();
        prefTun.setChecked(mHasTun);


        PreferenceCategory catAction = new PreferenceCategory(this);
        catAction.setTitle(R.string.checklist_category_action);

        screen.addPreference(catChecklist);
        screen.addPreference(prefRootAccess);
        screen.addPreference(prefTun);
        screen.addPreference(catAction);

        if (mHasRoot && mHasTun) {
            addProceedButton();
        } else {
            addHelpButton();
        }
    }

    /**
     * Custom checkbox.
     * @param id The resource id of the title.
     * @return the checkbox.
     */
    final CheckBoxPreference createCustomCheckBox(final int id) {
        CheckBoxPreference checkbox = new CheckBoxPreference(this);
        checkbox.setTitle(id);
        //checkbox.setWidgetLayoutResource(R.layout.checkbox);
        checkbox.setEnabled(false);

        return checkbox;
    }

    /**
     * Displays install button if all dependencies are satisfied.
     */
    private void addProceedButton() {
        PreferenceScreen screen = getPreferenceScreen();

        Preference prefProceed = new Preference(this);
        prefProceed.setTitle(R.string.checklist_proceed);
        prefProceed.setSummary(R.string.checklist_proceed_subtitle);

        prefProceed.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(final Preference preference) {
                Intent intent = new Intent().setClass(preference.getContext(),
                        TunnelListPreferences.class);

                Installer installer = new Installer(preference.getContext());

                if (!installer.installIodine()) {
                    Utils.showErrorMessage(preference.getContext(), "Could not install the client");
                    return false;
                }

                Toast t = Toast.makeText(preference.getContext(), R.string.checklist_install_ok, Toast.LENGTH_LONG);
                t.show();

                preference.getContext().startActivity(intent);
                finish();
                return false;
            }
        });

        screen.addPreference(prefProceed);
    }

    /**
     * Display a help button if some dependencies are missing.
     */
    private void addHelpButton() {
        PreferenceScreen screen = getPreferenceScreen();

        Preference prefHelp = new Preference(this);
        prefHelp.setTitle(R.string.checklist_nok);
        prefHelp.setSummary(R.string.checklist_nok_subtitle);

        prefHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(final Preference preference) {
                String url = preference.getContext().getString(R.string.url_setup_help);
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));

                try {
                    preference.getContext().startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    // do something about the exception, or not ...
                    return false;
                }

                return false;
            }
        });

        screen.addPreference(prefHelp);
    }

}
