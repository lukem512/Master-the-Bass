package com.masterthebass;

import java.util.Random;

public class NoiseFilter extends Filter  {
	private static final long serialVersionUID = 3476866265760242267L;
	private int range;
	
	// constructor
	public NoiseFilter(int ID, String name) {
		super(ID, name);
		
		// set default range
		setRange (20);
	}
	
	public NoiseFilter(int ID, String name, int range) {
		super(ID, name);
		
		// set the range to specified value
		setRange (range);
	}
	
	// sets the amount of noise, this is +/- range/2 either side of the wave
	public void setRange(int range){
		if (range >= 0) {
			this.range = range;
		}
	}
	
	// returns the current range of the noise
	public int getRange(){
		return range;
	}
	
	// maps a floating point value to a range between
	// 0 and Short.MAX_VALUE
	private int map(double d) {
		return (int) (d*Short.MAX_VALUE);
	}
	
	// filter an individual sample
	// removed to share common code
	private short processSample (short s, Random generator) {
		int randomValue = (generator.nextInt(range) - (range/2));
		int newValue = s + randomValue;
		
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