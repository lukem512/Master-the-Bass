package com.masterbass;

// References:	https://en.wikipedia.org/wiki/High-pass_filter

// An implementation of a High-Pass Filter.
public class HighPassFilter extends IIRFilter {
	private static final long serialVersionUID = 6062462755632521195L;
	@SuppressWarnings("unused")
	private static final String LogTag = "High-Pass Filter";
	private double prevAlpha;
	private boolean lastSampleSet;
	private double lastSample;
	
	// Use the default constructor
	public HighPassFilter(int ID, String name) {
		super(ID, name);
	}
	
	// This function returns a value between 0 and 1
	// smaller means more smoothing
	private double getAlpha(int sampleLength) {
	    return cutoffFrequencyToAlpha(getCutoffFrequency(), getSampleRate());
	}
	
	// Returns the -3dB cutoff frequency in Hz
	@SuppressWarnings("unused")
	private static double alphaToCutoffFrequency(double alpha, int sampleRate) {
		return -1 * ((sampleRate/twoPI)*Math.log(1-alpha));
	}
	
	// Returns the alpha value for a given cutoff frequency
	private static double cutoffFrequencyToAlpha(double cutoffFrequency, int sampleRate) {
		return 1 - Math.exp((sampleRate/twoPI)/(-1 * cutoffFrequency));
	}
	
	@Override
	public short[] applyFilter (short[] rawPCM) {
		int count = rawPCM.length;
		int ramp = 0;
		final int rampNum = 900; // TODO - this shouldn't be a magic number!
		double delta;
		double[] inputPCM = shortArrayToDoubleArray(rawPCM);
		double[] filteredPCM = inputPCM.clone();
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
		
		// If possible, set the first sample to be
		// the previous sample from the last data.
		// If a stream of data is the input, as multiple
		// samples, this should ensure continuous filtering.
		if (lastSampleSet) {
			filteredPCM[0] = lastSample;
		} else {
			lastSampleSet = true;
		}
		
		// Apply the simple high-pass filter to each sample.
		for (int i=1;i<count-1;i++) {
			if (ramp <= rampNum) {
				alpha += delta;
				ramp++;
			}
			filteredPCM[i] = alpha * (filteredPCM[i-1] + inputPCM[i] - inputPCM[i-1]);
		}
		
		// Set the last sample variable
		lastSample = filteredPCM[count-1];
		
		rawPCM = doubleArrayToShortArray(filteredPCM);
		prevAlpha = alpha;
		
		return rawPCM;
	}
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		int count = rawPCM.length;
		double[] LFOData = LFO.getSample(getDuration(rawPCM));
		double alpha;
		double[] inputPCM = shortArrayToDoubleArray(rawPCM);
		double[] filteredPCM = inputPCM.clone();
		
		// Apply the simple high-pass filter to each sample.
		for (int i=0;i<count-1;i++) {
			setCutoffFrequency (map (LFOData[i]));
			alpha = getAlpha(count);
			filteredPCM[i] = alpha * (filteredPCM[i] + inputPCM[i+1] - inputPCM[i]);
		}
		
		rawPCM = doubleArrayToShortArray(filteredPCM);
		
		return rawPCM;
	}
}