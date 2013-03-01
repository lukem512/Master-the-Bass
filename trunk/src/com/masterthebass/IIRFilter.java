package com.masterthebass;

public class IIRFilter extends Filter {

	private static final long serialVersionUID = 4637928974814745087L;
	
	protected final static double defaultCutoff = 5000;
	protected final static double defaultMaxCutoff = 5000;
	protected final static double defaultMinCutoff = 0;
	
	protected double cutoffFrequency;
	protected double maxCutoffFrequency;
	protected double minCutoffFrequency;
	
	protected double[] filteredPCM;

	public IIRFilter(int ID, String name) {
		super(ID, name);
	}
	
	public void setCutoffFrequency (double newCutoff) {
		if (newCutoff > 0) {
			this.cutoffFrequency = newCutoff;
		}
	}
	
	public void setMaxCutoffFrequency (float maxCutoffFrequency) {
		if (maxCutoffFrequency > 0) {
			this.maxCutoffFrequency = maxCutoffFrequency;
		}
	}
	
	public void setMinCutoffFrequency (float minCutoffFrequency) {
		if (minCutoffFrequency > 0) {
			this.minCutoffFrequency = minCutoffFrequency;
		}
	}
	
	public double getCutoffFrequency () {
		return cutoffFrequency;
	}
	
	public double getMaxCutoffFrequency () {
		return maxCutoffFrequency;
	}
	
	public double getMinCutoffFrequency () {
		return minCutoffFrequency;
	}
	
	protected double[] shortArrayToDoubleArray(short[] shortArray) {
		double[] doubleArray = new double[shortArray.length];
		int i = 0;
		
		for (short s : shortArray) {
			doubleArray[i++] = ((s / ((double) Short.MAX_VALUE)) + 1.0 ) / 2.0;
		}
		
		return doubleArray;
	}
	
	protected short[] doubleArrayToShortArray(double[] doubleArray) {
		short[] shortArray = new short[doubleArray.length];
		int i = 0;
		
		for (double d : doubleArray) {			
			shortArray[i++] = (short) (((2 * d) - 1) * Short.MAX_VALUE); // losing volume because of this? TODO
		}
		
		return shortArray;
	}

}
