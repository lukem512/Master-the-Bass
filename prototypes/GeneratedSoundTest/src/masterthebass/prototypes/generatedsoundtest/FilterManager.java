package masterthebass.prototypes.generatedsoundtest;

public class FilterManager {
    Filter [] FilterArray;
	// Ctor
	public FilterManager () {
		FilterArray = new Filter[1];
		FilterArray[0] = new LowPassFilter(0, "LowPassFilter");
	//	FilterArray[1] = new LowPassFilter2(1, "LowPassFilter2");
		// TODO: create an instance of every filter and add to a list
	}
	
	// TODO: implement - return array of filter IDs
	Filter[] getFiltersList () {
		return FilterArray;	
	}
	
	Filter getFilter(int ID){
		return FilterArray[ID];
	}
	
	// TODO: implement - return the name of a filter given its ID
	String getFilterName (int ID) {
		return this.FilterArray[ID].getName();
	}
	
	// TODO: implement - toggle on/off of a filter given its ID
	void toggleFilter (int ID) {
		this.FilterArray[ID].toggle();
		
	}
	
	// TODO: implement
	void enableFilter (int ID) {
		this.FilterArray[ID].enable();
	}
	
	// TODO: implement
	void disableFilter (int ID){
		this.FilterArray[ID].enable();		
	}
}

