package com.masterthebass;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class FilterManager {
    Hashtable<Integer, Filter> FilterList;
    
	// Constructor
	public FilterManager () {
		// instantiate filter list
		FilterList = new Hashtable<Integer, Filter>();
		
		// create an instance of every filter and add to a list
		FilterList.put(0, new LowPassFilter(0, "LowPassFilter"));
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

