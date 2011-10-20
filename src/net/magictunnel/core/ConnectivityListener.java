package net.magictunnel.core;

import net.magictunnel.settings.Profile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Monitors connectivity changes, kills the dns tunnel
 * and resets routes accordingly.
 * @author Vitaly
 *
 */
public class ConnectivityListener {

    /** The iodine client. */
    private Iodine mIodine;

    /**
     * Create a new connectivity listener.
     * @param ctx The Android context.
     * @param iodine The iodine client.
     */
    public ConnectivityListener(final Context ctx, final Iodine iodine) {
        mIodine = iodine;
        setupPhoneStateListener(ctx);
        setupWifiStateListener(ctx);
    }

    /**
     * XXX: Move to a separate class???
     * @param ctx The Android context.
     */
    private void setupPhoneStateListener(final Context ctx) {
        TelephonyManager telephonyManager =
            (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        // Create a new PhoneStateListener
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onDataConnectionStateChanged(final int state) {
                Profile p = mIodine.getActiveProfile();
                if (p == null) {
                    mIodine.resetSavedRoutes();
                    return;
                }
                mIodine.resetSavedRoutes();
            }
        };

        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    /**
     * Setup wifi state listener.
     * @param ctx The Android context.
     */
    private void setupWifiStateListener(final Context ctx) {

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);

                if (state != WifiManager.WIFI_STATE_DISABLING
                        && state != WifiManager.WIFI_STATE_DISABLED) {
                    return;
                }

                Profile p = mIodine.getActiveProfile();

                if (p == null) {
                    mIodine.resetSavedRoutes();
                    return;
                }
                mIodine.resetSavedRoutes();
                mIodine.disconnect();
            }
        };

        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentfilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        ctx.getApplicationContext().
        registerReceiver(receiver, intentfilter);
    }

}
