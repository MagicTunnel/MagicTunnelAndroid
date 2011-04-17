package net.magictunnel.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class Settings {
	private static final String PROFILES = "profiles";
	
	private static Settings s_settings = null;
	private HashMap<String, Profile> m_profiles = new HashMap<String,Profile>();
	private String m_currentSettingsProfile;
	
	public String getCurrentSettingsProfile() {
		return m_currentSettingsProfile;
	}
	
	public void setCurrentSettingsProfile(String profile) {
		m_currentSettingsProfile = profile;
	}
	
	private Settings() {
		
	}
	
	public void retrieveProfile(Context context, String name) {
		Profile.retrieveProfile(context, name);
	}
	
	private static Settings retrieveSettings(Context context) {
		Settings s = new Settings();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Map<String, ?> settings = prefs.getAll();
		Set<String> keys = settings.keySet();
		Set<String> profiles = new HashSet<String>();
		
		//Get all profile names from the list of settings
		for (String k: keys) {
			String [] components = k.split("_");
			if (components.length > 1) {
				profiles.add(components[1]);
			}
		}
		
		for (String k:profiles) {
			Profile p = Profile.retrieveProfile(context, k);
			if (p != null) {
				s.m_profiles.put(k,p);
			}
		}
		return s;
	}
	
	public static Settings get(Context context) {
		if (s_settings != null) {
			return s_settings;
		}
		
		s_settings = retrieveSettings(context);
		return s_settings;		
	}
	
	public List<String> getProfileNames() {
		ArrayList<String> ret = new ArrayList<String>(m_profiles.keySet());
		return ret;
	}
	
	public Profile getProfile(String name) {
		return m_profiles.get(name);
	}
	
	public boolean rename(Context ctx, String oldName, String newName) {
		Profile profile = m_profiles.get(oldName);
		if (profile == null) {
			return false;
		}
		profile.deleteProfile(ctx);
		m_profiles.remove(oldName);
		m_profiles.put(newName, profile);
		profile.setName(newName);
		return true;
	}
	
	public void addProfile(Profile profile) {
		if (m_profiles.containsKey(profile)) {
			throw new RuntimeException("Profile already exists");
		}
		m_profiles.put(profile.getName(), profile);
	}
	
	public void deleteProfile(Profile profile, Context ctx) {
		profile.deleteProfile(ctx);
		m_profiles.remove(profile.getName());
	}
}

