package com.masterthebass.prototypes.synth;

public class Oscillator {
	
	// Waveform
	private Wave wave;
	
	// Amplitude (0.0 - 1.0)
	private double depth;
	
	// Frequency (Hz)
	private double rate;
	
	protected SoundManager sm;
	protected AudioOutputManager am;
	private boolean started;
	
	// Bounds for modulation
	protected int maxCutoff;
	protected int minCutoff;
	
	/* Constructors */
	
	public Oscillator (AudioOutputManager am) {
		construct (new SineWave(), 0.5f, 10f, am);
	}
	
	public Oscillator (AudioOutputManager am, Wave wave) {
		construct (wave, 0.5f, 10f, am);
	}
	
	public Oscillator (AudioOutputManager am, Wave wave, double depth, double rate) {
		construct (wave, depth, rate, am);
	}
	
	private void construct (Wave wave, double depth, double rate, AudioOutputManager am) {
		this.am = am;
		sm = new SoundManager();
		
		started = false;
		
		setWave (wave);
		setDepth (depth);
		setRate (rate);
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
		double[] sampleData = sm.generateUnscaledTone(duration, getRate(), getDepth(), am.getSampleRate());
		sm.commit();
		return sampleData;
	}
	
}
