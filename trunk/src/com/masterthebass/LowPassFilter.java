package com.masterthebass;

// References:	http://stackoverflow.com/questions/13243399/implementing-a-low-pass-filter-in-android-application-how-to-determine-the-val
// 				http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter

public class LowPassFilter extends IIRFilter {
	private static final long serialVersionUID = 7533216475347295857L;
	private static final String LogTag = "Low-Pass Filter";
	private static final double twoPI = Math.PI * 2;
	private final static double amplitudeScalar = 4;
	double alpha;
	
	public LowPassFilter(int iD, String name) {
		super(iD, name);
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
			}
			
			rawPCM = doubleArrayToShortArray(filteredPCM.clone());
		}
		
		return rawPCM;
	}
}