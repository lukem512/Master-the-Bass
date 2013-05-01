package com.masterthebass;

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

	// maps the current oscillation value
	// to a cutoff frequency between the bounds
	public float map(double oscillation) {		
		return (float) ((oscillation * (getMaxCutoffFrequency() - getMinCutoffFrequency())) + getMinCutoffFrequency());
	}

	// this function returns a value between 0 and 1
	// smaller means more smoothing
	private double getAlpha(int sampleLength) {
		double T;
		double tau;
	    double alpha;

	    //tau = RC; // time constant for decay in seconds
	    //fc = 1/(twoPI*tau); // cutoff frequency

	    // TODO - hack, this could be nicer! and more mathematically correct
	    alpha = getCutoffFrequency()/(getMaxCutoffFrequency() - getMinCutoffFrequency());

	    return alpha;
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