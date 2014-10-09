package com.s16.staticwallpaper.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class SystemUtils {
	
	public static final int KITKAT = 19;
	
	public static boolean isTablet(Context context) {
	    boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
	    boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
	    return (xlarge || large);
	}

	public static int getScreenOrientation(Context context) {
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = manager.getDefaultDisplay().getRotation();
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        	if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270) {
        		return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        	} else {
        		return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        	}
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
        		return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        	} else {
        		return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        	}
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }
	
	// Display
	public static Point getScreenSize(Context context) {
		
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
	    int realWidth;
	    int realHeight;

	    if (Build.VERSION.SDK_INT >= 17){
	        //new pleasant way to get real metrics
	        DisplayMetrics realMetrics = new DisplayMetrics();
	        display.getRealMetrics(realMetrics);
	        realWidth = realMetrics.widthPixels;
	        realHeight = realMetrics.heightPixels;
	    } else if (Build.VERSION.SDK_INT >= 14) {
	        //reflection for this weird in-between time
	        try {
	            Method mGetRawH = Display.class.getMethod("getRawHeight");
	            Method mGetRawW = Display.class.getMethod("getRawWidth");
	            realWidth = (Integer)mGetRawW.invoke(display);
	            realHeight = (Integer)mGetRawH.invoke(display);
	        } catch (Exception e) {
	            //this may not be 100% accurate, but it's all we've got
	        	Point srcSize = new Point();
	        	display.getSize(srcSize);
	            realWidth = srcSize.x;
	            realHeight = srcSize.y;
	            Log.e("Display Info", "Couldn't use reflection to get the real display metrics.");
	        }
	    } else {
	        //This should be close, as lower API devices should not have window navigation bars
	        //realWidth = display.getWidth();
	        //realHeight = display.getHeight();
	    	Point srcSize = new Point();
        	display.getSize(srcSize);
            realWidth = srcSize.x;
            realHeight = srcSize.y;
	    }
	    return new Point(realWidth, realHeight);
	}
	
	public static void showSystemNotifications(Context context) {
		Object sbservice = context.getSystemService("statusbar");
		Class<?> statusbarManager = null;
		try {
			statusbarManager = Class.forName("android.app.StatusBarManager");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (statusbarManager != null) {
			Method showsb = null;
			try {
				if (Build.VERSION.SDK_INT >= 17) {
				    showsb = statusbarManager.getMethod("expandNotificationsPanel");
				}
				else {
				    showsb = statusbarManager.getMethod("expand");
				}	
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			
			if (showsb != null) {
				try {
					showsb.invoke(sbservice);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
