package com.masterthebass;

import java.io.Serializable;

public class Filter implements Serializable {
	private static final long serialVersionUID = -176105425284552882L;
	
	private int ID;
	private String name;
	private boolean enabled;
	private int sampleRate;
	private static final int defaultSampleRate = 44100;
	
	public Filter(int ID, String name){
		this.name = name;
		this.ID = ID;
		
		sampleRate = defaultSampleRate;
		enabled = false;
	}
	
	protected float getDuration (short[] rawPCM) {
		return (rawPCM.length/(float)sampleRate);
	}
	
	public boolean getState (){
		return enabled;
	}
	
	public String getName (){
		return name;
	}
	
	public int getID (){
		return ID;
	}
	
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		return rawPCM;
	}
	
	public short[] applyFilter (short[] rawPCM){
		return rawPCM;
	}
	
	public void enable (){
		enabled = true;
	}
	
	public void disable (){
		enabled = false;
	}
		
	public void toggle (){
		enabled = !enabled;
	}
	
	public void setSampleRate (int sampleRate) {
		if (sampleRate > 0) {
			this.sampleRate = sampleRate;
		} else {
			throw new IllegalArgumentException ("Sample rates can only be positive integer values.");
		}
	}
	
	public int getSampleRate () {
		return sampleRate;
	}

	protected float map(double oscillation, double min, double max) {		
		return (float) ((oscillation * (max - min)) + min);
	}
}