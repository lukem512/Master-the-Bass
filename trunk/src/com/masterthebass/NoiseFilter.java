package com.masterthebass;

import java.util.Random;

public class NoiseFilter extends Filter  {
	private static final long serialVersionUID = 3476866265760242267L;
	private static final int defaultMinRange = 1;
	private static final int defaultMaxRange = Short.MAX_VALUE;
	private int range, minRange, maxRange;
	
	@SuppressWarnings("unused")
	private static final String LogTag = "NoiseFilter";
	
	// constructor
	public NoiseFilter(int ID, String name) {
		super(ID, name);
		
		// set default range
		setRange (20);
	}
	
	public NoiseFilter(int ID, String name, int range) {
		super(ID, name);
		
		// set the bounds
		setMinRange(defaultMinRange);
		setMinRange(defaultMaxRange);
		
		// set the range to specified value
		setRange (range);
	}
	
	public NoiseFilter(int ID, String name, int range, int min, int max) {
		super(ID, name);
		
		// set the bounds to specified values
		setMinRange(min);
		setMinRange(max);
		
		// set the range to specified value
		setRange (range);
	}
	
	// sets the amount of noise, this is +/- range/2 either side of the wave
	public void setRange(int range){
		if (range >= 0) {
			this.range = range;
		}
	}
	
	// sets the minimum range to map to (for oscillating filter)
	public void setMinRange(int min){
		if (min >= 0) {
			minRange = min;
		}
	}
	
	// sets the maximum range to map to (for oscillating filter)
	public void setMaxRange(int max){
		if (range >= 0) {
			maxRange = max;
		}
	}

	// returns the current range of the noise
	public int getRange(){
		return range;
	}

	// returns the current minimum range of the noise
	public int getMinRange(){
		return minRange;
	}
	
	// returns the current maximum range of the noise
	public int getMaxRange(){
		return maxRange;
	}
	
	// maps a floating point value to a range
	private int map(double oscillation) {
		return (int) ((oscillation * (getMaxRange() - getMinRange())) + getMinRange());
	}
	
	// filter an individual sample
	// removed to share common code
	private short processSample (short s, Random generator) {
		// return if no noise is required
		if (getRange() == 0) {
			return s;
		}
		
		// otherwise, create the random amount
		int randomValue = generator.nextInt(getRange()) - (getRange()/2);
		int newValue = s + randomValue;
		
		// check type bounds
		if (newValue < Short.MAX_VALUE) {
			if (newValue > Short.MIN_VALUE) {
				return (short) newValue;
			} else {
				return Short.MIN_VALUE;
			}
		} else {
			return Short.MAX_VALUE;
		}
	}
	
	@Override
	public short[] applyFilter (short[] rawPCM){
	    Random generator = new Random();
		int count = rawPCM.length;
		
		for (int i=0; i<count; i++) {
			processSample(rawPCM[i], generator);
		}

		return rawPCM;
	}
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		double[] LFOData = LFO.getSample(getDuration(rawPCM));
		int count = rawPCM.length;
		Random generator = new Random();
		
		for (int i=0;i<count;i++) {
			setRange (map (LFOData[i]));
			processSample(rawPCM[i], generator);
		}

		return rawPCM;
	}
}