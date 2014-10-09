package com.s16.staticwallpaper.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class ColorBoxDrawable extends Drawable {

	private final Context mContext;
	private final Paint mBoxPaint;
	
	public ColorBoxDrawable(Context context, int color) {
		mContext = context;
		mBoxPaint = new Paint();
		mBoxPaint.setColor(color);
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	protected int dpToPx(int dp) {
		float density = getContext().getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
	
	@Override
	public void draw(Canvas canvas) {
		canvas.drawRect(getBounds(), mBoxPaint);
	}

	@Override
	public void setAlpha(int alpha) {
		mBoxPaint.setAlpha(alpha);
		invalidateSelf();
	}
	
	public void setColor(int color) {
		mBoxPaint.setColor(color);
		invalidateSelf();
	}
	
	@Override
	public void setColorFilter(ColorFilter cf) {
		mBoxPaint.setColorFilter(cf);
		invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return mBoxPaint.getAlpha() < 255 ? android.graphics.PixelFormat.TRANSLUCENT : android.graphics.PixelFormat.OPAQUE;
	}

	@Override
	public int getIntrinsicWidth() {
        return dpToPx(46);
    }
	
	@Override
	public int getIntrinsicHeight() {
        return dpToPx(30);
    }
}
