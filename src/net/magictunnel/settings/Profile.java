package net.magictunnel.settings;

import java.security.DomainCombiner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Profile implements Comparable<Profile> {
	public static final String PROFILE_PREFIX = "profile_";
	public static final String PROFILE_TYPE = "_type";
	public static final String PROFILE_INTERFACE = "_interface";
	public static final String PROFILE_DOMAIN = "_domain";
	public static final String PROFILE_PASSWORD = "_password";
	
	
	private String m_name;
	private String m_type;
	private Interfaces m_interface;
	private String m_domainName;
	private String m_password;
	
	public String getName() {
		return m_name;
	}
	
	public void setName(String name) {
		m_name = name;
	}
	
	public Interfaces getInterface() {
		return m_interface;
	}
	
	public void setInterface(Interfaces iface) {
		m_interface = iface;
	}
	
	public String getDomainName() {
		return m_domainName;
	}
	
	public void setDomainName(String name){
		m_domainName = name;
	}
	
	public String getPassword() {
		return m_password;
	}
	
	public void setPassword(String password) {
		m_password = password;
	}

	@Override
	public int compareTo(Profile another) {
		return m_name.compareTo(another.m_name);
	}
	
	public static Profile retrieveProfile(Context ctx, String name) {
		Profile prof = new Profile();
		prof.m_name = name; 
		
		String prefixedName = PROFILE_PREFIX + "_" + name;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		if (!prefs.contains(prefixedName + PROFILE_TYPE)) {
			return null;
		}
		
		prof.m_type = prefs.getString(prefixedName + PROFILE_TYPE, "dnstunnel");
		if (!prof.m_type.equals("dnstunnel")) {
			return null;
		}
		
		
		prof.m_interface = Interfaces.valueOf(prefs.getString(prefixedName + PROFILE_INTERFACE, "CELLULAR"));
		prof.m_domainName = prefs.getString(prefixedName + PROFILE_DOMAIN, "");
		prof.m_password = prefs.getString(prefixedName + PROFILE_PASSWORD, "");
		return prof;
	}
	
	public void saveProfile(Context ctx) {
		String prefixedName = PROFILE_PREFIX + m_name;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.edit().putString(prefixedName + PROFILE_TYPE, m_type);
		prefs.edit().putString(prefixedName + PROFILE_INTERFACE, m_interface.name());
		prefs.edit().putString(prefixedName + PROFILE_DOMAIN, m_domainName);
		prefs.edit().putString(prefixedName + PROFILE_PASSWORD, m_password);
	}
	
	public void deleteProfile(Context ctx) {
		String prefixedName = PROFILE_PREFIX + m_name;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.edit().remove(prefixedName + PROFILE_TYPE);
		prefs.edit().remove(prefixedName + PROFILE_INTERFACE);
		prefs.edit().remove(prefixedName + PROFILE_DOMAIN);
		prefs.edit().remove(prefixedName + PROFILE_PASSWORD);
	}
}
