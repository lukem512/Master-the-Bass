package com.masterthebass.prototypes.synth;

public class SquareWave extends Wave {
	
	private int storedSampleNumber;
	private int sampleNumber;

	@Override
	public double[] generateTone(int numSamples, double frequency, double volume, int sampleRate) {
		double[] sampleData = new double[numSamples];
		int samplesPerPeriod = samplesPerPeriod(frequency, sampleRate);
		
		// Restore the previous sample number!
		sampleNumber = storedSampleNumber;
		
		// Generate the samples
		for (int i = 0; i < numSamples; i++) {
			if (sampleNumber < (samplesPerPeriod/2)) {
				sampleData[i] = MAX;
			}  else  {
				sampleData[i] = MIN;
			}
			
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
		return "Square Wave";
	}

}
