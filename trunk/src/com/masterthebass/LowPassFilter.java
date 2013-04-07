package com.masterthebass;

// References:	http://stackoverflow.com/questions/13243399/implementing-a-low-pass-filter-in-android-application-how-to-determine-the-val
// 				http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter

// An implementation of a Low-Pass Filter. Used by the main application for the
// manipulation of the audio, as dictated by phone movement and the sensor managers.
// This filter can also be applied in the same way as any other, via
// the filter manager.
public class LowPassFilter extends IIRFilter {
	private static final long serialVersionUID = 7533216475347295857L;
	@SuppressWarnings("unused")
	private static final String LogTag = "Low-Pass Filter";
	private double[] filteredPCM;
	private double prevAlpha;
	
	// Use the default constructor
	public LowPassFilter(int ID, String name) {
		super(ID, name);
		this.setMaxCutoffFrequency(10000);
		this.setMinCutoffFrequency(3000);
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
	@SuppressWarnings("unused")
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
		
		// Initially, store the array so that we can use it
		// as a positive feedback for the filter.
		if (filteredPCM == null || filteredPCM.length != rawPCM.length) {
			filteredPCM = shortArrayToDoubleArray(rawPCM.clone());
		} else {
			int ramp = 0;
			final int rampNum = 900;
			double delta;
			double[] inputPCM = shortArrayToDoubleArray(rawPCM);
			double alpha = getAlpha(count);
			
			// Ramp up the alpha value of the filter
			// This ensures when the cutoff frequency
			// is dramatically changed, the waveform stays
			// roughly continuous and artifacts are not heard.
			if (prevAlpha == alpha) {
				ramp = rampNum+1;
				delta = 0;
			}
			else {
				delta = (alpha - prevAlpha)/rampNum;
				alpha = prevAlpha;
			}
			
			// Apply the simple low-pass filter to each sample.
			// This uses the feedback array to smooth the values,
			// this has the effect of low-passing the frequencies.
			for (int i=0;i<count;i++) {
				if (ramp <= rampNum) {
					alpha += delta;
					ramp++;
				}
				filteredPCM[i] = filteredPCM[i] + (alpha * (inputPCM[i] - filteredPCM[i]));
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
		
		// Initially, store the array so that we can use it
		// as a positive feedback for the filter.
		if (filteredPCM == null || filteredPCM.length != rawPCM.length) {
			filteredPCM = shortArrayToDoubleArray(rawPCM.clone());
		} else {
			double alpha;
			double[] inputPCM = shortArrayToDoubleArray(rawPCM);
			
			// Apply the simple low-pass filter to each sample.
			// This uses the feedback array to smooth the values,
			// this has the effect of low-passing the frequencies.
			for (int i=0;i<count;i++) {
				setCutoffFrequency (map (LFOData[i]));
				alpha = getAlpha(count);
				filteredPCM[i] = filteredPCM[i] + (alpha * (inputPCM[i] - filteredPCM[i]));
			}
			
			rawPCM = doubleArrayToShortArray(filteredPCM.clone());
		}
		
		return rawPCM;
	}
}