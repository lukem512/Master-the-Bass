package com.masterthebass;


import java.io.Serializable;

import android.util.Log;

public class WahWah extends Filter {
	private static final long serialVersionUID = 7533216475347295857L;
	private int sampleRate;
	private int cutoffFrequency;
	private int wahLevel;
	
	public WahWah(int iD, String name) {
		super(iD, name);
		
		// set default sample rate to 44.1KHz
		sampleRate = 44100;
		
		// set default cutoff to 5000Hz
		wahLevel = 1;
	}

	public void setSampleRate (int sampleRate) {
		if (sampleRate > 0) {
			this.sampleRate = sampleRate;
		}
	}
	
	public int getSampleRate () {
		return sampleRate;
	}
	
	public void setWahLevel(int wahLevel) {
			this.wahLevel = wahLevel;
	}
	
	public int getWahLevel () {
		return wahLevel;
	}

	void getLPCoefficientsButterworth2Pole(int samplerate, int cutoff, double ax[], double by[]) {
		   float freq = 1.5f;
		   float depth = 0.7f;
		   float res = 2.5f;
		   float freqofs = 0.3f;
		   // EffectWahwah::NewTrackSimpleMono()
		   double lfoskip = (freq * 2 * Math.PI / (double) sampleRate);
		   int skipcount = 0;
		   // EffectWahwah::ProcessSimpleMono()
		   double frequency, omega, sn, cs, alpha;
		   frequency = (1 + Math.cos(skipcount * lfoskip)) / 2;
	         frequency = frequency * depth * (1 - freqofs) + freqofs;
	         frequency = Math.exp((frequency - 1) * 6);
	         omega = Math.PI * frequency;
	         sn = Math.sin(omega);
	         cs = Math.cos(omega);
	         alpha = sn / (2 * res);
	         ax[0] = (1 - cs) / 2;
	         ax[1] = 1 - cs;
	         ax[2] = (1 - cs) / 2;
	         by[0] = 1 + alpha;
	         by[1] = -2 * cs;
	         by[2] = 1 - alpha;

	}
	

	
	@Override
	public short[] applyFilter (short[] rawPCM) {
		short[] xv = new short[3];
		short[] yv = new short[3];
		int count = rawPCM.length;
		double[] ax = new double [3];
		double[] by = new double[3];
		
		getLPCoefficientsButterworth2Pole(sampleRate, cutoffFrequency, ax, by);
		
		for (int i = 0; i < 3; i++) {
			xv[i] = 0;
			yv[i] = 0;
		}
		
		for (int i=0;i<count;i+=wahLevel) {
			xv[2] = xv[1]; xv[1] = xv[0];
		    xv[0] = rawPCM[i];
		    yv[2] = yv[1]; 
		    yv[1] = yv[0];
		    yv[0] =   (short) ((ax[0] * xv[0]) + (ax[1] * xv[1]) + (ax[2] * xv[2]) - (by[1] * yv[0])- (by[2] * yv[1]));
		    rawPCM[i] = yv[0];
		}

		return rawPCM;
	}
}