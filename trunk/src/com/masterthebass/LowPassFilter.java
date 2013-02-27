package com.masterthebass;

import android.util.Log;

//http://stackoverflow.com/questions/13243399/implementing-a-low-pass-filter-in-android-application-how-to-determine-the-val

public class LowPassFilter extends Filter {
	private static final long serialVersionUID = 7533216475347295857L;
	private static final String LogTag = "Low-Pass Filter";
	private static final double twoPI = Math.PI * 2;
	private double cutoffFrequency;
	private double maxCutoffFrequency;
	private double minCutoffFrequency;
	private final static double amplitudeScalar = 4;
	private final static double defaultCutoff = 5000;
	private final static double defaultMaxCutoff = 5000;
	private final static double defaultMinCutoff = 0;
	
	private double[] a, b;
	double[] inLeft, outLeft;
	double alpha;
	
	double[] filteredPCM;
	
	public LowPassFilter(int iD, String name) {
		super(iD, name);
		
		// set default cutoff to 5000Hz
		setCutoffFrequency(defaultCutoff);
		
		// set default bounds
		maxCutoffFrequency = defaultMaxCutoff;
		minCutoffFrequency = defaultMinCutoff;
		
		// initialise arrays
		/*int memSize = (a.length >= b.length) ? a.length : b.length;
	    inLeft = new double[memSize];
	    outLeft = new double[memSize];*/
	}
	
	public void setCutoffFrequency (double newCutoff) {
		if (newCutoff > 0) {
			this.cutoffFrequency = newCutoff;
			//getCoeffs();
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
	
	private double getAlpha(int sampleLength) {
		double T;
		double tau;
	    double alpha;
	    
	    T = sampleLength/((double)getSampleRate());
	    tau = 1/(twoPI*cutoffFrequency);
	    alpha = 1/(T*tau);
	    
	    Log.i (LogTag, "alpha is " + alpha);
	    return alpha;
	}
	
	private double[] shortArrayToDoubleArray(short[] shortArray) {
		double[] doubleArray = new double[shortArray.length];
		int i = 0;
		
		for (short s : shortArray) {
			doubleArray[i++] = ((s / ((double) Short.MAX_VALUE)) + 1.0 ) / 2.0;
			//Log.i (LogTag, s+"->"+doubleArray[i-1]);
		}
		
		return doubleArray;
	}
	
	private short[] doubleArrayToShortArray(double[] doubleArray) {
		short[] shortArray = new short[doubleArray.length];
		int i = 0;
		
		for (double d : doubleArray) {			
			shortArray[i++] = (short) (((2 * d) - 1) * Short.MAX_VALUE); // losing volume because of this? TODO
			
			//Log.i (LogTag, d+"->"+shortArray[i-1]);
		}
		
		return shortArray;
	}
	
	@Override
	public short[] applyFilter (short[] rawPCM) {
		int count = rawPCM.length;
		
		if (filteredPCM == null || filteredPCM.length != rawPCM.length) {
			filteredPCM = shortArrayToDoubleArray(rawPCM.clone());
		} else {
			double sample;
			double[] inputPCM = shortArrayToDoubleArray(rawPCM);
			double alpha = 0.15;//getAlpha(count);
			
			for (int i=0;i<count;i++) {
				sample = filteredPCM[i] + (alpha * (inputPCM[i] - filteredPCM[i]));
				filteredPCM[i] = sample;
				//Log.i (LogTag, "filteredPCM["+i+"] is " + filteredPCM[i]);
			}
			
			rawPCM = doubleArrayToShortArray(filteredPCM.clone());
		}
		
		return rawPCM;
	}
}