package com.s16.staticwallpaper.drawing;

import com.s16.staticwallpaper.utils.CacheManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;

public class WallpaperDrawable extends Drawable {
	
	protected static final String TAG = WallpaperDrawable.class.getSimpleName();
	
	public static final int ROTATE_0 = 0;
	public static final int ROTATE_90 = 90;
	public static final int ROTATE_180 = 180;
	public static final int ROTATE_270 = 270;
	
	public static final int POSITION_FILL = 0;
	public static final int POSITION_FIT = 1;
	public static final int POSITION_STRETCH = 2;
	public static final int POSITION_TILE = 3;
	public static final int POSITION_CENTER = 4;

	public interface OnImageChangedListener {
		public void onImageChanged();
	}
	private OnImageChangedListener mOnImageChangedListener;
	
	protected final Object mLock = new Object();
	
	private final Context mContext;
	private final Paint mPaint;
	private int mBitmapWidth = 0;
	private int mBitmapHeight = 0;
	private Drawable mSrcBitmapDrawable;
	private Bitmap mBitmapCache;
	private int mBackgroundColor = Color.BLACK;
	private int mPicturePosition = 0;
	private int mRotateMode = 0;
	private boolean mBitmapVisible;
	private boolean mChangeBitmap = true;
	
	public WallpaperDrawable(Context context) {
		mContext = context;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
	}
	
	public WallpaperDrawable(Context context, int backgroundColor) {
		mContext = context;
		mBackgroundColor = backgroundColor;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
	}
	
	public WallpaperDrawable(Context context, String bitmapCache, int picturePosition, int backgroundColor) {
		mContext = context;
		mPicturePosition = picturePosition;
		mBackgroundColor = backgroundColor;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		
		if (!TextUtils.isEmpty(bitmapCache)) {
			loadCache(bitmapCache);
		}
	}
	
	public WallpaperDrawable(Context context, Bitmap bitmap, int picturePosition, int backgroundColor) {
		mContext = context;
		mPicturePosition = picturePosition;
		mBackgroundColor = backgroundColor;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		
		if (bitmap != null) {
			mSrcBitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
			mBitmapWidth = bitmap.getWidth();
			mBitmapHeight = bitmap.getHeight();
			mBitmapVisible = true;
		}
	}
	
