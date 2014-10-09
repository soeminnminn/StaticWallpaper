package com.s16.staticwallpaper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.graphics.Color;

public class Common {
	
	public static final int DEFAULT_BKG_COLOR = Color.BLACK;
	
	// Activity Request Codes
	public static final int REQUEST_PICK_IMAGE = 0x100;
	public static final int REQUEST_PICK_IMAGE_PORT = 0x101;
	public static final int REQUEST_PICK_IMAGE_LAND = 0x102;
	public static final int REQUEST_CROP_IMAGE = 0x200;
	
	// Orientation
	public static String ORIENTATION_KEY = "orientation";
	public static String ORIENTATION_PORT = "orientation_portrait";
	public static String ORIENTATION_LAND = "orientation_landscape";
	
	// Argements
	public static final String ARG_NO_IMAGE = "arg_no_image";
	public static final String ARG_BKG_COLOR = "arg_bkg_color";
	public static final String ARG_OPEN_TYPE = "arg_open_type";
	public static final String ARG_TYPE = "arg_type";
	public static final String ARG_POSITION = "arg_position";
	public static final String ARG_CACHENAME = "arg_cachename";
	public static final String ARG_EDITOR_CACHENAME = "arg_editor_cachename";
	public static final String ARG_ORIENTATION = "arg_orientation";
	
	// Preferences
	public static final String PREFS_CACHE_CHANGED = "prefs_cache_changed";
	public static final String PREFS_EDITOR_CACHE_CHANGED = "prefs_editor_cache_changed";
	public static final String PREFS_EDITOR_CACHENAME = "prefs_editor_cachename";
	
	public static final String PREFS_PORT_NOIMAGE = "prefs_port_noimage";
	public static final String PREFS_PORT_BACK_COLOR = "prefs_port_back_color";
	public static final String PREFS_PORT_POSITION = "prefs_port_position";
	public static final String PREFS_PORT_CACHENAME = "prefs_port_cachename";
	
	public static final String PREFS_LAND_NOIMAGE = "prefs_port_noimage";
	public static final String PREFS_LAND_BACK_COLOR = "prefs_land_back_color";
	public static final String PREFS_LAND_POSITION = "prefs_land_position";
	public static final String PREFS_LAND_CACHENAME = "prefs_land_cachename";
	
	public static final String PREFS_ABOUT = "prefs_about";
	public static final String PREFS_ACTION_DOWN = "prefs_action_down";
	public static final String PREFS_ACTION_PINCH_IN = "prefs_action_pinch_in";
	
	// SharedPreference
	public static String getCurrentDateString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
		return format.format(Calendar.getInstance().getTime());
	}
}
