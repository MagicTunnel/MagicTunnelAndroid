package net.magictunnel;

import net.magictunnel.core.Iodine;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;


public class Log extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.log);
        
        MagicTunnel mt = ((MagicTunnel) getApplication());
		Iodine iod = mt.getIodine();
		
		TextView textview = (TextView)findViewById(R.id.log_view);
		textview.setMovementMethod(new ScrollingMovementMethod());
		String s = iod.getLog().toString();
		if (s.length() == 0) {
			textview.setText(R.string.log_empty);
		}else {
			textview.setText(iod.getLog().toString());
		}
    }
}
