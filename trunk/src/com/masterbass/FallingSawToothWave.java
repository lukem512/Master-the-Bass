package com.masterbass;

public class FallingSawToothWave extends Wave {
	
	private int storedSampleNumber;
	private int sampleNumber;

	@Override
	public double[] generateTone(int numSamples, double frequency, double volume, int sampleRate) {
		double[] sampleData = new double[numSamples];
		int samplesPerPeriod = samplesPerPeriod(frequency, sampleRate);
		
		// Negator used in transform
		double negator = 1.0;
		
		// Multiplier used to scale and apply volume
		double multiplier = 2.0 * volume;
		
		// Restore the previous sample number!
		sampleNumber = storedSampleNumber;
		
		// Generate the samples
		for (int i = 0; i < numSamples; i++) {
			// Create saw-tooth shape			
			sampleData[i] = ((double) sampleNumber) / samplesPerPeriod;
			
			// Scale the waveform
			sampleData[i] = sampleData[i] * multiplier;
			
			// Transform so bottom is -1
			sampleData[i] = sampleData[i] - negator;
			
			// Decrement sample number
			if (--sampleNumber < 0) {
				sampleNumber = samplesPerPeriod;
			}
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
		return "Falling Saw-Tooth Wave";
	}
}