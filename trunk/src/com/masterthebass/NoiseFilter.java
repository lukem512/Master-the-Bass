package com.masterthebass;

import java.util.Random;

public class NoiseFilter extends Filter  {
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
	public byte[] applyFilter (byte[] rawPCM){
	    Random generator = new Random();
		int count = rawPCM.length;
		
		for (int i=0; i<count; i++)
		{
			int randomIndex = (generator.nextInt(range) - (range/2));
		    rawPCM[i] =(byte)(rawPCM[i] + randomIndex);
		}

		return rawPCM;
	}
	
	@Override
	public short[] applyFilter (short[] rawPCM){
	    Random generator = new Random();
		int count = rawPCM.length;
		int newValue;
		
		for (int i=0; i<count; i++)
		{
			int randomIndex = (generator.nextInt(range) - (range/2));
			newValue = rawPCM[i] + randomIndex;
			if (newValue < Short.MAX_VALUE) {
				if (newValue > Short.MIN_VALUE) {
					rawPCM[i] = (short) (rawPCM[i] + randomIndex);
				} else {
					rawPCM[i] = Short.MIN_VALUE;
				}
			} else {
				rawPCM[i] = Short.MAX_VALUE;
			}
		}

		return rawPCM;
	}
}