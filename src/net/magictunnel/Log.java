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

import net.magictunnel.core.Iodine;
import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Displays the log to the user for debugging purposes.
 * @author Vitaly
 *
 */
public class Log extends Activity {

    /** Menu id for copying the log to the clipboard. */
    private static final int MENU_COPYLOG = Menu.FIRST;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState saved state.
     */
    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.log);

        MagicTunnel mt = ((MagicTunnel) getApplication());
        Iodine iod = mt.getIodine();

        TextView textview = (TextView) findViewById(R.id.log_view);
        textview.setMovementMethod(new ScrollingMovementMethod());
        String s = iod.getLog().toString();
        if (s.length() == 0) {
            textview.setText(R.string.log_empty);
        } else {
            textview.setText(iod.getLog().toString());
        }
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_COPYLOG, 0, R.string.log_copy).setIcon(
                android.R.drawable.ic_menu_share);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
        case MENU_COPYLOG:
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            MagicTunnel mt = ((MagicTunnel) getApplication());
            Iodine iod = mt.getIodine();
            clipboard.setText(iod.getLog().toString());
            break;

        default:
            return false;
        }

        return true;
    }
}
