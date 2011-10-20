package net.magictunnel;

import net.magictunnel.core.ConnectivityListener;
import net.magictunnel.core.Iodine;
import net.magictunnel.settings.Settings;
import android.app.Application;

/**
 * Stores application-wide data.
 * @author Vitaly
 *
 */
public class MagicTunnel extends Application {

    /** The iodine client. */
    private Iodine mIodine;

    /** Connection listener. */
    @SuppressWarnings("unused")
    private ConnectivityListener mListener;


    @Override
    public final void onCreate() {
        super.onCreate();
        mIodine = new Iodine();
        mListener = new ConnectivityListener(this, mIodine);
    }

    /**
     * Get the settings.
     * @return the settings.
     */
    public final Settings getSettings() {
        return Settings.get(getApplicationContext());
    }

    /**
     * Get the iodine instance.
     * @return the iodine instance.
     */
    public final Iodine getIodine() {
        return mIodine;
    }
}
