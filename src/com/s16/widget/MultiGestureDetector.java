package com.s16.widget;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public class MultiGestureDetector {
	
	private final ScaleGestureDetector mScaleGestureDetector;
	private final GestureDetector mGestureDetector;
	
	public MultiGestureDetector(Context context, OnMultiGestureListener listener) {
		mScaleGestureDetector = new ScaleGestureDetector(context, listener);
		mGestureDetector = new GestureDetector(context, listener);
	}
	
	public MultiGestureDetector(Context context, OnMultiGestureListener listener, Handler handler) {
		mScaleGestureDetector = new ScaleGestureDetector(context, listener, handler);
		mGestureDetector = new GestureDetector(context, listener, handler);
	}
	
	public interface OnMultiGestureListener extends GestureDetector.OnGestureListener
			, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {
		
	}
	
	public static class SimpleOnMultiGestureListener implements OnMultiGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
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
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			return false;
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
		}
		
	}
	
	 /**
     * Analyzes the given motion event and if applicable triggers the
     * appropriate callbacks on the {@link OnGestureListener} supplied.
     *
     * @param ev The current motion event.
     * @return true if the {@link OnGestureListener} consumed the event,
     *              else false.
     */
    public boolean onTouchEvent(MotionEvent event) {
    	if (event.getPointerCount() == 1) {
    		return mGestureDetector.onTouchEvent(event);
    	} else {
    		return mScaleGestureDetector.onTouchEvent(event);
    	}
    }
	
	/**
     * Sets the listener which will be called for double-tap and related
     * gestures.
     * 
     * @param onDoubleTapListener the listener invoked for all the callbacks, or
     *        null to stop listening for double-tap gestures.
     */
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener) {
    	mGestureDetector.setOnDoubleTapListener(onDoubleTapListener);
    }

    /**
     * Set whether longpress is enabled, if this is enabled when a user
     * presses and holds down you get a longpress event and nothing further.
     * If it's disabled the user can press and hold down and then later
     * moved their finger and you will get scroll events. By default
     * longpress is enabled.
     *
     * @param isLongpressEnabled whether longpress should be enabled.
     */
    public void setIsLongpressEnabled(boolean isLongpressEnabled) {
    	mGestureDetector.setIsLongpressEnabled(isLongpressEnabled);
    }

    /**
     * @return true if longpress is enabled, else false.
     */
    public boolean isLongpressEnabled() {
        return mGestureDetector.isLongpressEnabled();
    }
    
    /**
     * Set whether the associated {@link OnScaleGestureListener} should receive onScale callbacks
     * when the user performs a doubleTap followed by a swipe. Note that this is enabled by default
     * if the app targets API 19 and newer.
     * @param scales true to enable quick scaling, false to disable
     */
    public void setQuickScaleEnabled(boolean scales) {
    	mScaleGestureDetector.setQuickScaleEnabled(scales);
    }
	
    /**
     * Return whether the quick scale gesture, in which the user performs a double tap followed by a
     * swipe, should perform scaling. {@see #setQuickScaleEnabled(boolean)}.
     */
      public boolean isQuickScaleEnabled() {
          return mScaleGestureDetector.isQuickScaleEnabled();
      }

      /**
       * Returns {@code true} if a scale gesture is in progress.
       */
      public boolean isInProgress() {
          return mScaleGestureDetector.isInProgress();
      }

      /**
       * Get the X coordinate of the current gesture's focal point.
       * If a gesture is in progress, the focal point is between
       * each of the pointers forming the gesture.
       *
       * If {@link #isInProgress()} would return false, the result of this
       * function is undefined.
       *
       * @return X coordinate of the focal point in pixels.
       */
      public float getFocusX() {
          return mScaleGestureDetector.getFocusX();
      }

      /**
       * Get the Y coordinate of the current gesture's focal point.
       * If a gesture is in progress, the focal point is between
       * each of the pointers forming the gesture.
       *
       * If {@link #isInProgress()} would return false, the result of this
       * function is undefined.
       *
       * @return Y coordinate of the focal point in pixels.
       */
      public float getFocusY() {
          return mScaleGestureDetector.getFocusY();
      }

      /**
       * Return the average distance between each of the pointers forming the
       * gesture in progress through the focal point.
       *
       * @return Distance between pointers in pixels.
       */
      public float getCurrentSpan() {
          return mScaleGestureDetector.getCurrentSpan();
      }

      /**
       * Return the average X distance between each of the pointers forming the
       * gesture in progress through the focal point.
       *
       * @return Distance between pointers in pixels.
       */
      public float getCurrentSpanX() {
          return mScaleGestureDetector.getCurrentSpanX();
      }

      /**
       * Return the average Y distance between each of the pointers forming the
       * gesture in progress through the focal point.
       *
       * @return Distance between pointers in pixels.
       */
      public float getCurrentSpanY() {
          return mScaleGestureDetector.getCurrentSpanY();
      }

      /**
       * Return the previous average distance between each of the pointers forming the
       * gesture in progress through the focal point.
       *
       * @return Previous distance between pointers in pixels.
       */
      public float getPreviousSpan() {
          return mScaleGestureDetector.getPreviousSpan();
      }

      /**
       * Return the previous average X distance between each of the pointers forming the
       * gesture in progress through the focal point.
       *
       * @return Previous distance between pointers in pixels.
       */
      public float getPreviousSpanX() {
          return mScaleGestureDetector.getPreviousSpanX();
      }

      /**
       * Return the previous average Y distance between each of the pointers forming the
       * gesture in progress through the focal point.
       *
       * @return Previous distance between pointers in pixels.
       */
      public float getPreviousSpanY() {
          return mScaleGestureDetector.getPreviousSpanY();
      }

      /**
       * Return the scaling factor from the previous scale event to the current
       * event. This value is defined as
       * ({@link #getCurrentSpan()} / {@link #getPreviousSpan()}).
       *
       * @return The current scaling factor.
       */
      public float getScaleFactor() {
          return mScaleGestureDetector.getScaleFactor();
      }

      /**
       * Return the time difference in milliseconds between the previous
       * accepted scaling event and the current scaling event.
       *
       * @return Time difference since the last scaling event in milliseconds.
       */
      public long getTimeDelta() {
          return mScaleGestureDetector.getTimeDelta();
      }

      /**
       * Return the event time of the current event being processed.
       *
       * @return Current event time in milliseconds.
       */
      public long getEventTime() {
          return mScaleGestureDetector.getEventTime();
      }
}
