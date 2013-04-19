package com.masterthebass;

import android.util.Log;

public class OverdriveFilter extends Filter {
	private static final long serialVersionUID = -4543222800830811103L;
	private double gain, maxGain, minGain;
	private final static double defaultGain = 3;
	private final static double defaultMinGain = 0;
	private final static double defaultMaxGain = 5;

	public OverdriveFilter(int ID, String name) {
		super(ID, name);
		
		// set default gain values
		setGain (defaultGain);
		setMaxGain (defaultMaxGain);
		setMinGain (defaultMinGain);
	}
	
	public OverdriveFilter(int ID, String name, double gain) {
		super(ID, name);
		
		// set default gain value
		setGain (gain);
		setMaxGain (defaultMaxGain);
		setMinGain (defaultMinGain);
	}
	
	public void setGain (double newGain) {		
		this.gain = newGain;
	}
	
	public double getGain () {
		return gain;
	}
	
	public void setMaxGain (double maxGain) {		
		this.maxGain = maxGain;
	}
	
	public double getMaxGain () {
		return maxGain;
	}
	
	public void setMinGain (double minGain) {		
		this.minGain = minGain;
	}
	
	public double getMinGain () {
		return minGain;
	}
	
	public short processSample(short s) {
		// create the sample
		int newVal = (int) (s + (s*getGain()));
		
		// hard clipping
		if (newVal >= Short.MIN_VALUE) {
			if (newVal <= Short.MAX_VALUE) {
				return (short) newVal;
			} else {
				return Short.MAX_VALUE;
			}
		} else {
			return Short.MIN_VALUE;
		}
	}

	@Override
	public short[] applyFilter (short[] rawPCM) {
		int count = rawPCM.length;
		
		for (int i = 0; i < count; i++) {
			processSample(rawPCM[i]);
		}
		
		return rawPCM;
	}
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		int count = rawPCM.length;
		double[] LFOData = LFO.getSample(getDuration(rawPCM));
		double newGain;
		
		for (int i = 0; i < count; i++) {
			newGain = map(LFOData[i], getMinGain(), getMaxGain());
			setGain(newGain);
			processSample(rawPCM[i]);
		}
		
		return rawPCM;
	}
}
