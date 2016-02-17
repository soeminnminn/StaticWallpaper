package com.s16.screenoff;

import com.s16.staticwallpaper.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Listener class which detects phone state changes and locks the device when a
 * call is initiated or answered.
 */
public class ScreenOffPhoneListener extends PhoneStateListener {
	private Context mContext;
	private boolean mIncomingCall;

	public ScreenOffPhoneListener(Context context) {
		this.mContext = context;
	}
	
	private boolean isEnabled() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		return prefs.getBoolean(Common.PREFS_SCREENOFF_PHONE_STATE, true);
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		if (!isEnabled()) return;
		
		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
			mIncomingCall = true;
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			turnScreenOff(mIncomingCall ? 400 : 1200);
			mIncomingCall = false;
			break;
		default:
			mIncomingCall = false;
			break;
		}
	}

	private void turnScreenOff(final long delay) {
		Thread t = new Thread() {
			public void run() {
				try {
					sleep(delay);
				} catch (InterruptedException e) {
					/* ignore this */
				}
				ScreenOffUtils.turnScreenOff(mContext);
			}
		};
		t.start();
	}
}
