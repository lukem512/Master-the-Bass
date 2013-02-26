package com.masterthebass.prototypes.synth;

public class TriangleWave extends Wave {
	
	private int storedSampleNumber;
	private int sampleNumber;
	
	// TODO - this doesn't work!

	@Override
	public double[] generateTone(int numSamples, double frequency, double volume, int sampleRate) {
		double[] sampleData = new double[numSamples];
		int samplesPerPeriod = samplesPerPeriod(frequency, sampleRate);
		double negator = 2*MAX;
		
		// Restore the previous sample number!
		sampleNumber = storedSampleNumber;
		
		// Generate the samples
		for (int i = 0; i < numSamples; i++) {
			double x = (i/samplesPerPeriod);
			sampleData[i] = Math.abs(MAX - x % negator);
			sampleData[i] = sampleData[i] * volume;
			sampleNumber = (sampleNumber + 1) % samplesPerPeriod;
		}
		
		return sampleData;
	}

	@Override
	public void reset() {
		sampleNumber = 0;
		commit();
	}

	@Override
	public void commit() {
		storedSampleNumber = sampleNumber;
	}
	
	@Override
	public String toString() {
		return "Triangle Wave";
	}

}
