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
	private Hashtable<Integer, Oscillator> OscillatorList;
	
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
		OscillatorList = new Hashtable<Integer, Oscillator>();
		
		// create an instance of every filter and add to a list
		FilterList.put(0, new LowPassFilter(0, "Low Pass Filter"));
		FilterList.put(1, new AmplitudeFilter(1, "Oscillating Amplitude Filter", 1.0));
		FilterList.put(2, new NoiseFilter(2, "Noise Filter", 1028));
		FilterList.put(3, new NoiseFilter(3, "Extra Noise Filter", 4086));
		FilterList.put(4, new EchoFilter(4, "Echo Filter", 3));
		FilterList.put(5, new WahWahFilter(5, "Wah Wah Filter"));
		
		// add oscillators
		// TODO - this is being passed a sample rate!!!! SORT THIS OUT
		attachOscillator(1, new Oscillator(new SineWave(), 1.0, 3.0, 44100));
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
	
	// return array of filter names
	public String[] getFilterNamesList () {		
		int[] IDs = getFiltersList();
		String[] names = new String[FilterList.size()];
		int index = 0;
		
		for (int id : IDs) {
			names[index] = getFilterName(id);
			index++;
		}
		
		return names;
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
	
	// returns the oscillator object attached to a filter
	// returns null if one does not exist
	public Oscillator getOscillator (int ID) {
		return OscillatorList.get(ID);
	}
	
	// enables a new oscillator
	public void attachOscillator (int ID, Oscillator LFO) {
		OscillatorList.put(ID, LFO);
	}
	
	// disables an oscillator
	public void detachOscillator (int ID) {
		OscillatorList.remove(ID);
	}
	
	// applies a filter
	public short[] applyFilter (int ID, short[] rawPCM) {
		Oscillator LFO = getOscillator(ID);
		Filter f = getFilter(ID);
		
		if (LFO != null) {
			return f.applyFilterWithOscillator(rawPCM, LFO);
		} else {
			return f.applyFilter(rawPCM);
		}
	}
}

