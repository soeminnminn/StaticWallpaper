package com.s16.staticwallpaper.activity;

import com.s16.staticwallpaper.Common;
import com.s16.staticwallpaper.R;
import com.s16.staticwallpaper.fragment.PreviewFragment;
import com.s16.staticwallpaper.fragment.SettingsFragment;
import com.s16.staticwallpaper.utils.SystemUtils;
import com.s16.widget.PagerSlidingTabStrip;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class MainActivity extends Activity 
		implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	protected static final String TAG = MainActivity.class.getSimpleName();
	
	public class PagePagerAdapter extends FragmentPagerAdapter {
		
		private final PreviewFragment[] mFragment;
		private SettingsFragment mSettingsFragment; 

        public PagePagerAdapter(FragmentManager fm) {
            super(fm);
            mFragment = new PreviewFragment[2];
        }

        @Override
        public Fragment getItem(int position) {
        	//int type = getResources().getConfiguration().orientation;
        	if (position < 2) {
        		if (mFragment[position] == null) {
        			int type = position;
                	if (SystemUtils.isTablet(getContext())) type += 2;
        			mFragment[position] = PreviewFragment.newInstance(getContext(), type);
        		}
            	return mFragment[position];
            	
        	} else if (position == 2) {
        		if (mSettingsFragment == null) {
        			mSettingsFragment = new SettingsFragment(getContext());
        		}
        		return mSettingsFragment;
        	}
        	return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	switch(position) {
	        	case 0:
	        		return getString(R.string.action_portrait);
	        	case 1:
	        		return getString(R.string.action_landscape);
	        	case 2:
	        		return getString(R.string.action_settings);
	        	default:
	        		break;
        	} 
            return "";
        }
        
        public void performSave() {
        	for(PreviewFragment fragment : mFragment) {
        		if (fragment != null) {
        			fragment.performSave();
        		}
        	}
        }
        
        public void performCacheChanged(int position) {
        	if (position == 0 || position == 1) {
	        	if (mFragment[position] != null) {
	        		mFragment[position].cacheChanged();
	        	}
        	}
        }
    }
	
	protected final Object mLock = new Object();
	protected final Object mProgressLock = new Object();
	private PagePagerAdapter mPagePagerAdapter;
	private int mSaveCount;
	private ProgressDialog mProgressDialog;
	
	protected Context getContext() {
		return this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip)findViewById(R.id.main_tabs_strip);
		ViewPager pager = (ViewPager)findViewById(R.id.main_view_pager);
		
		mPagePagerAdapter = new PagePagerAdapter(getFragmentManager());
		pager.setAdapter(mPagePagerAdapter);
		tabs.setViewPager(pager);
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				break;
			case R.id.action_accept:
				onAccept();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	public void onDestroy() {
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		System.gc();
		super.onDestroy();
	}
	
	public void showProgress(int msgResId) {
		synchronized (mProgressLock) {
			//setProgressBarIndeterminateVisibility(true);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				return;
			}
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(getString(msgResId));
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();	
		}
	}
	
	public void hideProgress() {
		synchronized (mProgressLock) {
			//setProgressBarIndeterminateVisibility(false);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	}
	
	public void onAccept() {
		mSaveCount = 0;
		mPagePagerAdapter.performSave();
		showProgress(R.string.message_progress_saving);
	}
	
	public void onSaveSuccess() {
		synchronized (mLock) {
			mSaveCount++;
			if (mSaveCount == mPagePagerAdapter.getCount() - 1) {
				hideProgress();
				finish();
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Common.PREFS_EDITOR_CACHE_CHANGED)) {
			if (mPagePagerAdapter != null) {
				String cacheKey = sharedPreferences.getString(key, null);
				if (cacheKey != null && cacheKey.length() > 0) {
					if (cacheKey.charAt(0) == 'P') {
						mPagePagerAdapter.performCacheChanged(0);
					} else {
						mPagePagerAdapter.performCacheChanged(1);
					}
				}
			}
		}
	}
}
