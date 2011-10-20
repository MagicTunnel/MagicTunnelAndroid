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
