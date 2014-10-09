package com.s16.staticwallpaper.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.s16.staticwallpaper.Common;
import com.s16.staticwallpaper.utils.SimpleCacheManager.CacheTransactionException;

public class CacheManager {
	
	public static String CACHE_NAME_KEY = "cache_name";
	public static String CACHE_PORTRAIT = "cache_portrait";
	public static String CACHE_LANDSCAPE = "cache_landscape";
	protected static String CACHE_PORTRAIT_ACTUAL = "cache_portrait_actual";
	protected static String CACHE_LANDSCAPE_ACTUAL = "cache_landscape_actual";
	
	//private static long DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	//private static int DISK_CACHE_FILE_COUNT = 5;
	private static SimpleCacheManager DISK_CACHE = null;
	private static Object DISK_CACHE_LOCK = new Object();
	
	public static String getPortraitActualCacheName() {
		String dateStr = Common.getCurrentDateString();
		return "PA_" + dateStr;
	}
	
	public static String getLandscapeActualCacheName() {
		String dateStr = Common.getCurrentDateString();
		return "LA_" + dateStr;
	}
	
	public static String getPortraitCacheName() {
		String dateStr = Common.getCurrentDateString();
		return "P_" + dateStr;
	}
	
	public static String getLandscapeCacheName() {
		String dateStr = Common.getCurrentDateString();
		return "L_" + dateStr;
	}
	
	public static SimpleCacheManager getDiskCache(Context context) {
		if (DISK_CACHE == null) {
			DISK_CACHE = SimpleCacheManager.getInstance(context);
		}
		return DISK_CACHE;
	}
	
	public static boolean hasDiskCache(final Context context, final String cacheName) {
		boolean result = false;
		synchronized (DISK_CACHE_LOCK) {
			result = getDiskCache(context).hasFile(cacheName);
		}
		return result;
	}
	
	public static boolean putDiskCache(final Context context, final String cacheName, Bitmap bitmap) {
		synchronized (DISK_CACHE_LOCK) {
			if (bitmap != null) {
				try {
					getDiskCache(context).write(bitmap, CompressFormat.JPEG, 90, cacheName);
				} catch (CacheTransactionException e) {
					e.printStackTrace();
					return false;
				}
			} else {
				if (!getDiskCache(context).deleteFile(cacheName)) {
					return false;
				}
			}	
		}
		return true;
	}
	
	public static Bitmap popDiskCache(final Context context, final String cacheName) {
		Bitmap bitmap = null;
		synchronized (DISK_CACHE_LOCK) {
			if (getDiskCache(context).hasFile(cacheName)) {
				try {
					bitmap = getDiskCache(context).readBitmap(cacheName);
				} catch (CacheTransactionException e) {
					e.printStackTrace();
				}
			}
			
//			final File imageFile = getDiskCache(context).get(cacheName);
//			if (imageFile != null && imageFile.exists() && imageFile.canRead()) {
//				
//				Bitmap image = null;
//				boolean tryGC = true;
//		        for (int i = 0; i < GCUtils.GC_TRY_LOOP_MAX && tryGC; ++i) {
//		        	try {
//		        		/*
//		        		ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(imageFile, ParcelFileDescriptor.MODE_READ_ONLY);
//		                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//		                image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
//		                parcelFileDescriptor.close();
//		                */
//		        		image = BitmapFactory.decodeFile(imageFile.getPath());
//		                tryGC = false;
//		            } catch (OutOfMemoryError e) {
//		                tryGC = GCUtils.getInstance().tryGCOrWait(e);
//		            } catch (InflateException e) {
//		                tryGC = GCUtils.getInstance().tryGCOrWait(e);
//		            } /*catch (FileNotFoundException e) {
//		                e.printStackTrace();
//		                tryGC = false;
//		            } catch (IOException e) {
//		                e.printStackTrace();
//		                tryGC = false;
//		            }*/
//		        }
//				return image;
//			}
		}
		return bitmap;
	}
	
}
