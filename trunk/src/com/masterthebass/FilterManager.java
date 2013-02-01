									package com.masterthebass;

import android.util.Log;

public class FilterManager {
	// Ctor
	public static final String TAG = "com.masterthebass";
	public FilterManager () {
		// TODO: create an instance of every filter and add to a list
	}
	
	// TODO: implement - return array of filter IDs
	static int[] getFiltersList () {
		int[] IDs = {-1};
		return IDs;	
	}
	
	// TODO: implement - return the name of a filter given its ID
	static String getFilterName (int ID) {
		return "Filter Name";
	}
	
	// TODO: implement - toggle on/off of a filter given its ID
	void toggleFilter (int ID) {
	}
	
	// TODO: implement
	static void enableFilter (int ID) {

		Log.e(TAG,"the filter has been enabled");
	}
	
	// TODO: implement
	static void disableFilter (int ID) {
		Log.e(TAG,"the filter has been disabled");
	}
}
