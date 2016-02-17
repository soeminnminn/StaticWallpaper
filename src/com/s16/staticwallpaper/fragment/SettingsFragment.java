package com.s16.staticwallpaper.fragment;

import com.s16.staticwallpaper.Common;
import com.s16.staticwallpaper.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingsFragment extends PreferenceFragment {
	
	protected static final String TAG = SettingsFragment.class.getSimpleName();
	private Context mContext;
	
	public SettingsFragment() {
	}
	
	public SettingsFragment(Context context) {
		mContext = context;	
	}
	
	protected Context getContext() {
		if (mContext == null) {
			return getActivity();
		}
		return mContext;
	}
	
	@Override
	 public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		Preference prefsVersion = findPreference(Common.PREFS_ABOUT);
		PackageInfo pInfo;
		try {
			pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
			prefsVersion.setSummary(getString(R.string.prefs_version, pInfo.versionName));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		prefsVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showAboutDialog(getContext());
				return false;
			}
        });
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		boolean enabledActionDown = preferences.getBoolean(Common.PREFS_ACTION_DOWN, false);
		boolean enabledActionPinchIn = preferences.getBoolean(Common.PREFS_ACTION_DOWN, false);
		
		CheckBoxPreference prefsActionDown = (CheckBoxPreference)findPreference(Common.PREFS_ACTION_DOWN);
		prefsActionDown.setSummary(enabledActionDown ? R.string.prefs_action_down_summary : R.string.prefs_action_disabled_summary);
		prefsActionDown.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue.equals(true)) {
					preference.setSummary(R.string.prefs_action_down_summary);
				} else {
					preference.setSummary(R.string.prefs_action_disabled_summary);
				}
				return true;
			}
			
		});
		
		CheckBoxPreference prefsActionPinchIn = (CheckBoxPreference)findPreference(Common.PREFS_ACTION_PINCH_IN);
		prefsActionPinchIn.setSummary(enabledActionPinchIn ? R.string.prefs_action_pinch_in_summary : R.string.prefs_action_disabled_summary);
		prefsActionPinchIn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue.equals(true)) {
					preference.setSummary(R.string.prefs_action_pinch_in_summary);
				} else {
					preference.setSummary(R.string.prefs_action_disabled_summary);
				}
				return true;
			}
			
		});
	 }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		if (mContext == null) {
			mContext = inflater.getContext();
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	protected void showAboutDialog(Context context) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(getText(R.string.prefs_group_about));
		
		String html = context.getText(R.string.about_text).toString();
		final TextView message = new TextView(context);
		message.setPadding((int)Common.convertDpToPixel(10f, context), (int)Common.convertDpToPixel(10f, context), 
				(int)Common.convertDpToPixel(10f, context), (int)Common.convertDpToPixel(10f, context));
		message.setTextColor(context.getResources().getColor(android.R.color.black));
		message.setMovementMethod(LinkMovementMethod.getInstance());
		message.setText(Html.fromHtml(html));
		dialogBuilder.setView(message);
		
		dialogBuilder.setNegativeButton(getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialogBuilder.show();
	}
}
