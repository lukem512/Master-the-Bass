package com.masterthebass;

import android.util.Log;

// References:	http://stackoverflow.com/questions/13243399/implementing-a-low-pass-filter-in-android-application-how-to-determine-the-val
// 				http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter

public class LowPassFilter extends IIRFilter {
	private static final long serialVersionUID = 7533216475347295857L;
	private static final String LogTag = "Low-Pass Filter";
	private static final double twoPI = Math.PI * 2;
	private final static double amplitudeScalar = 4;
	private double[] filteredPCM;
	private double prevAlpha;
	
	public LowPassFilter(int ID, String name) {
		super(ID, name);
	}
	
	// Maps the current oscillation value
	// to a cutoff frequency between the bounds
	private float map(double oscillation) {		
		return (float) ((oscillation * (getMaxCutoffFrequency() - getMinCutoffFrequency())) + getMinCutoffFrequency());
	}
	
	// This function returns a value between 0 and 1
	// smaller means more smoothing
	private double getAlpha(int sampleLength) {
	    return cutoffFrequencyToAlpha(getCutoffFrequency(), getSampleRate());
	}
	
	// Returns the -3dB cutoff frequency in Hz
	private static double alphaToCutoffFrequency(double alpha, int sampleRate) {
		return -1 * (Math.log(1-alpha)*(sampleRate/twoPI));
	}
	
	// Returns the alpha value for a given cutoff frequency
	private static double cutoffFrequencyToAlpha(double cutoffFrequency, int sampleRate) {
		return 1 - Math.exp((-1 * cutoffFrequency)/(sampleRate/twoPI));
	}
	
	@Override
	public short[] applyFilter (short[] rawPCM) {
		int count = rawPCM.length;
		
		if (filteredPCM == null || filteredPCM.length != rawPCM.length) {
			filteredPCM = shortArrayToDoubleArray(rawPCM.clone());
		} else {
			int ramp = 0;
			final int rampNum = 900;
			double sample;
			double delta;
			double[] inputPCM = shortArrayToDoubleArray(rawPCM);
			double alpha = getAlpha(count);
			
			if (prevAlpha == alpha) {
				ramp = rampNum+1;
				delta = 0;
			}
			else {
				// decrease
				delta = (alpha - prevAlpha)/rampNum;
				alpha = prevAlpha;
			}
			
			for (int i=0;i<count;i++) {
				if (ramp <= rampNum) {
					alpha += delta;
					ramp++;
				}
				sample = filteredPCM[i] + (alpha * (inputPCM[i] - filteredPCM[i]));
				filteredPCM[i] = sample;
			}
			
			rawPCM = doubleArrayToShortArray(filteredPCM.clone());
			prevAlpha = alpha;
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