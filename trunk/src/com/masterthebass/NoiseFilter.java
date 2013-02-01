package com.masterthebass;

import java.util.Random;

public class NoiseFilter extends Filter  {
	private int range;
	
	// constructor
	public NoiseFilter(int ID, String name) {
		super(ID, name);
		
		//set default range
		range = 20;
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
}