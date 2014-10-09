package com.s16.staticwallpaper.activity;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.s16.staticwallpaper.Common;
import com.s16.staticwallpaper.R;
import com.s16.staticwallpaper.utils.CacheManager;
import com.s16.staticwallpaper.utils.SystemUtils;
import com.s16.widget.popupmenu.PopupMenuCompat;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.s16.widget.bars.ToolbarView;
import com.s16.widget.cropper.CropImageView;

public class EditorActivity extends Activity {

	protected static final String TAG = EditorActivity.class.getSimpleName();
	
	protected final Object mLock = new Object();
	private boolean mIsWorking;
	private CropImageView mCropImageView;
	private ToolbarView mToolbar; 
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_editor);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mCropImageView = (CropImageView)findViewById(R.id.cropImageView);
		mToolbar = (ToolbarView)findViewById(R.id.layoutBottom);
		mToolbar.inflate(R.menu.menu_image_tools);
		mToolbar.setOverFlowIconColor(ToolbarView.ICON_COLOR_DARK);
		
		if (!getIsImageRemoved()) {
			String cacheName = getCacheName();
			if (!TextUtils.isEmpty(cacheName)) {
				loadImage(cacheName);
				setAspectRatio();
			}
		}
	}
	
	protected boolean getIsImageRemoved() {
		return getIntent().getBooleanExtra(Common.ARG_NO_IMAGE, false); 
	}
	
	protected int getOrientation() {
		String cacheName = getIntent().getStringExtra(Common.ARG_ORIENTATION);
		if (cacheName != null && cacheName.equals(Common.ORIENTATION_LAND)) {
			return Configuration.ORIENTATION_LANDSCAPE;
		}
		return Configuration.ORIENTATION_PORTRAIT;
	}
	
	protected String getCacheName() {
		String cacheName = getIntent().getStringExtra(Common.ARG_EDITOR_CACHENAME);
		if (!TextUtils.isEmpty(cacheName)) {
			return cacheName;
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				break;
			case R.id.action_pick:
				actionPickImage();
				break;
			case R.id.action_crop:
				actionCrop();
				break;
			case R.id.action_rotate_left:
				actionRotateLeft();
				break;
			case R.id.action_rotate_right:
				actionRotateRight();
				break;
			case R.id.action_aspect_ratio:
				actionAspectRatio();
				break;
			case R.id.action_save:
				actionSave();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		if (mIsWorking) return;
		synchronized (mLock) {
			super.onBackPressed();
		}
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		if (mCropImageView != null) {
			mCropImageView.clear();
		}
		System.gc();
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Common.REQUEST_PICK_IMAGE) {
				Bitmap image = null;
				ParcelFileDescriptor parcelFileDescriptor = null;
				
				if (data != null && data.getData() != null) {
					Uri selectedImageUri = data.getData();
					
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
						String[] filePathColumn = { MediaStore.Images.Media.DATA };
				         
				        Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
				        cursor.moveToFirst();
				 
				        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				        String picturePath = cursor.getString(columnIndex);
				        cursor.close();
				        
				        File imageFile = new File(picturePath);
				        try {
							parcelFileDescriptor = ParcelFileDescriptor.open(imageFile, ParcelFileDescriptor.MODE_READ_ONLY);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
		            } else {
		                try {
		                    parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
		                } catch (FileNotFoundException e) {
		                    e.printStackTrace();
		                }
		            }
				}
				
				if (parcelFileDescriptor != null) {
					try {
	                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
	                    image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
	                    parcelFileDescriptor.close();
	                } catch (FileNotFoundException e) {
	                    e.printStackTrace();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
					
					if (image != null) {
						if (mCropImageView != null ) {
							mCropImageView.setImageBitmap(image);
							setAspectRatio();
						}
					}
				}
			}
		}
	}
	
	protected void loadImage(String cacheName) {
		if (mIsWorking) return;
		final Context context = this;
		if (!CacheManager.hasDiskCache(context, cacheName)) return;
		synchronized (mLock) {
			new AsyncTask<String, Void, Bitmap>() {
	
				@Override
				protected Bitmap doInBackground(String... params) {
					return CacheManager.popDiskCache(context, params[0]);
				}
				
				@Override
				protected void onPreExecute() {
					showProgress(R.string.message_progress_loading);
					mIsWorking = true;
			    }
				
				@Override
				protected void onPostExecute(final Bitmap image) {
					if (image != null && mCropImageView != null) {
						mCropImageView.setImageBitmap(image);
						setAspectRatio();
					}
					hideProgress();
					mIsWorking = false;
			    }
				
			}.execute(cacheName);
		}
	}
	
	protected void setAspectRatio() {
		if (mCropImageView == null) return;
		//Point scrSize = new Point();
		//getWindowManager().getDefaultDisplay().getSize(scrSize);
		Point scrSize = SystemUtils.getScreenSize(this);
		
		if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
			mCropImageView.setAspectRatio(Math.max(scrSize.x, scrSize.y), Math.min(scrSize.x, scrSize.y));
		} else {
			mCropImageView.setAspectRatio(Math.min(scrSize.x, scrSize.y), Math.max(scrSize.x, scrSize.y));
		}
	}
	
	protected void showProgress(int msgResId) {
		//setProgressBarIndeterminateVisibility(true);
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(msgResId));
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}
	
	protected void hideProgress() {
		//setProgressBarIndeterminateVisibility(false);
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
	
	protected void actionPickImage() {
		if (mIsWorking) return;
		if (mCropImageView == null) return;
		// Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
		// startActivityForResult(intent, PICK_IMAGE);
		synchronized (mLock) {
			Intent intent = new Intent();
			/*
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
			} else {
				intent.setAction(Intent.ACTION_GET_CONTENT);
			}*/
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			//Intent chooserIntent = Intent.createChooser(intent, getString(R.string.title_chooser));
			//startActivityForResult(chooserIntent, Common.REQUEST_PICK_IMAGE);
			startActivityForResult(intent, Common.REQUEST_PICK_IMAGE);
		}
	}
	
	protected void actionCrop() {
		if (mIsWorking) return;
		if (mCropImageView == null) return;
		synchronized (mLock) {
			final Bitmap bitmap = mCropImageView.getCroppedImage();
			if (bitmap != null) {
				mCropImageView.setImageBitmap(bitmap);
			}
		}
	}
	
	protected void actionRotate(View view) {
		if (mIsWorking) return;
		if (mCropImageView == null) return;
		synchronized (mLock) {
			final PopupMenuCompat.OnMenuItemClickListener onMenuItemClickListener =
					new PopupMenuCompat.OnMenuItemClickListener() {
	
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					return onOptionsItemSelected(item);
				}
			};
			
			PopupMenuCompat popupMenu = new PopupMenuCompat(this, view);
			popupMenu.inflate(R.menu.menu_rotate);
			popupMenu.setOnMenuItemClickListener(onMenuItemClickListener);
	        popupMenu.show();
		}
	}

	protected void actionRotateLeft() {
		if (mIsWorking) return;
		if (mCropImageView == null) return;
		synchronized (mLock) {
			mCropImageView.rotateImage(270);
			setAspectRatio();
		}
	}
	
	protected void actionRotateRight() {
		if (mIsWorking) return;
		if (mCropImageView == null) return;
		synchronized (mLock) {
			mCropImageView.rotateImage(90);
			setAspectRatio();
		}
	}
	
	protected void actionAspectRatio() {
		if (mCropImageView == null) return;
		
		if (mCropImageView.getFixedAspectRatio()) {
			mCropImageView.setFixedAspectRatio(false);
			mToolbar.getItemById(R.id.action_aspect_ratio).setIcon(R.drawable.ic_action_aspect_ratio_unlock);
		} else {
			setAspectRatio();
			mCropImageView.setFixedAspectRatio(true);
			mToolbar.getItemById(R.id.action_aspect_ratio).setIcon(R.drawable.ic_action_aspect_ratio_lock);
		}
	}
	
	protected void savePreferences(String cacheName) {
		SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPreference.edit();
		editor.putString(Common.PREFS_EDITOR_CACHENAME, cacheName);
		if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			editor.putString(Common.PREFS_EDITOR_CACHE_CHANGED, CacheManager.getPortraitCacheName());
		} else {
			editor.putString(Common.PREFS_EDITOR_CACHE_CHANGED, CacheManager.getLandscapeCacheName());
		}
		editor.commit();
	}
	
	protected void actionSave() {
		if (mIsWorking) return;
		if (mCropImageView == null) return;
		synchronized (mLock) {
			Bitmap bitmap = mCropImageView.getImageBitmap();
			if (bitmap != null) {
				final String cacheName = getCacheName();
				if (cacheName == null) return;
				showProgress(R.string.message_progress_saving);
				
				final Context context = this;
				new AsyncTask<Bitmap, Void, String>() {
					
					@Override
					protected String doInBackground(Bitmap... params) {
						if (CacheManager.putDiskCache(context, cacheName, params[0])) {
							return cacheName;
						} else { 
							return "ERROR";
						}
					}
					
					@Override
					protected void onPostExecute(String result) {
						hideProgress();
						Log.i(TAG, result);
						if (result.equals(cacheName)) {
							savePreferences(result);
							finish();
						} 
				    }
					
				}.execute(bitmap);
			}
		}
	}
}
