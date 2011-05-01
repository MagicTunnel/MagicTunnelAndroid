package net.magictunnel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Utils {
	public static void showErrorMessage(Context c, String message) {
		AlertDialog.Builder b = new AlertDialog.Builder(c)
        .setTitle(android.R.string.dialog_alert_title)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage(message);

		b.setPositiveButton(android.R.string.ok, null);
		b.create();
		b.show();
    }

	public static void showErrorMessage(Context c, int title, int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(c)
        .setTitle(title)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage(message);

		b.setPositiveButton(android.R.string.ok, null);
		b.create();
		b.show();
    }

    private Utils() {
    	
    }
	
}