	public WallpaperDrawable(Context context, Drawable drawable, int width, int height, int picturePosition, int backgroundColor) {
		mContext = context;
		mPicturePosition = picturePosition;
		mBackgroundColor = backgroundColor;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		
		if (drawable != null) {
			mSrcBitmapDrawable = drawable;
			mBitmapWidth = width;
			mBitmapHeight = height;
			mBitmapVisible = true;
		}
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	@Override
	public void draw(Canvas canvas) {
		Rect rectPaint = new Rect();
		rectPaint.set(getBounds());
		if (rectPaint.width() > 0 && rectPaint.height() > 0) {
			final Paint paint = mPaint;
			if (mBitmapCache == null || mChangeBitmap) {
				mBitmapCache = Bitmap.createBitmap(rectPaint.width(), rectPaint.height(), Bitmap.Config.ARGB_8888);
				Canvas bitmapCanvas = new Canvas(mBitmapCache);
				bitmapCanvas.save();
				bitmapCanvas.drawColor(mBackgroundColor);
				
				if (mSrcBitmapDrawable != null && mBitmapVisible) {
					int srcWidth = mBitmapWidth;
					int srcHeight = mBitmapHeight;
					if (mRotateMode != ROTATE_0) {
						bitmapCanvas.translate(rectPaint.width(), rectPaint.height());
						bitmapCanvas.rotate((float)mRotateMode);
						
						if ((mRotateMode == ROTATE_90) || (mRotateMode == ROTATE_270)) {
							srcWidth = mBitmapHeight;
							srcHeight = mBitmapWidth;
						}
					}
					
					switch (mPicturePosition) {
						case POSITION_FILL: // Fill
						default:
							drawFillBitmap(bitmapCanvas, srcWidth, srcHeight);
							break;
						case POSITION_FIT: // Fit
							drawFitBitmap(bitmapCanvas, srcWidth, srcHeight);
							break;
						case POSITION_STRETCH: // Stretch
							drawStretchBitmap(bitmapCanvas, srcWidth, srcHeight);
							break;
						case POSITION_TILE: // Tile
							drawTileBitmap(bitmapCanvas, srcWidth, srcHeight);
							break;
						case POSITION_CENTER: // Center
							drawCenterBitmap(bitmapCanvas, srcWidth, srcHeight);
							break;
					}
				}
				bitmapCanvas.restore();
				System.gc();
				mChangeBitmap = false;
			}
			canvas.drawBitmap(mBitmapCache, null, rectPaint, paint);
		}
	}
	
	protected void loadCache(final String cacheName) {
		final Context context = getContext();
		if (!CacheManager.hasDiskCache(context, cacheName)) return;
		new AsyncTask<String, Void, Bitmap>() {
			
			@Override
			protected Bitmap doInBackground(String... params) {
				return CacheManager.popDiskCache(context, params[0]);
			}
			
			@Override
			protected void onPreExecute() {
		    }
			
			@Override
			protected void onPostExecute(final Bitmap bitmap) {
				if (bitmap != null) {
					mSrcBitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
					mBitmapWidth = bitmap.getWidth();
					mBitmapHeight = bitmap.getHeight();
					mChangeBitmap = true;
					onImageChanged();
					invalidateSelf();
				}
		    }
			
		}.execute(cacheName);
	}
	
	private void drawFillBitmap(Canvas bitmapCanvas, int bmpWidth, int bmpHeight) {
		if (mSrcBitmapDrawable == null) return;
		
		Rect rectPaint = bitmapCanvas.getClipBounds();
		int width = rectPaint.width();
		int height = rectPaint.height();
		
		float targetRatio = (float)rectPaint.width() / (float)rectPaint.height();
		float sourceRatio = (float)bmpWidth / (float)bmpHeight;
		if (sourceRatio >= targetRatio) { // source is wider than target in proportion
			height = rectPaint.height();
			width = (int)((float)height * sourceRatio);
		} else { // source is higher than target in proportion
			width = rectPaint.width();
			height = (int)((float)width / sourceRatio);
		}
		int x = (rectPaint.width() - width) / 2;
		int y = (rectPaint.height() - height) / 2;
		
		Rect rectDraw = new Rect(x, y, x + width, y + height);
		mSrcBitmapDrawable.setBounds(rectDraw);
		mSrcBitmapDrawable.draw(bitmapCanvas);
	}
	
	private void drawFitBitmap(Canvas bitmapCanvas, int bmpWidth, int bmpHeight) {
		if (mSrcBitmapDrawable == null) return;
		
		Rect rectPaint = bitmapCanvas.getClipBounds();
		int width = 0;
		int height = 0;
		float targetRatio = (float)rectPaint.width() / (float)rectPaint.height();
		float sourceRatio = (float)bmpWidth / (float)bmpHeight;
		if (sourceRatio >= targetRatio) { // source is wider than target in proportion
			width = rectPaint.width();
			height = (int)((float)width / sourceRatio);
		} else { // source is higher than target in proportion
			height = rectPaint.height();
		    width = (int)((float)height * sourceRatio);
		}
		int x = (rectPaint.width() - width) / 2;
		int y = (rectPaint.height() - height) / 2;
		
		Rect rectDraw = new Rect(x, y, x + width, y + height);
		mSrcBitmapDrawable.setBounds(rectDraw);
		mSrcBitmapDrawable.draw(bitmapCanvas);
	}
	
	private void drawStretchBitmap(Canvas bitmapCanvas, int bmpWidth, int bmpHeight) {
		if (mSrcBitmapDrawable == null) return;
		
		Rect rectPaint = bitmapCanvas.getClipBounds();
		mSrcBitmapDrawable.setBounds(rectPaint);
		mSrcBitmapDrawable.draw(bitmapCanvas);
	}
	
	private void drawTileBitmap(Canvas bitmapCanvas, int bmpWidth, int bmpHeight) {
		if (mSrcBitmapDrawable == null) return;
		
		Rect rectPaint = bitmapCanvas.getClipBounds();
		int numHoriz = (int) Math.ceil((rectPaint.width() / bmpWidth));
		int numVert = (int) Math.ceil(rectPaint.height() / bmpHeight);
		for (int i = 0; i <= numVert; i++) {
			for (int j = 0; j <= numHoriz; j++) {
				int x = j * bmpWidth;
				int y = i * bmpHeight;
				mSrcBitmapDrawable.setBounds(new Rect(x, y, x + bmpWidth, y + bmpHeight));
				mSrcBitmapDrawable.draw(bitmapCanvas);
			}
		}
	}
	
	private void drawCenterBitmap(Canvas bitmapCanvas, int bmpWidth, int bmpHeight) {
		if (mSrcBitmapDrawable == null) return;
		
		Rect rectPaint = bitmapCanvas.getClipBounds();
		int x = (rectPaint.width() - bmpWidth) / 2;
		int y = (rectPaint.height() - bmpHeight) / 2;
		Rect rectDraw = new Rect(x, y, x + bmpWidth, y + bmpHeight);
		mSrcBitmapDrawable.setBounds(rectDraw);
		mSrcBitmapDrawable.draw(bitmapCanvas);
	}

	@Override
	public void setAlpha(int alpha) {
		int oldAlpha = mPaint.getAlpha();
        if (alpha != oldAlpha) {
            mPaint.setAlpha(alpha);
            onImageChanged();
            invalidateSelf();
        }
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
		onImageChanged();
        invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return mPaint.getAlpha() < 255 ? android.graphics.PixelFormat.TRANSLUCENT : android.graphics.PixelFormat.OPAQUE;
	}
	
	@Override
	public int getIntrinsicWidth() {
        return mBitmapWidth;
    }
	
	@Override
	public int getIntrinsicHeight() {
        return mBitmapHeight;
    }
	
	private void onImageChanged() {
		synchronized (mLock) {
			if (mOnImageChangedListener != null) {
				mOnImageChangedListener.onImageChanged();
			}
		}
	}
	
	public void setOnImageChangedListener(OnImageChangedListener listener) {
		mOnImageChangedListener = listener;
	}
	
	public void setBitmapDrawable(Drawable value, int width, int height) {
		if (value != null) {
			mSrcBitmapDrawable = value;
			mBitmapWidth = width;
			mBitmapHeight = height;
			mChangeBitmap = true;
			onImageChanged();
			invalidateSelf();
		}
	}
	
	public void setBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			mSrcBitmapDrawable = new BitmapDrawable(getContext().getResources(), bitmap);
			mBitmapWidth = bitmap.getWidth();
			mBitmapHeight = bitmap.getHeight();
			mChangeBitmap = true;
			onImageChanged();
			invalidateSelf();
		}
	}
	
	public void setBitmapCache(String bitmapCache) {
		if (!TextUtils.isEmpty(bitmapCache)) {
			loadCache(bitmapCache);
		}
	}
	
	public void setBitmapVisibility(boolean visible) {
		if (mBitmapVisible != visible) {
			mBitmapVisible = visible;
			mChangeBitmap = true;
			onImageChanged();
			invalidateSelf();
		}
	}
	
	public boolean isBitmapVisibled() {
		return mBitmapVisible;
	}
	
	public void setBackgroundColor(int color) {
		if (mBackgroundColor != color) {
			mBackgroundColor = color;
			mChangeBitmap = true;
			onImageChanged();
			invalidateSelf();
		}
	}
	
	public int getBackgroundColor() {
		return mBackgroundColor;
	}
	
	public void setRotateMode(int mode) {
		if ((mode != ROTATE_0) && (mode != ROTATE_90) 
				&& (mode != ROTATE_180) && (mode != ROTATE_270)) return;
		
		if (mRotateMode != mode) {
			mRotateMode = mode;
			mChangeBitmap = true;
			onImageChanged();
			invalidateSelf();
		}
	}
	public int getRotateMode() {
		return mRotateMode;
	}
	
	public void setPicturePosition(int value) {
		if (value < POSITION_FILL || value > POSITION_CENTER) return;
		
		if (mPicturePosition != value) {
			mPicturePosition = value;
			mChangeBitmap = true;
			onImageChanged();
			invalidateSelf();
		}
	}
	public int getWallpaperMode() {
		return mPicturePosition;
	}

	public void recycle() {
		if (mBitmapCache != null) {
			mBitmapCache.recycle();
			mBitmapCache = null;
		}
		if (mSrcBitmapDrawable != null) {
			mSrcBitmapDrawable = null;
		}
	}
}
