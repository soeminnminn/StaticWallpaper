package com.s16.staticwallpaper.drawing;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class BitmapUtils {
	
	static final String TAG = BitmapUtils.class.getSimpleName();
	
	public BitmapUtils() {
		
	}
	
	public static int dpToPx(Context context, int dp) {
	    float density = context.getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
	
	public static Point calculateAspectRatio(int srcWidth, int srcHeight, int destWidth, int destHeight) {
		Point result = new Point();
		if (srcWidth <= 0 || srcHeight <= 0 || destWidth <= 0 || destHeight <= 0) return result;
		
		float targetRatio = (float)destWidth / (float)destHeight;
		float sourceRatio = (float)srcWidth / (float)srcHeight;
		if(sourceRatio >= targetRatio){ // source is wider than target in proportion
			result.x = destWidth;
			result.y = Math.round((float)destWidth / sourceRatio);
		}else{ // source is higher than target in proportion
			result.y = destHeight;
			result.x = Math.round((float)destHeight * sourceRatio);
		} 
		return result;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	  // Raw height and width of image
	  final int height = options.outHeight;
	  final int width = options.outWidth;
	  int inSampleSize = 1;

	  if (height > reqHeight || width > reqWidth) {

		  // Calculate ratios of height and width to requested height and width
		  final int heightRatio = Math.round((float) height / (float) reqHeight);
		  final int widthRatio = Math.round((float) width / (float) reqWidth);

		  // Choose the smallest ratio as inSampleSize value, this will guarantee
		  // a final image with both dimensions larger than or equal to the requested height and width.
		  inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	  }

	  return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);
	    
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static Bitmap decodeSampledBitmapFromFile(String imagePath, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(imagePath, options);
	    
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(imagePath, options);
	}
	
	public static Bitmap getBitmapFromUri(Context context, Uri uri) 
			throws IOException {
	    ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
	    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
	    Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
	    parcelFileDescriptor.close();
	    return image;
	}
	
	public static Bitmap getBitmapFromFile(File file) 
			throws IOException {
	    ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
	    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
	    Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
	    parcelFileDescriptor.close();
	    return image;
	}
}
