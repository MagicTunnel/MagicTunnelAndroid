package net.magictunnel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

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
	
	public static void showAboutBox(Context c) {
		Dialog dialog = new Dialog(c);
		dialog.setContentView(R.layout.aboutbox);
		TextView tv = (TextView)dialog.findViewById(R.id.about_url);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(Html.fromHtml(c.getString(R.string.about_url)));
		dialog.show();
	}

    private Utils() {
    	
    }
	
}
