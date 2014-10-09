package com.s16.staticwallpaper;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class WallpaperGestureListener 
		implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
			, ScaleGestureDetector.OnScaleGestureListener {

	protected static final String TAG = WallpaperGestureListener.class.getSimpleName();
	
	public enum ACTIONS {
		None, SwipeDown, SwipeUp, LongPress, SingleTap, DoubleTap, PinchIn, PinchOut
	}
	
	public interface OnGestureActionListener {
		public boolean onGestureAction(ACTIONS action);
	}
	
	private Context mContext;
	private ACTIONS mCurrentAction;
	private OnGestureActionListener mOnGestureActionListener;
	
	public WallpaperGestureListener(Context context) {
		mContext = context;
		mCurrentAction = ACTIONS.None;
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	public void setOnGestureActionListener(OnGestureActionListener listener) {
		mOnGestureActionListener = listener;
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		mCurrentAction = ACTIONS.None;
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		if (mCurrentAction != ACTIONS.None) return;
		mCurrentAction = ACTIONS.LongPress;
		onAction();
	}

	@Override
	public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
		if (mCurrentAction != ACTIONS.None) return false;
		if (start.getPointerCount() == 1 && finish.getPointerCount() == 1) { 
			if (start.getRawY() < finish.getRawY()) {
				mCurrentAction = ACTIONS.SwipeDown;
				return onAction();
			} else if (start.getRawY() > finish.getRawY()) {
				mCurrentAction = ACTIONS.SwipeUp;
				return onAction();
			}
		}
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (mCurrentAction != ACTIONS.None) return false;
		mCurrentAction = ACTIONS.SingleTap;
		return onAction();
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (mCurrentAction != ACTIONS.None) return false;
		mCurrentAction = ACTIONS.DoubleTap;
		return onAction();
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		return false;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		if (detector.getPreviousSpan() > detector.getCurrentSpan()) {
			mCurrentAction = ACTIONS.PinchIn;
			onAction();
		} else if (detector.getPreviousSpan() < detector.getCurrentSpan()) {
			mCurrentAction = ACTIONS.PinchOut;
			onAction();
		}
	}
	
	protected boolean onAction() {
		if (mOnGestureActionListener != null) {
			return mOnGestureActionListener.onGestureAction(mCurrentAction);
		}
		return false;
	}
}
