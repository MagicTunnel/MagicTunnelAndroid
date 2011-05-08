package net.magictunnel.core;

import net.magictunnel.settings.Interfaces;
import net.magictunnel.settings.Profile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class ConnectivityListener {

	private TelephonyManager m_telephonyManager;
	private WifiManager m_wifiManager;
	private PhoneStateListener m_phoneStatelistener;
	private Iodine m_iodine;
	
	public ConnectivityListener(Context ctx, Iodine iodine) {
		m_iodine = iodine;
		setupPhoneStateListener(ctx);
		setupWifiStateListener(ctx);
	}
	
	/**
	 * XXX: Move to a separate class???
	 * @param ctx
	 */
	private void setupPhoneStateListener(Context ctx) {
		m_telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		// Create a new PhoneStateListener
		m_phoneStatelistener = new PhoneStateListener() {
	      @Override
	      public void onDataConnectionStateChanged(int state) {
	        Profile p = m_iodine.getActiveProfile();
	    	  if (p == null) {
	        	m_iodine.resetSavedRoutes();
	        	return;
	        }
	    	if (state == TelephonyManager.DATA_DISCONNECTED) {
	        	if (p.getInterface().equals(Interfaces.CELLULAR)) {
	        		m_iodine.resetSavedRoutes();
	        		m_iodine.disconnect();
	        	}
	        }
	      }
	    };
	    m_telephonyManager.listen(m_phoneStatelistener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
	}
	
	private void setupWifiStateListener(Context ctx) {
		m_wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		
		BroadcastReceiver receiver = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN); 
		        if (state != WifiManager.WIFI_STATE_DISABLING && state != WifiManager.WIFI_STATE_DISABLED) {
		        	return;
		        }
		        
		        Profile p = m_iodine.getActiveProfile();
		        
				if (p == null) {
		        	m_iodine.resetSavedRoutes();
		        	return;
		        }
		        if (p.getInterface().equals(Interfaces.WIFI)) {
		        	m_iodine.resetSavedRoutes();
		        	m_iodine.disconnect();
	        	}				
			}
		};
		
		IntentFilter intentfilter = new IntentFilter();
		intentfilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentfilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

		ctx.getApplicationContext().
		registerReceiver(receiver, intentfilter);
	}
	
}
