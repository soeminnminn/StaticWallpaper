package com.s16.widget.cropper;

public interface AsyncTaskResultListener<T> {
	/***
	 * Callback listener when AsyncTask complete. 
	 * 
	 * @param result Executed result
	 */
	public void onTaskComplete(T result);
}
