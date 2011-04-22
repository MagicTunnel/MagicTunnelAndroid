package net.magictunnel;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class SystemComponentChecklist extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.checklist);
		populateScreen();
	}
	
	private void populateScreen() {
		PreferenceScreen screen = getPreferenceScreen();
		
		PreferenceCategory catChecklist = new PreferenceCategory(this);
		catChecklist.setTitle(R.string.checklist_category);
		
		CheckBoxPreference prefRootAccess = createCustomCheckBox(R.string.checklist_root);
		CheckBoxPreference prefTun = createCustomCheckBox(R.string.checklist_tun);
		
		PreferenceCategory catAction = new PreferenceCategory(this);
		catAction.setTitle(R.string.checklist_category_action);
		
		screen.addPreference(catChecklist);
		screen.addPreference(prefRootAccess);
		screen.addPreference(prefTun);
		screen.addPreference(catAction);
		
		addProceedButton();
		addHelpButton();
	}
	
	CheckBoxPreference createCustomCheckBox(int id) {
		CheckBoxPreference checkbox = new CheckBoxPreference(this);
		checkbox.setTitle(id);
		checkbox.setWidgetLayoutResource(R.layout.checkbox);
		checkbox.setEnabled(false);
		return checkbox;
	}
	
	/**
	 * Displays install button if all dependencies are satisfied
	 */
	private void addProceedButton() {
		PreferenceScreen screen = getPreferenceScreen();
		
		Preference prefProceed = new Preference(this);
		prefProceed.setTitle(R.string.checklist_proceed);
		prefProceed.setSummary(R.string.checklist_proceed_subtitle);
		
		screen.addPreference(prefProceed);		
	}
	
	/**
	 * Display a help button if some dependencies are missing
	 */
	private void addHelpButton() {
		PreferenceScreen screen = getPreferenceScreen();
		
		Preference prefHelp = new Preference(this);
		prefHelp.setTitle(R.string.checklist_nok);
		prefHelp.setSummary(R.string.checklist_nok_subtitle);
		
		screen.addPreference(prefHelp);
	}

}
