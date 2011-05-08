package net.magictunnel;

import net.magictunnel.core.ConnectivityListener;
import net.magictunnel.core.Iodine;
import net.magictunnel.settings.Settings;
import android.app.Application;

public class MagicTunnel extends Application {

	private Iodine m_iodine;
	private ConnectivityListener m_listener;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		m_iodine = new Iodine();
		m_listener = new ConnectivityListener(this, m_iodine);
	}
	
	public Settings getSettings() {
		return Settings.get(getApplicationContext());
	}
	
	public Iodine getIodine() {
		return m_iodine;
	}
}
