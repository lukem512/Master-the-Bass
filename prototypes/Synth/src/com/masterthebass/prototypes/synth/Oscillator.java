package com.masterthebass.prototypes.synth;

public class Oscillator {
	
	// Waveform
	private WaveType waveType;
	
	// Amplitude (0.0 - 1.0)
	private float depth;
	
	// Frequency (Hz)
	private float rate;
	
	protected SoundManager sm;
	protected AudioOutputManager am;
	private boolean started;
	
	/* Constructors */
	
	public Oscillator (AudioOutputManager am) {
		construct (WaveType.SINE, 0.5f, 10f, am);
	}
	
	public Oscillator (AudioOutputManager am, WaveType waveType) {
		construct (waveType, 0.5f, 10f, am);
	}
	
	public Oscillator (AudioOutputManager am, WaveType waveType, float depth, float rate) {
		construct (waveType, depth, rate, am);
	}
	
	private void construct (WaveType waveType, float depth, float rate, AudioOutputManager am) {
		setWaveType (waveType);
		setDepth (depth);
		setRate (rate);
		
		started = false;
		
		this.am = am;
		sm = new SoundManager();
	}
	
	/* Getters and setters */
	
	public WaveType getWaveType () {
		return waveType;
	}
	
	public void setWaveType (WaveType waveType) {
		this.waveType = waveType;
	}
	
	public float getDepth () {
		return depth;
	}
	
	public void setDepth (float depth) {
		if (depth > 1.0) {
			depth = 1.0f;
		} else if (depth < 0.0) {
			depth = 0.0f;
		}
		
		this.depth = depth;
	}
	
	public float getRate () {
		return rate;
	}
	
	public void setRate (float rate) {
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
	
	public short[] getSample (float duration) {
		if (started) {
			return sm.generateTone(duration, rate, depth, am.getSampleRate());
		} else {
			return sm.generateSilence(duration, am.getSampleRate());
		}
	}
	
}
