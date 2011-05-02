package net.magictunnel;

import net.magictunnel.core.Iodine;
import net.magictunnel.settings.Settings;
import android.app.Application;

public class MagicTunnel extends Application {

	private Iodine m_iodine = new Iodine();
	
	public Settings getSettings() {
		return Settings.get(getApplicationContext());
	}
	
	public Iodine getIodine() {
		return m_iodine;
	}
}
