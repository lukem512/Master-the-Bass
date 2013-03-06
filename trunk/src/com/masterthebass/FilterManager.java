package com.masterthebass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import android.util.Log;



public class FilterManager implements Serializable {
	public final static String TAG = "com.masterthebass.FILTERMANAGER";
	private static final long serialVersionUID = 754321647758395857L;
	
	private Hashtable<Integer, Filter> FilterList;
	
	// helper function to convert a list of Integers to their
	// primitive cousins, int
	private int[] convertIntegers(List<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    Iterator<Integer> iterator = integers.iterator();
	    for (int i = 0; i < ret.length; i++)
	    {
	        ret[i] = iterator.next().intValue();
	    }
	    return ret;
	}
	
	// constructor
	public FilterManager () {
		// instantiate filter list
		FilterList = new Hashtable<Integer, Filter>();
		
		// create an instance of every filter and add to a list
		FilterList.put(0, new LowPassFilter(0, "Low Pass Filter"));
		FilterList.put(1, new AmplitudeFilter(1, "Amplitude Filter"));
		FilterList.put(2, new NoiseFilter(2, "Noise Filter", 1028));
		FilterList.put(3, new NoiseFilter(3, "Extra Noise Filter", 4086));
		FilterList.put(4, new EchoFilter(4, "Echo Filter", 3));
		FilterList.put(5, new WahWahFilter(5, "Wah Wah Filter"));
	}
    
	// return array of filter IDs
	public int[] getFiltersList () {
		int[] IDs = new int[FilterList.size()];
		int index = 0;
		
		Set<Integer> keys = FilterList.keySet();
		Iterator<Integer> itr = keys.iterator();
		
	    while(itr.hasNext()) {
	      IDs[index] = ((Integer) itr.next());
	      index++;
	    }
		
		return IDs;	
	}
	
	// return array of enabled filter IDs
	public int[] getEnabledFiltersList () {
		ArrayList<Integer> list = new ArrayList<Integer>();
		Set<Integer> keys = FilterList.keySet();
		Iterator<Integer> itr = keys.iterator();
		int id;
		
	    while(itr.hasNext()) {
	      id = ((Integer) itr.next());
	      
	      if (getFilter(id).getState()) {
	    	  list.add(id);
	      }
	    }
		
	    return convertIntegers(list);
	}
	
	// returns a filter object given its ID
	public Filter getFilter(int ID){
		return FilterList.get(ID);
	}
	
	// return the name of a filter given its ID
	public String getFilterName (int ID) {
		return getFilter(ID).getName();
	}
	
	// toggle on/off of a filter given its ID
	public void toggleFilter (int ID) {
		getFilter(ID).toggle();
	}
	
	// enable a filter given its ID
	public void enableFilter (int ID) {
		Log.i(TAG,"filter enabled");
		getFilter(ID).enable();
	}
	
	// disable a filter given its ID
	public void disableFilter (int ID){
		Log.i(TAG,"filter disabled");
		getFilter(ID).disable();
	}
	
	// applies a filter
	public short[] applyFilter (int ID, short[] rawPCM) {
		Filter f = getFilter(ID);
		return f.applyFilter(rawPCM);
	}
}

