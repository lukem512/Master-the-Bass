package com.masterthebass.prototypes.synth;

public abstract class Wave {
	
	public Wave() {
		reset();
	}
	
	public abstract double[] generateTone (int numSamples, double frequency, double volume, int sampleRate);
	public abstract void reset();
	public abstract void commit();
	
}
