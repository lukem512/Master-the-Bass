package com.masterthebass.prototypes.synth;

// TODO	- clean up duplicated code in apply* functions

public class LowPassFilter extends Filter {
	private static final long serialVersionUID = 7533216475347295857L;
	private float cutoffFrequency;
	private float maxCutoffFrequency;
	private float minCutoffFrequency;
	private final static float amplitudeScalar = 5f;
	private final static float defaultCutoff = 5000f;
	private final static float defaultMaxCutoff = 5000f;
	private final static float defaultMinCutoff = 0f;
	double alpha;
	double[] filteredPCM;
	
	public LowPassFilter(int iD, String name) {
		super(iD, name);
		
		// set default cutoff to 5000Hz
		cutoffFrequency = defaultCutoff;
		
		// set default bounds
		maxCutoffFrequency = defaultMaxCutoff;
		minCutoffFrequency = defaultMinCutoff;
	}
	
	public void setCutoffFrequency (float cutoffFrequency) {
		if (cutoffFrequency > 0) {
			this.cutoffFrequency = cutoffFrequency;
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
	
	public float getCutoffFrequency () {
		return cutoffFrequency;
	}
	
	public float getMaxCutoffFrequency () {
		return maxCutoffFrequency;
	}
	
	public float getMinCutoffFrequency () {
		return minCutoffFrequency;
	}
	
	// maps the current oscillation value
	// to a cutoff frequency between the bounds
	private float map(double oscillation) {		
		return (float) ((oscillation * (maxCutoffFrequency - minCutoffFrequency)) + minCutoffFrequency);
	}
	
	// TODO - this function should return a value between 0 and 1
	// smaller means more smoothing
	private double getAlpha(int sampleLength) {
		double T;
		double tau;
	    double alpha;
	    
	    //tau = RC; // time constant for decay in seconds
	    //fc = 1/(twoPI*tau); // cutoff frequency
	    
	    // TODO - hack
	    alpha = cutoffFrequency/(maxCutoffFrequency - minCutoffFrequency);

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
			double alpha = getAlpha(count);
			
			for (int i=0;i<count;i++) {
				sample = filteredPCM[i] + (alpha * (inputPCM[i] - filteredPCM[i]));
				filteredPCM[i] = sample;
				//Log.i (LogTag, "filteredPCM["+i+"] is " + filteredPCM[i]);
			}
			
			rawPCM = doubleArrayToShortArray(filteredPCM.clone());
		}
		
		return rawPCM;
	}
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		int count = rawPCM.length;
		double[] LFOData = LFO.getSample(getDuration(rawPCM));
		
		if (filteredPCM == null || filteredPCM.length != rawPCM.length) {
			filteredPCM = shortArrayToDoubleArray(rawPCM.clone());
		} else {
			double sample;
			double alpha;
			double[] inputPCM = shortArrayToDoubleArray(rawPCM);
			
			for (int i=0;i<count;i++) {
				setCutoffFrequency (map (LFOData[i]));
				alpha = getAlpha(count);
				sample = filteredPCM[i] + (alpha * (inputPCM[i] - filteredPCM[i]));
				filteredPCM[i] = sample;
			}
			
			rawPCM = doubleArrayToShortArray(filteredPCM.clone());
		}
		
		return rawPCM;
	}
}