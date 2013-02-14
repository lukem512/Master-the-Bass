package com.masterthebass.prototypes.synth;

public class DepthLFO extends Oscillator {

	public DepthLFO(AudioOutputManager am) {
		super(am);
	}
	
	public double[] getDepthSample (float duration) {
		if (isStarted()) {
			return sm.generateUnscaledTone(duration, getRate(), getDepth(), am.getSampleRate());
		} else {
			return null;
		}
	}

}
