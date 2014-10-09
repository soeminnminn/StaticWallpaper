package com.s16.staticwallpaper.drawing;

import com.s16.staticwallpaper.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class PreviewDrawable extends Drawable {
	
	protected static Rect phone_landscape = new Rect(0, 0, 349, 209);
	protected static Rect phone_portrait = new Rect(0, 0, 209, 349);
	protected static Rect tablet_landscape = new Rect(0, 0, 553, 369);
	protected static Rect tablet_portrait = new Rect(0, 0, 326, 489);
	
	protected static Rect phone_landscape_inner = new Rect(70, 37, 70 + 199, 37 + 135);
	protected static Rect phone_portrait_inner = new Rect(37, 70, 37 + 135, 70 + 199);
	protected static Rect tablet_landscape_inner = new Rect(93, 77, 93 + 367, 77 + 209);
	protected static Rect tablet_portrait_inner = new Rect(62, 89, 62 + 202, 89 + 306);
	
	private final Context mContext;
	private final Paint mDrawPaint;
	private final Paint mPaint;
	private int mBkgBitmapWidth = 0;
	private int mBkgBitmapHeight = 0;
	private int mOrigBitmapWidth = 0;
	private int mOrigBitmapHeight = 0;
	private int mSrcBitmapWidth = 0;
	private int mSrcBitmapHeight = 0;
	private Drawable mSrcBitmapDrawable;
	private Drawable mBkgBitmapDrawable;
	private Bitmap mBitmapCache;
	private WallpaperDrawable mWallpaperDrawable;
	private int mPicturePosition = 0;
	private int mBackgroundColor = Color.BLACK;
	private int mRotation = 0;
	private Rect mRectInner;
	private boolean mChangeBitmap = true;
	private boolean mClearBitmap = false;
	
	public PreviewDrawable(Context context, int backgroundMode, int backgroundColor) {
		mContext = context;
		mBackgroundColor = backgroundColor;
		
		mRectInner = new Rect();
		mDrawPaint = new Paint();
		mDrawPaint.setAntiAlias(true);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		
		initialize(context.getResources(), backgroundMode, 0, 0);
		createWallpaperDrawable();
	}

	public PreviewDrawable(Context context, Bitmap bitmap, int backgroundMode, int picturePosition, int backgroundColor) {
		mContext = context;
		mPicturePosition = picturePosition;
		mBackgroundColor = backgroundColor;
		
		mRectInner = new Rect();
		mDrawPaint = new Paint();
		mDrawPaint.setAntiAlias(true);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		
		if (bitmap != null) {
			Resources res = context.getResources();
			mSrcBitmapDrawable = new BitmapDrawable(res, bitmap);
			mOrigBitmapWidth = bitmap.getWidth();
			mOrigBitmapHeight = bitmap.getHeight();
			initialize(context.getResources(), backgroundMode, bitmap.getWidth(), bitmap.getHeight());
		}
		createWallpaperDrawable();
	}
	
	public PreviewDrawable(Context context, int resId, int width, int height, int backgroundMode, int picturePosition, int backgroundColor) {
		mContext = context;
		mPicturePosition = picturePosition;
		mBackgroundColor = backgroundColor;
		
		mRectInner = new Rect();
		mDrawPaint = new Paint();
		mDrawPaint.setAntiAlias(true);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		
		if (resId > -1) {
			Resources res = context.getResources();
			mSrcBitmapDrawable = res.getDrawable(resId);
			mOrigBitmapWidth = width;
			mOrigBitmapHeight = height;
			initialize(context.getResources(), backgroundMode, width, height);
		}
		createWallpaperDrawable();
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	private void initialize(Resources res, int backgroundMode, int bmpWidth, int bmpHeight) {
		switch (backgroundMode) {
			case 1:
				mBkgBitmapDrawable = res.getDrawable(R.drawable.phone_landscape);
				mRectInner.set(phone_landscape_inner);
				mBkgBitmapWidth = phone_landscape.width();
				mBkgBitmapHeight = phone_landscape.height();
				break;
			case 2:
				mBkgBitmapDrawable = res.getDrawable(R.drawable.tablet_portrait);
				mRectInner.set(tablet_portrait_inner);
				mBkgBitmapWidth = tablet_portrait.width();
				mBkgBitmapHeight = tablet_portrait.height();
				break;
			case 3:
				mBkgBitmapDrawable = res.getDrawable(R.drawable.tablet_landscape);
				mRectInner.set(tablet_landscape_inner);
				mBkgBitmapWidth = tablet_landscape.width();
				mBkgBitmapHeight = tablet_landscape.height();
				break;
			default:
				mBkgBitmapDrawable = res.getDrawable(R.drawable.phone_portrait);
				mRectInner.set(phone_portrait_inner);
				mBkgBitmapWidth = phone_portrait.width();
				mBkgBitmapHeight = phone_portrait.height();
				break;
		}
		
		calculateSize(bmpWidth, bmpHeight);
	}
	
	private void calculateSize(int bmpWidth, int bmpHeight) {
		if (bmpWidth == 0 || bmpHeight == 0) return;
		
		int width = mRectInner.width();
		int height = mRectInner.height();
		float targetRatio = (float)width / (float)height;
		float sourceRatio = (float)bmpWidth / (float)bmpHeight;
		if(sourceRatio >= targetRatio){ // source is wider than target in proportion
			mSrcBitmapWidth = width;
			mSrcBitmapHeight = Math.round((float)width / sourceRatio);
		}else{ // source is higher than target in proportion
			mSrcBitmapHeight = height;
		    mSrcBitmapWidth = Math.round((float)height * sourceRatio);
		}
	}
	
	private void createWallpaperDrawable() {
		if (mWallpaperDrawable != null) return;
		if (mSrcBitmapDrawable == null) {
			mWallpaperDrawable = new WallpaperDrawable(getContext(), mBackgroundColor);
		} else {
			mWallpaperDrawable = new WallpaperDrawable(getContext(), mSrcBitmapDrawable, mSrcBitmapWidth, mSrcBitmapHeight, mPicturePosition, mBackgroundColor);
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		Rect rectPaint = new Rect();
		rectPaint.set(getBounds());
		if (rectPaint.width() > 0 && rectPaint.height() > 0) {
			Paint paint = mPaint;

			if (mBitmapCache == null || mChangeBitmap) {
				mBitmapCache = Bitmap.createBitmap(mBkgBitmapWidth, mBkgBitmapHeight, Bitmap.Config.ARGB_8888);
				Canvas bitmapCanvas = new Canvas(mBitmapCache);
				
				// Draw Wallpaper
				mWallpaperDrawable.setBounds(mRectInner);
				mWallpaperDrawable.draw(bitmapCanvas);
				
				// Draw Background Frame
				mBkgBitmapDrawable.setBounds(new Rect(0, 0, mBkgBitmapWidth, mBkgBitmapHeight));
				mBkgBitmapDrawable.draw(bitmapCanvas);

				mChangeBitmap = false;
				System.gc();
			}
			
			canvas.drawBitmap(mBitmapCache, null, rectPaint, paint);
		}
	}

	@Override
	public void setAlpha(int alpha) {
		int oldAlpha = mPaint.getAlpha();
        if (alpha != oldAlpha) {
            mPaint.setAlpha(alpha);
            invalidateSelf();
        }
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
        invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return mPaint.getAlpha() < 255 ? android.graphics.PixelFormat.TRANSLUCENT : android.graphics.PixelFormat.OPAQUE;
	}

	@Override
	public int getIntrinsicWidth() {
        return mBkgBitmapWidth;
    }
	
	@Override
	public int getIntrinsicHeight() {
        return mBkgBitmapHeight;
    }
	
	public int getOriginalWidth() {
        return mOrigBitmapWidth;
    }
	
	public int getOriginalHeight() {
        return mOrigBitmapHeight;
    }
	
	public void setBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			mSrcBitmapDrawable = new BitmapDrawable(getContext().getResources(), bitmap);
			mOrigBitmapWidth = bitmap.getWidth();
			mOrigBitmapHeight = bitmap.getHeight();
			calculateSize(mOrigBitmapWidth, mOrigBitmapHeight);
			mClearBitmap = false;
			mWallpaperDrawable.setBitmapDrawable(mSrcBitmapDrawable, mSrcBitmapWidth, mSrcBitmapHeight);
			mWallpaperDrawable.setBitmapVisibility(true);
			mChangeBitmap = true;
			invalidateSelf();
		}
	}
	
	public Bitmap getOriginalBitmap() {
		if (mSrcBitmapDrawable != null) {
			Bitmap bitmap = Bitmap.createBitmap(mOrigBitmapWidth, mOrigBitmapHeight, Bitmap.Config.ARGB_8888);
			Canvas bitmapCanvas = new Canvas(bitmap);
			
			mSrcBitmapDrawable.setBounds(new Rect(0, 0, mOrigBitmapWidth, mOrigBitmapHeight));
			mSrcBitmapDrawable.draw(bitmapCanvas);
			
			return bitmap;
		}
		return null;
	}
	
	public Bitmap getOriginalBitmap(int width, int height) {
		if (mSrcBitmapDrawable != null) {
			int destWidth = mOrigBitmapWidth;
			int destHeight = mOrigBitmapHeight;
			if (mOrigBitmapWidth > width || mOrigBitmapHeight > height) {
				Point destPoint = BitmapUtils.calculateAspectRatio(mOrigBitmapWidth, width, mOrigBitmapHeight, height);
				destWidth = destPoint.x;
				destHeight = destPoint.y;
			}
			
			Bitmap bitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
			Canvas bitmapCanvas = new Canvas(bitmap);
			
			mSrcBitmapDrawable.setBounds(new Rect(0, 0, destWidth, destHeight));
			mSrcBitmapDrawable.draw(bitmapCanvas);
			
			return bitmap;
		}
		return null;
	}
	
	public void clearBitmap() {
		mClearBitmap = true;
		mWallpaperDrawable.setBitmapVisibility(false);
		mChangeBitmap = true;
		invalidateSelf();
	}
	
	public boolean isBitmapCleared() {
		return mClearBitmap;
	}
	
	public void setBackgroundColor(int color) {
		if (mBackgroundColor != color) {
			mBackgroundColor = color;
			mWallpaperDrawable.setBackgroundColor(mBackgroundColor);
			mChangeBitmap = true;
			invalidateSelf();
		}
	}
	public int getBackgroundColor() {
		return mBackgroundColor;
	}
	
	public void setRotation(int mode) {
		if ((mode != WallpaperDrawable.ROTATE_0) && (mode != WallpaperDrawable.ROTATE_90) 
				&& (mode != WallpaperDrawable.ROTATE_180) && (mode != WallpaperDrawable.ROTATE_270)) return;
		
		if (mRotation != mode) {
			mRotation = mode;
			mWallpaperDrawable.setRotateMode(mRotation);
			mChangeBitmap = true;
			invalidateSelf();
		}
	}
	public int getRotation() {
		return mRotation;
	}
	
	public void setPicturePosition(int value) {
		if (value < WallpaperDrawable.POSITION_FILL || value > WallpaperDrawable.POSITION_CENTER) return;
		
		if (mPicturePosition != value) {
			mPicturePosition = value;
			mWallpaperDrawable.setPicturePosition(mPicturePosition);
			mChangeBitmap = true;
			invalidateSelf();
		}
	}
	public int getPicturePosition() {
		return mPicturePosition;
	}
	
	public void recycle() {
		if (mBitmapCache != null) {
			mBitmapCache.recycle();
			mBitmapCache = null;
		}
		if (mWallpaperDrawable != null) {
			mWallpaperDrawable.recycle();
			mWallpaperDrawable = null;
		}
		if (mSrcBitmapDrawable != null) {
			mSrcBitmapDrawable = null;
		}
		if (mBkgBitmapDrawable != null) {
			mBkgBitmapDrawable = null;
		}
	}
}
