package com.s16.staticwallpaper.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.InflateException;

public class SimpleCacheManager {
	
	protected static final boolean DEBUG = true;
	protected static final String TAG = SimpleCacheManager.class.getSimpleName();
	protected static String WRITE_EXCEPTION_ALERT = "Failed the write to cache";
	protected static String READ_EXCEPTION_ALERT = "Failed the read from cache";
	
	public class CacheTransactionException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public CacheTransactionException(){ }
		
		public CacheTransactionException(String alert) {
			super(alert);
		}
	}
	
	private static SimpleCacheManager mInstance;
	private Context mContext;
	private String mCacheDir;
	
	public static SimpleCacheManager getInstance(Context applicationContext) {
		if(mInstance == null) {
			mInstance = new SimpleCacheManager(applicationContext);
		}
		return mInstance;
	}
	
	private SimpleCacheManager(Context applicationContext) {
		mContext = applicationContext;
		mCacheDir = mContext.getCacheDir().getPath() + "/";
		if (DEBUG) {
			Log.d(TAG, "[CacheManager]: Initializing new instance. CacheDir=" + mCacheDir);
		}
	}
	
	protected Context getApplicationContext() {
		return mContext;
	}
	
	//=======================================
	//========== String Read/Write ==========
	//=======================================
	
	/**
	 * Writes a string to the given file name.  The file will be placed
	 * in the current application's cache directory.
	 * 
	 * @param toWrite The String to write to a file.
	 * @param fileName The File name that will be written to.  
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void write(String toWrite, String fileName) throws CacheTransactionException {
		File file = new File(mCacheDir, fileName);
		
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file), 1024);
			out.write(toWrite);
			if (DEBUG) {
				Log.d(TAG, "[CacheManager]: Writing to " + mCacheDir + fileName);
			}
		} catch (IOException e) {
			if (DEBUG) {
				Log.d(TAG, "[CacheManager]: Unsuccessful write to " + mCacheDir + fileName);
			}
			e.printStackTrace();
			throw new CacheTransactionException(WRITE_EXCEPTION_ALERT);
		} finally {
			if(out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Reads a string from an existing file in the cache directory 
	 * and returns it.
	 * 
	 * @param fileName The file name of an existing file in the 
	 * cache directory to be read.
	 * @return Returns whatever is read.  Null if read fails.
	 * @throws CacheTransactionException Throws the exception if reading failed.  
	 * Will not throw an exception in the result of a successful read.
	 */
	public String readString(String fileName) throws CacheTransactionException {
		String readString = "";
		File file = new File(mCacheDir, fileName);
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			
			String currentLine;
			while ((currentLine = in.readLine()) != null) {
				readString += currentLine;
			}
			if (DEBUG) {
				Log.d(TAG, "[CacheManager]: Reading from " + mCacheDir + fileName);
			}
			return readString;
		} catch(IOException e){
			if (DEBUG) {
				Log.d(TAG, "[CacheManager]: Unsuccessful read from " + mCacheDir + fileName);
			}
			e.printStackTrace();
			throw new CacheTransactionException(READ_EXCEPTION_ALERT);
		} finally{
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//=======================================
	//========= Bitmap Read/Write ===========
	//=======================================
	
	/**
	 * Writes a Bitmap to the given file name.  The file will be placed
	 * in the current application's cache directory.
	 * 
	 * @param bitmap The Bitmap to be written to cache.
	 * @param format The format that the Bitmap will be written to cache. 
	 * 	(Either CompressFormat.PNG, CompressFormat.JPEG, or CompressFormat.WEBP) 
	 * @param quality The quality that the Bitmap will be written at.  0 is the lowest quality, 100
	 *  is the highest quality.  If you are writing as .PNG format, this parameter will not matter 
	 *  as PNG is lossless.
	 * @param fileName The File name that will be written to.
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void write(Bitmap bitmap, CompressFormat format, int quality, String fileName) throws CacheTransactionException {     
		File file = new File(mCacheDir, fileName);
		FileOutputStream out = null;
	    try {      
	        out = new FileOutputStream(file); 
	        bitmap.compress(format, quality, out);
	    } catch (Exception e) {
	    	if (DEBUG) {
	    		Log.d(TAG, "[CacheManager]: Unsuccessful write to " + mCacheDir + fileName);
	    	}
	    	e.printStackTrace();
			throw new CacheTransactionException(WRITE_EXCEPTION_ALERT);
	    } finally {
	    	if (out != null) {
	    		try {
	    			out.flush();
	    			out.close();
	    		} catch(IOException e){
	    			e.printStackTrace();
	    		}
	    	}
	    }
	}
	
	/**
	 * Reads a bitmap from the specified file and returns the bitmap.
	 * 
	 * @param fileName The File name that will be read from.
	 * @return Returns the bitmap in the case of a successful read.
	 * @throws CacheTransactionException CacheTransactionException Throws the exception if reading failed.  
	 * Will not throw an exception in the result of a successful read.
	 */
	public Bitmap readBitmap(String fileName) throws CacheTransactionException {
		File file = new File(mCacheDir, fileName);
		Bitmap bitmap = null;
		boolean tryGC = true;
        for (int i = 0; i < GCUtils.GC_TRY_LOOP_MAX && tryGC; ++i) {
        	try {
        		bitmap = BitmapFactory.decodeFile(file.getPath());
        		tryGC = false;
            } catch (OutOfMemoryError e) {
                tryGC = GCUtils.getInstance().tryGCOrWait(e);
            } catch (InflateException e) {
                tryGC = GCUtils.getInstance().tryGCOrWait(e);
            }
        }
		if(bitmap != null) {
			return bitmap;
		} else { // BitmapFactory.decodeFile returns null if it can't decode a bitmap.
			throw new CacheTransactionException(READ_EXCEPTION_ALERT); 
		}
	}
	
	//=======================================
	//========== Binary Read/Write ==========
	//=======================================
	
	/**
	 * Writes an array of bytes to the given file name.
	 * The file will be placed in the current application's cache directory.
	 * 
	 * @param toWrite The byte array to write to a file.
	 * @param fileName The File name that will be written to.
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void write(byte[] toWrite, String fileName) throws CacheTransactionException {
		File file = new File(mCacheDir, fileName);
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(toWrite);
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "[CacheManager]: Unsuccessful write to " + mCacheDir + fileName);
			}
			e.printStackTrace();
			throw new CacheTransactionException(WRITE_EXCEPTION_ALERT);
		} finally {
			if(out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Reads an array of bytes from an existing file in the cache directory 
	 * and returns it.
	 * 
	 * @param fileName The file name of an existing file in the 
	 * cache directory to be read.
	 * @return The byte array that was read
	 * @throws CacheTransactionException Throws the exception if reading failed.  
	 * Will not throw an exception in the result of a successful read.
	 */
	public byte[] readBinaryFile(String fileName) throws CacheTransactionException {
		RandomAccessFile raFile = null;
		try {
			File file = new File(mCacheDir, fileName);
			raFile = new RandomAccessFile(file, "r");
			byte[] byteArray = new byte[(int)raFile.length()];
			raFile.read(byteArray);
			return byteArray;
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "[CacheManager]: Unsuccessful read from " + mCacheDir + fileName);
			}
			e.printStackTrace();
			throw new CacheTransactionException(READ_EXCEPTION_ALERT);
		} finally {
			if(raFile != null) {
				try {
					raFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//=======================================
	//========= Bundle Read/Write ===========
	//=======================================
	
	/**
	 * Writes a Bundle to cache as a readable binary data to cache.
	 * 
	 * @param obj The Bundle to write.
	 * @param fileName The File name that will be written to.
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void write(Bundle obj, String fileName) throws CacheTransactionException {
		Parcel parcelData = Parcel.obtain();
		parcelData.writeBundle(obj);
		byte[] data = parcelData.marshall();
		write(data, fileName);
	}
	
	
	/**
	 * Reads a Bundle from a file.  Initially runs readBinaryFile(), so
	 * there may be logs saying there was a successful read, but the log will be followed
	 * up by another log stating that it was unable to create a Bundle from the binary data.
	 * 
	 * @param fileName The file name that will be read from.
	 * @return The Bundle the file was storing, in the result of a successful read.
	 * @throws CacheTransactionException Throws the exception if reading failed, or the
	 * creation of the Bundle fails.
	 */
	public Bundle readBundle(String fileName) throws CacheTransactionException {
		byte[] data = readBinaryFile(fileName);
		Parcel parcelData = Parcel.obtain();
		parcelData.unmarshall(data, 0, data.length);
		return parcelData.readBundle();
	}
	
	//===========================================
	//========== FileSystem Management ==========
	//===========================================

	/**
	 * Deletes a file in the cache directory.
	 * @param fileName The file to delete.
	 */
	public boolean deleteFile(String fileName) {
		if (DEBUG) {
			Log.d(TAG, "[CacheManager]: Deleting the file " + mCacheDir + fileName);
		}
		File toDelete = new File(mCacheDir, fileName);
		return toDelete.delete();
	}
	
	/**
	 * Check a file has in the cache directory.
	 * @param fileName The file to check.
	 */
	public boolean hasFile(String fileName) {
		if (DEBUG) {
			Log.d(TAG, "[CacheManager]: Check the file exists " + mCacheDir + fileName);
		}
		File file = new File(mCacheDir, fileName);
		return file.exists() && file.canRead();
	}
}
