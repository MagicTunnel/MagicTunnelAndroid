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
