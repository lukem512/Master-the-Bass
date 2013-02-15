package com.masterthebass.prototypes.synth;

public class DepthLFO extends Oscillator {

	public DepthLFO(AudioOutputManager am) {
		super(am);
	}
	
	public DepthLFO (AudioOutputManager am, WaveType waveType) {
		super(am, waveType);
	}
	
	public DepthLFO(AudioOutputManager am, WaveType waveType, float depth, float rate) {
		super (am, waveType, depth, rate);
	}
	
	public double[] getDepthSample (float duration) {
		if (isStarted()) {
			return sm.generateUnscaledTone(duration, getRate(), getDepth(), am.getSampleRate());
		} else {
			return null;
		}
	}

}
