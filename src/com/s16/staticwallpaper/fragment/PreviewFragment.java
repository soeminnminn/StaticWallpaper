package com.s16.staticwallpaper.fragment;

import com.s16.staticwallpaper.Common;
import com.s16.staticwallpaper.R;
import com.s16.staticwallpaper.activity.EditorActivity;
import com.s16.staticwallpaper.activity.MainActivity;
import com.s16.staticwallpaper.drawing.ColorBoxDrawable;
import com.s16.staticwallpaper.drawing.PreviewDrawable;
import com.s16.staticwallpaper.drawing.WallpaperDrawable;
import com.s16.staticwallpaper.utils.CacheManager;
import com.s16.widget.colorpicker.ColorPickerDialog;
import com.s16.widget.popupmenu.PopupMenuCompat;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class PreviewFragment extends Fragment {

	protected static final String TAG = PreviewFragment.class.getSimpleName();
	
	static final int SCR_PHONE_PORT = 0;
	static final int SCR_PHONE_LAND = 1;
	static final int SCR_TAB_PORT = 2;
	static final int SCR_TAB_LAND = 3;
	
	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private long mUiThreadId;
	
	private ImageView mPreviewImageView;
	private PreviewDrawable mPreviewDrawable;
	private int mPicturePosition = WallpaperDrawable.POSITION_FILL;
	private String mCacheName;
	private String mEditorCacheName;
	private int mArgType = -1;
	private boolean mIsImageRemoved;
	private Button mActionDrawMode;
	private int mPicBkgColor = Common.DEFAULT_BKG_COLOR;
	private ColorBoxDrawable mColorBoxDrawable;
	
	private View.OnClickListener mButtonPickClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			actionCropImage(v);
		}
		
	};
	
	private View.OnClickListener mButtonRemoveImageClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			actionRemoveImage(v);
		}
		
	};
	
	private View.OnClickListener mButtonBkgColorPickClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			actionBkgColorPick(v);
		}
		
	};
	
	private View.OnClickListener mButtonDrawModeClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			actionDrawMode(v);
		}
		
	};
	
	public static PreviewFragment newInstance(Context context, int type) {
		PreviewFragment fragment = new PreviewFragment(context);
        Bundle args = new Bundle();
        args.putInt(Common.ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
	}
	
	private Context mContext;
	
	public PreviewFragment() {
	}
	
	public PreviewFragment(Context context) {
		mContext = context;
	}
	
	protected Context getContext() {
		if (mContext == null) {
			return getActivity();
		}
		return mContext;
	}
	
	protected MainActivity getMainActivity() {
		if (getActivity() instanceof MainActivity) {
			return (MainActivity)getActivity();
		}
		return null;
	}
	
	protected final void runOnUiThread(Runnable action) {
        if (Thread.currentThread().getId() != mUiThreadId) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }
	
	protected int getArgType() {
		if (mArgType < 0) {
			Bundle args = getArguments(); 
			if (args != null) {
				mArgType = args.getInt(Common.ARG_TYPE);
			}
		}
		return mArgType;
	}
	
	protected boolean isPortrait() {
		return (mArgType == SCR_PHONE_PORT || mArgType == SCR_TAB_PORT);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments(); 
		if (args != null) {
			mArgType = args.getInt(Common.ARG_TYPE);
		}
		
		if (savedInstanceState != null) {
			mArgType = savedInstanceState.getInt(Common.ARG_TYPE);
			mPicturePosition = savedInstanceState.getInt(Common.ARG_POSITION);
			mCacheName = savedInstanceState.getString(Common.ARG_CACHENAME);
			mEditorCacheName = savedInstanceState.getString(Common.ARG_EDITOR_CACHENAME);
			mIsImageRemoved = savedInstanceState.getBoolean(Common.ARG_NO_IMAGE);
			mPicBkgColor = savedInstanceState.getInt(Common.ARG_BKG_COLOR);
		} else {
			loadPreferences();
		}
    }
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		if (mPreviewImageView != null) {
			mPreviewImageView.setImageBitmap(null);
		}
		if (mPreviewDrawable != null) {
			mPreviewDrawable.recycle();
		}
		super.onDestroy();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		if (mContext == null) mContext = inflater.getContext();
		mUiThreadId = Thread.currentThread().getId();
		
		View rootView = inflater.inflate(R.layout.fragment_preview, container, false);
		setDefaultImage();
		
		mPreviewImageView = (ImageView)rootView.findViewById(R.id.image_preview);
		mPreviewImageView.setImageDrawable(mPreviewDrawable);
		
		Button actionPick = (Button)rootView.findViewById(R.id.actionPick);
		actionPick.setOnClickListener(mButtonPickClick);
		actionPick.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_new_picture), null, null, null);
		
		Button actionRemoveImage = (Button)rootView.findViewById(R.id.actionRemoveImage);
		actionRemoveImage.setOnClickListener(mButtonRemoveImageClick);
		actionRemoveImage.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_discard), null, null, null);
		
		mColorBoxDrawable = new ColorBoxDrawable(getContext(), mPicBkgColor);
		Button actionBkgColorPick = (Button)rootView.findViewById(R.id.actionPickColor);
		actionBkgColorPick.setOnClickListener(mButtonBkgColorPickClick);
		actionBkgColorPick.setCompoundDrawablesWithIntrinsicBounds(mColorBoxDrawable, null, null, null);
		
		mActionDrawMode = (Button)rootView.findViewById(R.id.actionDrawMode);
		mActionDrawMode.setOnClickListener(mButtonDrawModeClick);
		mActionDrawMode.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_size_both), null, null, null);
		updateDrawModeString();
		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (!mIsImageRemoved) {
			loadBitmap(mCacheName);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (outState == null) {
			outState = new Bundle();
		}
		outState.putInt(Common.ARG_TYPE, mArgType);
		outState.putInt(Common.ARG_POSITION, mPicturePosition);
		outState.putString(Common.ARG_CACHENAME, mCacheName);
		outState.putString(Common.ARG_EDITOR_CACHENAME, mEditorCacheName);
		outState.putBoolean(Common.ARG_NO_IMAGE, mIsImageRemoved);
		outState.putInt(Common.ARG_BKG_COLOR, mPicBkgColor);
		
		super.onSaveInstanceState(outState);
    }
	
	private void updateDrawModeString() {
		if (mActionDrawMode == null) return;
		String[] arr = getContext().getResources().getStringArray(R.array.draw_modes);
		if (arr != null && arr.length > 0 && arr.length > mPicturePosition) {
			mActionDrawMode.setText(getString(R.string.action_draw_mode) + " [ " + arr[mPicturePosition] + " ]");
		}
	}
	
	private void loadPreferences() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		if (isPortrait()) {
			mIsImageRemoved = sharedPreferences.getBoolean(Common.PREFS_PORT_NOIMAGE, true);
			mPicBkgColor = sharedPreferences.getInt(Common.PREFS_PORT_BACK_COLOR, Common.DEFAULT_BKG_COLOR);
			mPicturePosition = sharedPreferences.getInt(Common.PREFS_PORT_POSITION, WallpaperDrawable.POSITION_FILL);
			mCacheName = sharedPreferences.getString(Common.PREFS_PORT_CACHENAME, null);
		} else {
			mIsImageRemoved = sharedPreferences.getBoolean(Common.PREFS_LAND_NOIMAGE, true);
			mPicBkgColor = sharedPreferences.getInt(Common.PREFS_LAND_BACK_COLOR, Common.DEFAULT_BKG_COLOR);
			mPicturePosition = sharedPreferences.getInt(Common.PREFS_LAND_POSITION, WallpaperDrawable.POSITION_FILL);
			mCacheName = sharedPreferences.getString(Common.PREFS_LAND_CACHENAME, null);
		}
	}
	
	private void loadBitmap(String cacheName) {
		
		new AsyncTask<String, Void, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... params) {
				return CacheManager.popDiskCache(getContext(), params[0]);
			}
			
			@Override
			protected void onPreExecute() {
				//getActivity().setProgressBarIndeterminateVisibility(true);
				getMainActivity().showProgress(R.string.message_progress_loading);
		    }
			
			@Override
			protected void onPostExecute(Bitmap result) {
				if (result != null) {
					setImageBitmap(result);
				}
				//getActivity().setProgressBarIndeterminateVisibility(false);
				getMainActivity().hideProgress();
		    }
		}.execute(cacheName);
	}
	
	protected void setDefaultImage() {
		/*
		if (isPortrait()) {
			mPreviewDrawable = new PreviewDrawable(getContext(), R.drawable.portrait, 1081, 1600, getArgType(), mPicturePosition, mPicBkgColor);
		} else {
			mPreviewDrawable = new PreviewDrawable(getContext(), R.drawable.landscape, 1024, 768, getArgType(), mPicturePosition, mPicBkgColor);
		}
		*/
		mPreviewDrawable = new PreviewDrawable(getContext(), getArgType(), mPicBkgColor);
	}
	
	public void setImageBitmap(final Bitmap bitmap) {
		if (bitmap != null) {
			mIsImageRemoved = false;
			if (mPreviewDrawable == null) {
				mPreviewDrawable = new PreviewDrawable(getContext(), bitmap, getArgType(), mPicturePosition, mPicBkgColor);
				mPreviewDrawable.setBackgroundColor(mPicBkgColor);
			} else {
				mPreviewDrawable.setBitmap(bitmap);
			}
		}
	}
	
	public void setPicturePosition(int value) {
		if (mPicturePosition != value) {
			mPicturePosition = value;
			if (mPreviewDrawable != null) {
				mPreviewDrawable.setPicturePosition(value);
			}
			updateDrawModeString();
		}
	}
	
	public void setPictureBackgroundColor(int color) {
		mPicBkgColor = color;
		if (mPreviewDrawable != null) {
			mPreviewDrawable.setBackgroundColor(color);
		}
		if (mColorBoxDrawable != null) {
			mColorBoxDrawable.setColor(mPicBkgColor);
		}
	}
	
	public void cacheChanged() {
		mEditorCacheName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Common.PREFS_EDITOR_CACHENAME, "");
		if (!TextUtils.isEmpty(mEditorCacheName)) {
			loadBitmap(mEditorCacheName);
		}
	}
	
	private void actionDrawMode(View view) {
		PopupMenuCompat popupMenu = new PopupMenuCompat(getContext(), view);
		popupMenu.inflate(R.menu.menu_draw_modes);
		popupMenu.setOnMenuItemClickListener(new PopupMenuCompat.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId()) {
					case R.id.action_draw_modes_fill:
						setPicturePosition(WallpaperDrawable.POSITION_FILL);
						break;
					case R.id.action_draw_modes_fit:
						setPicturePosition(WallpaperDrawable.POSITION_FIT);
						break;
					case R.id.action_draw_modes_stretch:
						setPicturePosition(WallpaperDrawable.POSITION_STRETCH);
						break;
					case R.id.action_draw_modes_tile:
						setPicturePosition(WallpaperDrawable.POSITION_TILE);
						break;
					case R.id.action_draw_modes_center:
						setPicturePosition(WallpaperDrawable.POSITION_CENTER);
						break;
					default:
						break;
				}
				return false;
			}
		});
        popupMenu.show();
	}
	
	private void actionRemoveImage(View view) {
		mPreviewDrawable.clearBitmap();
		mIsImageRemoved = true;
		if (!TextUtils.isEmpty(mCacheName)) {
			CacheManager.putDiskCache(getContext(), mCacheName, null);
		}
		if (!TextUtils.isEmpty(mEditorCacheName)) {
			CacheManager.putDiskCache(getContext(), mEditorCacheName, null);
		}
	}
	
	private void actionBkgColorPick(View view) {
		if (mPreviewDrawable == null) return;
		final ColorPickerDialog colorDialog = new ColorPickerDialog(getContext(), mPicBkgColor);
		colorDialog.setTitle(getString(R.string.title_color_picker));
		// colorDialog.setAlphaSliderVisible(true);
		colorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setPictureBackgroundColor(colorDialog.getColor());
				dialog.dismiss();
			}
		});
		
		colorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		colorDialog.show();
	}
	
	private void actionCropImage(View view) {
		Intent intent = new Intent(getContext(), EditorActivity.class);
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.putExtra(Common.ARG_NO_IMAGE, mIsImageRemoved);
		if (isPortrait()) {
			intent.putExtra(Common.ARG_EDITOR_CACHENAME, CacheManager.CACHE_PORTRAIT);
			intent.putExtra(Common.ARG_ORIENTATION, Common.ORIENTATION_PORT);
		} else {
			intent.putExtra(Common.ARG_EDITOR_CACHENAME, CacheManager.CACHE_LANDSCAPE);
			intent.putExtra(Common.ARG_ORIENTATION, Common.ORIENTATION_LAND);
		}
		startActivity(intent);
	}
	
	protected void saveCache(final String cacheName, final Bitmap bitmap) {
		
		final MainActivity activity = getMainActivity();
		new AsyncTask<Bitmap, Void, String>() {

			@Override
			protected String doInBackground(Bitmap... params) {
				CacheManager.putDiskCache(getContext(), cacheName, bitmap);
				bitmap.recycle();
				return "";
			}
			
			@Override
			protected void onPostExecute(String result) {
				savePreferences(cacheName);
				if (activity != null) {
					activity.onSaveSuccess();
				}
		    }
			
		}.execute(bitmap);
	}
	
	protected void savePreferences(String cacheName) {
		SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPreference.edit();
		
		if (isPortrait()) {
			editor.putBoolean(Common.PREFS_PORT_NOIMAGE, mIsImageRemoved);
			editor.putInt(Common.PREFS_PORT_BACK_COLOR, mPicBkgColor);
			editor.putInt(Common.PREFS_PORT_POSITION, mPicturePosition);
			editor.putString(Common.PREFS_PORT_CACHENAME, cacheName);
			editor.putString(Common.PREFS_CACHE_CHANGED, CacheManager.getPortraitCacheName());
		} else {
			editor.putBoolean(Common.PREFS_LAND_NOIMAGE, mIsImageRemoved);
			editor.putInt(Common.PREFS_LAND_BACK_COLOR, mPicBkgColor);
			editor.putInt(Common.PREFS_LAND_POSITION, mPicturePosition);
			editor.putString(Common.PREFS_LAND_CACHENAME, cacheName);
			editor.putString(Common.PREFS_CACHE_CHANGED, CacheManager.getLandscapeCacheName());
		}
		editor.commit();
	}
	
	public void performSave() {
		String cacheNameActual = null;
		if (isPortrait()) {
			cacheNameActual = CacheManager.getPortraitActualCacheName();
		} else {
			cacheNameActual = CacheManager.getLandscapeActualCacheName();
		}
		
		if (!TextUtils.isEmpty(mCacheName)) {
			Log.i(TAG, "CacheName=" + mCacheName);
			CacheManager.putDiskCache(getContext(), mCacheName, null);
		}
		
		if (mIsImageRemoved) {
			savePreferences(cacheNameActual);
			getMainActivity().onSaveSuccess();
		} else {
			final Bitmap bitmap = mPreviewDrawable.getOriginalBitmap();
			if (bitmap != null) {
				saveCache(cacheNameActual, bitmap);
			} else {
				mIsImageRemoved = true;
				savePreferences(cacheNameActual);
				getMainActivity().onSaveSuccess();
			}	
		}
	}
}

