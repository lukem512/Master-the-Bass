package com.masterbass;

public class IIRFilter extends Filter {

	private static final long serialVersionUID = 4637928974814745087L;
	
	private final static double defaultCutoff = 50;
	private final static double defaultMaxCutoff = 100;
	private final static double defaultMinCutoff = 0;
	
	protected static final double twoPI = Math.PI * 2;

	@SuppressWarnings("unused")
	private static final String LogTag = "Infinite Response Filter";
	
	private double cutoffFrequency;
	private double maxCutoffFrequency;
	private double minCutoffFrequency;

	public IIRFilter(int ID, String name) {
		super(ID, name);
		
		// set defaults
		setMaxCutoffFrequency(defaultMaxCutoff);
		setMinCutoffFrequency(defaultMinCutoff);
		setCutoffFrequency(defaultCutoff);
	}
	
	public void setCutoffFrequency (double newCutoff) {
		if (newCutoff >= minCutoffFrequency) {
			if (newCutoff <= maxCutoffFrequency) {
				this.cutoffFrequency = newCutoff;
			} else {
				this.cutoffFrequency = maxCutoffFrequency;
			}
		} else {
			this.cutoffFrequency = minCutoffFrequency;
		}
	}
	
	public void setMaxCutoffFrequency (double maxCutoffFrequency) {
		if (maxCutoffFrequency >= 0) {
			this.maxCutoffFrequency = maxCutoffFrequency;
			setCutoffFrequency (cutoffFrequency);
		}
	}
	
	public void setMinCutoffFrequency (double minCutoffFrequency) {
		if (minCutoffFrequency >= 0) {
			this.minCutoffFrequency = minCutoffFrequency;
			setCutoffFrequency (cutoffFrequency);
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
	
	// Maps the current oscillation value
	// to a cutoff frequency between the bounds
	protected float map(double oscillation) {		
		return map(Math.abs(oscillation), getMinCutoffFrequency(), getMaxCutoffFrequency());
	}
	
	// convert an array of shorts to an array of doubles
	// where the double values have a value between 0.0 and 1.0
	protected double[] shortArrayToDoubleArray(short[] shortArray) {
		double[] doubleArray = new double[shortArray.length];
		int i = 0;
		
		for (short s : shortArray) {
			doubleArray[i++] = shortToDouble(s);
		}
		
		return doubleArray;
	}
	
	// convert a short value to a double value with a value
	// between 0.0 and 1.0
	protected double shortToDouble(short s) {
		return ((s / ((double) Short.MAX_VALUE)) + 1.0 ) / 2.0;
	}
	
	// convert a double array with values between 0.0 and 1.0
	// to an array of short
	protected short[] doubleArrayToShortArray(double[] doubleArray) {
		short[] shortArray = new short[doubleArray.length];
		int i = 0;
		
		for (double d : doubleArray) {			
			shortArray[i++] = doubleToShort(d); // losing volume because of this? TODO
		}
		
		return shortArray;
	}

	// convert a double with a value between 0.0 and 1.0
	// to a short
	protected short doubleToShort(double d) {
		return (short) (((2 * d) - 1) * Short.MAX_VALUE);
	}
	
}
