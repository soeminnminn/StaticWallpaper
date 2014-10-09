package com.s16.staticwallpaper.activity;

import com.s16.screenoff.ScreenOffAdminReceiver;
import com.s16.screenoff.ScreenOffUtils;
import com.s16.staticwallpaper.R;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;

public class ScreenOffActivity extends Activity {
	
	protected static final String TAG = ScreenOffActivity.class.getSimpleName();
	protected static final int REQ_ACTIVATE_DEVICE_ADMIN = 0x300;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_off);
		
		if (requestDeviceAdmin(getApplicationContext())) {
			turnScreenOffAndExit(getApplicationContext());
		}
	}
	
	protected boolean requestDeviceAdmin(Context context) {
		DevicePolicyManager policyManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName adminReceiver = new ComponentName(context, ScreenOffAdminReceiver.class);
		boolean admin = policyManager.isAdminActive(adminReceiver);
		if (!admin) {
			Intent activateDeviceAdminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		    activateDeviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
		    activateDeviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_activation_message));
		    startActivityForResult(activateDeviceAdminIntent, REQ_ACTIVATE_DEVICE_ADMIN);
		}
		return admin;
	}
	
	private void turnScreenOffAndExit(Context context) {
		
		((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
		ScreenOffUtils.turnScreenOff(context);
		
		// schedule end of activity
		final Activity activity = this;
		Thread t = new Thread() {
			public void run() {
				try {
					sleep(500);
				} catch (InterruptedException e) {
					/* ignore this */
				}
				activity.finish();
			}
		};
		t.start();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQ_ACTIVATE_DEVICE_ADMIN) {
				turnScreenOffAndExit(getApplicationContext());
			}
		}
	}
}
