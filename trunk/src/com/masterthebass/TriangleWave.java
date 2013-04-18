package com.masterthebass;

public class TriangleWave extends Wave {
	
	private int storedSampleNumber;
	private int sampleNumber;
	private boolean storedRising;
	private boolean rising;
	
	// TODO - this doesn't work!

	@Override
	public double[] generateTone(int numSamples, double frequency, double volume, int sampleRate) {
		double[] sampleData = new double[numSamples];
		int samplesPerPeriod = samplesPerPeriod(frequency, sampleRate)/2;
		
		// Negator used in transform
		double negator = 1.0;
		
		// Multiplier used to scale and apply volume
		double multiplier = 2.0 * volume;
		
		// Restore the previous sample number!
		sampleNumber = storedSampleNumber;
		
		// Restore the direction
		rising = storedRising;
		
		// Generate the samples
		for (int i = 0; i < numSamples; i++) {
			// Create triangle shape			
			sampleData[i] = ((double) sampleNumber) / samplesPerPeriod;
			
			// Scale the waveform
			sampleData[i] = sampleData[i] * multiplier;
			
			// Transform so bottom is -1
			sampleData[i] = sampleData[i] - negator;
			
			// Modify sample number
			if (rising) {
				sampleNumber++;
				if (sampleNumber == samplesPerPeriod) {
					rising = false;
				}
			} else {
				sampleNumber--;
				if (sampleNumber == 0) {
					rising = true;
				}
			}
		}
		
		return sampleData;
	}

	@Override
	public void reset() {
		sampleNumber = 0;
		rising = true;
		commit();
	}

	@Override
	public void commit() {
		storedSampleNumber = sampleNumber;
		storedRising = rising;
	}
	
	@Override
	public String toString() {
		return "Triangle Wave";
	}
}