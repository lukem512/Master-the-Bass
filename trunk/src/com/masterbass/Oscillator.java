package com.masterbass;

public class Oscillator {
	
	// Waveform
	private Wave wave;
	
	// Amplitude (0.0 - 1.0)
	private double depth;
	
	// Frequency (Hz)
	private double rate;
	
	protected SoundManager sm;
	protected int sampleRate;
	protected final static int defaultSampleRate = 44100;
	private boolean started;
	
	// Bounds for modulation
	protected int maxCutoff;
	protected int minCutoff;
	
	/* Constructors */
	
	public Oscillator () {
		construct (new SineWave(), 0.5f, 10f, defaultSampleRate);
	}
	
	public Oscillator (Wave wave) {
		construct (wave, 0.5f, 10f, defaultSampleRate);
	}
	
	public Oscillator (Wave wave, double depth, double rate) {
		construct (wave, depth, rate, defaultSampleRate);
	}
	
	public Oscillator (Wave wave, double depth, double rate, int sampleRate) {
		construct (wave, depth, rate, sampleRate);
	}
	
	private void construct (Wave wave, double depth, double rate, int sampleRate) {
		sm = new SoundManager();
		
		started = false;
		
		setWave (wave);
		setDepth (depth);
		setRate (rate);
		setSampleRate (sampleRate);
	}
	
	/* Getters and setters */
	
	public Wave getWave () {
		return wave;
	}
	
	public void setWave (Wave wave) {
		this.wave = wave;
		sm.setWave(wave);
	}
	
	public double getDepth () {
		return depth;
	}
	
	public void setDepth (double depth) {
		if (depth > 1.0) {
			depth = 1.0;
		} else if (depth < 0.0) {
			depth = 0.0;
		}
		
		this.depth = depth;
	}
	
	public double getRate () {
		return rate;
	}
	
	public void setRate (double rate) {
		this.rate = rate;
	}
	
	public int getSampleRate () {
		return sampleRate;
	}
	
	public void setSampleRate (int sampleRate) {
		if (sampleRate > 0) {
			this.sampleRate = sampleRate;
		}
	}
	
	public boolean isStarted () {
		return started;
	}
	
	/* Control methods */
	
	public void start () {
		started = true;
	}
	
	public void stop () {
		started = false;
	}
	
	public double[] getSample (double duration) {
		double[] sampleData = sm.generateUnscaledTone(duration, getRate(), getDepth(), getSampleRate());
		sm.commit();
		return sampleData;
	}
	
}