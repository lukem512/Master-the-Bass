package com.masterthebass;

import android.util.Log;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class FilterManager implements Serializable {
	private static final long serialVersionUID = 754321647758395857L;
	
	private Hashtable<Integer, Filter> FilterList;
	
	public FilterManager () {
		// instantiate filter list
		FilterList = new Hashtable<Integer, Filter>();
		
		// create an instance of every filter and add to a list
		FilterList.put(0, new LowPassFilter(0, "Low Pass Filter"));
		FilterList.put(1, new AmplitudeFilter(1, "Amplitude Filter"));
		FilterList.put(2, new NoiseFilter(2, "Noise Filter"));
		FilterList.put(3, new NoiseFilter(3, "Extra Noise Filter", 50));
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
		getFilter(ID).enable();
	}
	
	// disable a filter given its ID
	public void disableFilter (int ID){
		getFilter(ID).disable();
	}
}

