package net.magictunnel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
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
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = v.getContext().getString(R.string.about_url);
				Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));

				try {
				 v.getContext().startActivity(intent);
				} catch (ActivityNotFoundException ex) {
				 // do something about the exception, or not ...
				}

			}
		});
		dialog.show();
	}

    private Utils() {
    	
    }
	
}
