package com.masterthebass.prototypes.synth;

public class Oscillator {
	
	// Waveform
	private WaveType waveType;
	
	// Amplitude (0.0 - 1.0)
	private float depth;
	
	// Frequency (Hz)
	private float rate;
	
	private SoundManager sm;
	private AudioOutputManager am;
	private boolean started;
	
	/* Constructors */
	
	public Oscillator (AudioOutputManager am) {
		construct (WaveType.SQUARE, 0.2f, 10f, am);
	}
	
	public Oscillator (AudioOutputManager am, WaveType waveType) {
		construct (waveType, 0.2f, 10f, am);
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
		this.depth = depth;
	}
	
	public float getRate () {
		return rate;
	}
	
	public void setRate (float rate) {
		if (rate > 1.0) {
			rate = 1.0f;
		} else if (rate < 0.0) {
			rate = 0.0f;
		}
		
		this.rate = rate;
	}
	
	public boolean isStarted () {
		return started;
	}
	
	/* Control methods */
	
	public void start () {
		started = true;
		
		// TODO - spawn a thread that will create a waveform
		sm.generateToneShort(0.01, rate, depth, am.getSampleRate());
	}
	
	public void stop () {
		started = false;
	}
	
}
