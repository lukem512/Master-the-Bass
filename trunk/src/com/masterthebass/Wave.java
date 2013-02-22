package com.masterthebass;

public abstract class Wave {
	
	protected final static double MAX = 1.0;
	protected final static double MIN = -1.0;
	protected final static double SILENT = 0;
	
	public Wave() {
		reset();
	}
	
	public abstract double[] generateTone (int numSamples, double frequency, double volume, int sampleRate);
	public abstract void reset();
	public abstract void commit();
	
	protected int samplesPerPeriod(double frequency, int sampleRate) {
		return (int) (sampleRate/frequency);
	}
	
	@Override
	public String toString() {
		return "Abstract Wave";
	}
	
}
