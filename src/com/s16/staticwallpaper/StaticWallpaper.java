package com.s16.staticwallpaper;

import com.s16.screenoff.ScreenOffUtils;
import com.s16.staticwallpaper.WallpaperGestureListener.ACTIONS;
import com.s16.staticwallpaper.activity.ScreenOffActivity;
import com.s16.staticwallpaper.drawing.WallpaperDrawable;
import com.s16.staticwallpaper.utils.SystemUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;

public class StaticWallpaper extends WallpaperService {

	protected static final String TAG = StaticWallpaper.class.getSimpleName();
	private final Handler handler = new Handler();
	
	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}
	
	@Override
    public void onDestroy() {
        super.onDestroy();
    }
	
	class WallpaperEngine extends Engine 
			implements SharedPreferences.OnSharedPreferenceChangeListener
			, WallpaperDrawable.OnImageChangedListener
			, WallpaperGestureListener.OnGestureActionListener {

		private final Object mLock = new Object();
		private final Paint mPaint = new Paint();
		
		private WallpaperDrawable mPortrait;
		private WallpaperDrawable mLandscape;
		
		private GestureDetector mGestureDetector;
		private ScaleGestureDetector mScaleGestureDetector;
		private WallpaperGestureListener mGestureListener;
		private boolean mActionDownEnabled;
		private boolean mActionPinchInEnabled;
		
		private final Runnable drawRunnable = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
		
		public WallpaperEngine() {
			SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            setting.registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            
            if (!isPreview()) {
            	setTouchEventsEnabled(true);
            	final Context context = getApplicationContext();
            	
            	mGestureListener = new WallpaperGestureListener(context);
            	mGestureListener.setOnGestureActionListener(this);
            	mGestureDetector = new GestureDetector(context, mGestureListener);
            	mScaleGestureDetector = new ScaleGestureDetector(context, mGestureListener);
            }
            
            final Paint paint = mPaint;
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            
            mPortrait = new WallpaperDrawable(getApplicationContext(), Common.DEFAULT_BKG_COLOR);
            mPortrait.setOnImageChangedListener(this);
            mLandscape = new WallpaperDrawable(getApplicationContext(), Common.DEFAULT_BKG_COLOR);
            mLandscape.setOnImageChangedListener(this);
		}
		
		@Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawRunnable);
		}
		
		@Override
        public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			if (visible) {
				update();
                drawFrame();
            } else {
                handler.removeCallbacks(drawRunnable);
            }
		}
		
		@Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }
		
		@Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawFrame();
        }
		
		@Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            handler.removeCallbacks(drawRunnable);
        }
		
		@Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
            if (isPreview()) return;
            drawFrame();
        }
		
		@Override
		public void onTouchEvent(MotionEvent event) {
			if (!isPreview() && mGestureDetector != null && mScaleGestureDetector != null) {
				if (mActionDownEnabled) {
					mGestureDetector.onTouchEvent(event);
				}
				if (mActionPinchInEnabled) {
					mScaleGestureDetector.onTouchEvent(event);
				}
				return;
			}
			super.onTouchEvent(event);
        }
		
		private void drawFrame() {
			final SurfaceHolder holder = getSurfaceHolder();
            final Rect frame = holder.getSurfaceFrame();
            final int width = frame.width();
            final int height = frame.height();
            final int orientation = width < height ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                	final Paint paint = mPaint;
                	Rect bounds = new Rect(0, 0, width, height);
                	
                	if (mPortrait != null && mLandscape != null) {
                		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                			mPortrait.setBounds(bounds);
                			mPortrait.draw(c);
                    	} else {
                    		mLandscape.setBounds(bounds);
                    		mLandscape.draw(c);
                    	}
                	} else {
                		c.drawPaint(paint);
                	}
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
		}
		
		protected void update() {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
			if (isVisible()) {
				mActionDownEnabled = sharedPreferences.getBoolean(Common.PREFS_ACTION_DOWN, false);
				mActionPinchInEnabled = sharedPreferences.getBoolean(Common.PREFS_ACTION_PINCH_IN, false);
				
				updatePortrait(sharedPreferences);
				updateLandscape(sharedPreferences);
				drawFrame();
			}
		}
		
		protected void updatePortrait(SharedPreferences sharedPreferences) {
			if (sharedPreferences != null) {
				synchronized (mLock) {
					boolean noImage = sharedPreferences.getBoolean(Common.PREFS_PORT_NOIMAGE, true);
					int bkgColor = sharedPreferences.getInt(Common.PREFS_PORT_BACK_COLOR, Common.DEFAULT_BKG_COLOR);
					int picturePosition = sharedPreferences.getInt(Common.PREFS_PORT_POSITION, WallpaperDrawable.POSITION_FILL);
					String cacheName = sharedPreferences.getString(Common.PREFS_PORT_CACHENAME, null);
					if (noImage || TextUtils.isEmpty(cacheName)) {
						if (mPortrait == null) {
							mPortrait = new WallpaperDrawable(getApplicationContext(), bkgColor);
							mPortrait.setOnImageChangedListener(this);
						} else {
							mPortrait.setBackgroundColor(bkgColor);
							mPortrait.setBitmapVisibility(false);
						}
					} else {
						if (mPortrait == null) {
							mPortrait = new WallpaperDrawable(getApplicationContext(), cacheName, picturePosition, bkgColor);
							mPortrait.setOnImageChangedListener(this);
						} else {
							mPortrait.setBackgroundColor(bkgColor);
							mPortrait.setBitmapVisibility(true);
							mPortrait.setPicturePosition(picturePosition);
							mPortrait.setBitmapCache(cacheName);
						}
					}
				}
			}
		}
		
		protected void updateLandscape(SharedPreferences sharedPreferences) {
			if (sharedPreferences != null) {
				synchronized (mLock) {
					boolean noImage = sharedPreferences.getBoolean(Common.PREFS_LAND_NOIMAGE, true);
					int bkgColor = sharedPreferences.getInt(Common.PREFS_LAND_BACK_COLOR, Color.BLACK);
					int picturePosition = sharedPreferences.getInt(Common.PREFS_LAND_POSITION, WallpaperDrawable.POSITION_FILL);
					String cacheName = sharedPreferences.getString(Common.PREFS_LAND_CACHENAME, null);
					if (noImage || TextUtils.isEmpty(cacheName)) {
						if (mLandscape == null) {
							mLandscape = new WallpaperDrawable(getApplicationContext(), bkgColor);
							mLandscape.setOnImageChangedListener(this);
						} else {
							mLandscape.setBackgroundColor(bkgColor);
							mLandscape.setBitmapVisibility(false);
						}
					} else {
						if (mLandscape == null) {
							mLandscape = new WallpaperDrawable(getApplicationContext(), cacheName, picturePosition, bkgColor);
							mLandscape.setOnImageChangedListener(this);
						} else {
							mLandscape.setBackgroundColor(bkgColor);
							mLandscape.setBitmapVisibility(true);
							mLandscape.setPicturePosition(picturePosition);
							mLandscape.setBitmapCache(cacheName);
						}
					}
				}
			}
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(Common.PREFS_CACHE_CHANGED)) {
				String cacheKey = sharedPreferences.getString(key, null);
				if (cacheKey != null && cacheKey.length() > 0) {
					if (cacheKey.charAt(0) == 'P') {
						updatePortrait(sharedPreferences);
					} else {
						updateLandscape(sharedPreferences);
					}
				}
			} else if (key.equals(Common.PREFS_ACTION_DOWN)) {
				mActionDownEnabled = sharedPreferences.getBoolean(key, false);
			} else if (key.equals(Common.PREFS_ACTION_PINCH_IN)) {
				mActionPinchInEnabled = sharedPreferences.getBoolean(key, false);
			}
		}

		@Override
		public void onImageChanged() {
			synchronized (mLock) {
				if (isVisible()) {
					drawFrame();
				}	
			}
		}
		
		@Override
		public boolean onGestureAction(ACTIONS action) {
			//Log.i(TAG, "onGestureAction=" + action);
			if (mActionDownEnabled && action == WallpaperGestureListener.ACTIONS.SwipeDown) {
				SystemUtils.showSystemNotifications(getApplicationContext());
				return true;
			} else if (mActionPinchInEnabled && action == WallpaperGestureListener.ACTIONS.PinchIn) {
				if (ScreenOffUtils.requestDeviceAdmin(getApplication(), ScreenOffActivity.class)) {
					ScreenOffUtils.turnScreenOff(getApplicationContext());
				}
				return true;
			}
			return false;
		}
	}
}
