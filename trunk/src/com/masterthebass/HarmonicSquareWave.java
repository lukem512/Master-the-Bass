package com.masterthebass;

public class HarmonicSquareWave extends Wave {

	private double angleIncrement;
	private double storedCurrentAngle;
	private double currentAngle;
	
	private static final double twoPI = Math.PI * 2;
	
	private double[] _generateTone(int numSamples, double frequency, double volume, int sampleRate) {
		double[] buffer = new double[numSamples];
	    angleIncrement = twoPI * frequency / sampleRate;
	    
	    for (int i = 0; i < numSamples; i++) {
	    	buffer[i] = Math.sin(currentAngle) + Math.sin(3*currentAngle)/3 + Math.sin(5*currentAngle)/5 + Math.sin(7*currentAngle)/7 + Math.sin(9*currentAngle)/9;
	    	buffer[i] = buffer[i] * volume;
	        currentAngle = ((currentAngle + angleIncrement) % twoPI);  
	    }
	    
	    return buffer;  
	}

	@Override
	public double[] generateTone(int numSamples, double frequency, double volume, int sampleRate) {
		currentAngle = storedCurrentAngle;
		return _generateTone (numSamples, frequency, volume, sampleRate);
	}

	@Override
	public void reset() {
		currentAngle = 0;
		commit();
	}

	@Override
	public void commit() {
		storedCurrentAngle = currentAngle;
	}
	
	@Override
	public String toString() {
		return "Harmonic Square Wave";
	}

}
