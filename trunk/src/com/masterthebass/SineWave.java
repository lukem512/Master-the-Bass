package com.masterthebass;

public class SineWave extends Wave {

	private double angleIncrement;
	private double storedCurrentAngle;
	private double currentAngle;
	
	private static final double twoPI = Math.PI * 2;
	
	private double[] _generateTone(int numSamples, double frequency, double volume, int sampleRate) {
		double[] buffer = new double[numSamples];
	    angleIncrement = twoPI * frequency / sampleRate;
	    
	    for (int i = 0; i < numSamples; i++) {
	    	buffer[i] = Math.sin(currentAngle) * volume;
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
		return "Sine Wave";
	}

}

