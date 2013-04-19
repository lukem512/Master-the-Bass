package com.masterthebass;

public class OverdriveFilter extends Filter {
	private static final long serialVersionUID = -4543222800830811103L;
	private int gain, maxGain, minGain;
	private final static int defaultGain = 3;
	private final static int defaultMinGain = 0;
	private final static int defaultMaxGain = 5;

	public OverdriveFilter(int ID, String name) {
		super(ID, name);
		
		// set default gain values
		setGain (defaultGain);
		setMaxGain (defaultMaxGain);
		setMaxGain (defaultMinGain);
	}
	
	public OverdriveFilter(int ID, String name, double amplitude) {
		super(ID, name);
		
		// set default gain value
		setGain (defaultGain);
	}
	
	public void setGain (int newGain) {		
		this.gain = newGain;
	}
	
	public int getGain () {
		return gain;
	}
	
	public void setMaxGain (int maxGain) {		
		this.maxGain = maxGain;
	}
	
	public int getMaxGain () {
		return maxGain;
	}
	
	public void setMinGain (int minGain) {		
		this.minGain = minGain;
	}
	
	public int getMinGain () {
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
		
		for (int i = 0; i < count; i++) {
			setGain((int) map(LFOData[i], getMinGain(), getMaxGain()));
			processSample(rawPCM[i]);
		}
		
		return rawPCM;
	}
}
