package com.s16.screenoff;

import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.s16.staticwallpaper.R;

public class ScreenOffUtils {
	
	protected static final String TAG = ScreenOffUtils.class.getSimpleName();
	
	/**
	 * Turns the screen off and locks the device, provided that proper rights are given.
	 * 
	 * @param context - The application context
	 */
	public static void turnScreenOff(Context context) {
		DevicePolicyManager policyManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName adminReceiver = new ComponentName(context, ScreenOffAdminReceiver.class);
		boolean admin = policyManager.isAdminActive(adminReceiver);
		if (admin) {
			policyManager.lockNow();
		} else {
			Toast.makeText(context, R.string.device_admin_not_enabled, Toast.LENGTH_LONG).show();
		}
	}
	
	public static boolean requestDeviceAdmin(Application application, Class<?> classActivity) {
		Context context = application.getApplicationContext();
		DevicePolicyManager policyManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName adminReceiver = new ComponentName(context, ScreenOffAdminReceiver.class);
		boolean admin = policyManager.isAdminActive(adminReceiver);
		if (!admin) {
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
		    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.device_admin_activation_message));
		    intent.addCategory(Intent.CATEGORY_LAUNCHER);
		    ComponentName componentName = new ComponentName(context, classActivity);
		    intent.setComponent(componentName);
		    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    application.startActivity(intent);
		}
		return admin;
	}
}
