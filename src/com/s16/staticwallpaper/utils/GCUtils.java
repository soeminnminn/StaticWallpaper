package com.s16.staticwallpaper.utils;

import android.text.format.DateUtils;
import android.util.Log;

public class GCUtils {
	protected static final String TAG = GCUtils.class.getSimpleName();
    public static final int GC_TRY_COUNT = 2;
    // GC_TRY_LOOP_MAX is used for the hard limit of GC wait,
    // GC_TRY_LOOP_MAX should be greater than GC_TRY_COUNT.
    public static final int GC_TRY_LOOP_MAX = 5;
    private static final long GC_INTERVAL = DateUtils.SECOND_IN_MILLIS;
    private static GCUtils sInstance = new GCUtils();
    private int mGCTryCount = 0;

    public static GCUtils getInstance() {
        return sInstance;
    }

    public void reset() {
        mGCTryCount = 0;
    }

    public boolean tryGCOrWait(Throwable t) {
        if (mGCTryCount == 0) {
            System.gc();
        }
        if (++mGCTryCount > GC_TRY_COUNT) {
        	t.printStackTrace();
            return false;
        } else {
            try {
                Thread.sleep(GC_INTERVAL);
                return true;
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep was interrupted.");
                t.printStackTrace();
                return false;
            }
        }
    }
}
