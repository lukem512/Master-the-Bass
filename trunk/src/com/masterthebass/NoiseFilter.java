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
	
	@Override
	public short[] applyFilter (short[] rawPCM){
	    Random generator = new Random();
		int count = rawPCM.length;
		int newValue;
		int randomValue;
		
		for (int i=0; i<count; i++)
		{
			randomValue = (generator.nextInt(range) - (range/2));
			newValue = rawPCM[i] + randomValue;
			if (newValue < Short.MAX_VALUE) {
				if (newValue > Short.MIN_VALUE) {
					rawPCM[i] = (short) (rawPCM[i] + randomValue);
				} else {
					rawPCM[i] = Short.MIN_VALUE;
				}
			} else {
				rawPCM[i] = Short.MAX_VALUE;
			}
		}

		return rawPCM;
	}
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		// TODO - oscillate the amount of noise using the LFO
		return applyFilter(rawPCM);
	}
}