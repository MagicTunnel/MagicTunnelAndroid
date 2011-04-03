package net.magictunnel;

import net.magictunnel.settings.Settings;
import android.app.Application;

public class MagicTunnel extends Application {

	public Settings getSettings() {
		return Settings.get(getApplicationContext());
	}
}
