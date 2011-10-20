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
