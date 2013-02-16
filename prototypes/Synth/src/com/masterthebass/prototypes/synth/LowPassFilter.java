package com.masterthebass.prototypes.synth;

import android.util.Log;

// TODO	- clean up duplicated code in apply* functions

public class LowPassFilter extends Filter {
	private static final long serialVersionUID = 7533216475347295857L;
	private float cutoffFrequency;
	private float maxCutoffFrequency;
	private float minCutoffFrequency;
	private final static float amplitudeScalar = 5f;
	private final static float defaultCutoff = 5000f;
	private final static float defaultMaxCutoff = 5000f;
	private final static float defaultMinCutoff = 0f;
	
	public LowPassFilter(int iD, String name) {
		super(iD, name);
		
		// set default cutoff to 5000Hz
		cutoffFrequency = defaultCutoff;
		
		// set default bounds
		maxCutoffFrequency = defaultMaxCutoff;
		minCutoffFrequency = defaultMinCutoff;
	}
	
	public void setCutoffFrequency (float cutoffFrequency) {
		if (cutoffFrequency > 0) {
			this.cutoffFrequency = cutoffFrequency;
		}
	}
	
	public void setMaxCutoffFrequency (float maxCutoffFrequency) {
		if (maxCutoffFrequency > 0) {
			this.maxCutoffFrequency = maxCutoffFrequency;
		}
	}
	
	public void setMinCutoffFrequency (float minCutoffFrequency) {
		if (minCutoffFrequency > 0) {
			this.minCutoffFrequency = minCutoffFrequency;
		}
	}
	
	public float getCutoffFrequency () {
		return cutoffFrequency;
	}
	
	public float getMaxCutoffFrequency () {
		return maxCutoffFrequency;
	}
	
	public float getMinCutoffFrequency () {
		return minCutoffFrequency;
	}
	
	// maps the current oscillation value
	// to a cutoff frequency between the bounds
	private float map(double oscillation) {		
		return (float) ((oscillation * (maxCutoffFrequency - minCutoffFrequency)) + minCutoffFrequency);
	}

	private void getLPCoefficientsButterworth2Pole(int samplerate, float cutoff, double ax[], double by[]) {
	    double sqrt2 = Math.sqrt(2);
	    double PI = Math.PI;

	    double QcRaw  = (2 * PI * cutoff) / samplerate; // Find cutoff frequency in [0..PI]
	    double QcWarp = Math.tan(QcRaw); 				// Warp cutoff frequency
	    double gain = 1 / (1+sqrt2/QcWarp + 2/(QcWarp*QcWarp));

	    by[2] = (double) ((1 - sqrt2/QcWarp + 2/(QcWarp*QcWarp)) * gain);
	    by[1] = (double) ((2 - 2 * 2/(QcWarp*QcWarp)) * gain);
	    by[0] = 1;
	    ax[0] = (double) (1 * gain);
	    ax[1] = (double) (2 * gain);
	    ax[2] = (double) (1 * gain);
	}
	
	@Override
	public short[] applyFilter (short[] rawPCM) {
		short[] xv = new short[3];
		short[] yv = new short[3];
		int count = rawPCM.length;
		double[] ax = new double [3];
		double[] by = new double[3];
		
		getLPCoefficientsButterworth2Pole(getSampleRate(), cutoffFrequency, ax, by);
		
		for (int i = 0; i < 3; i++) {
			xv[i] = 0;
			yv[i] = 0;
		}
		
		for (int i=0;i<count;i++) {
			xv[2] = xv[1]; xv[1] = xv[0];
		    xv[0] = rawPCM[i];
		    yv[2] = yv[1]; 
		    yv[1] = yv[0];
		    yv[0] =   (short) ((ax[0] * xv[0]) + (ax[1] * xv[1]) + (ax[2] * xv[2]) - (by[1] * yv[0])- (by[2] * yv[1]));
		    rawPCM[i] = (short) (yv[0] * amplitudeScalar);
		}

		return rawPCM;
	}
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		short[] xv = new short[3];
		short[] yv = new short[3];
		int count = rawPCM.length;
		double[] ax = new double [3];
		double[] by = new double[3];
		double[] LFOData = LFO.getSample(getDuration(rawPCM));
		
		for (int i = 0; i < count; i++) {
			setCutoffFrequency (map (LFOData[i]));
			
			getLPCoefficientsButterworth2Pole(getSampleRate(), cutoffFrequency, ax, by);
			
			for (int j = 0; j < 3; j++) {
				xv[j] = 0;
				yv[j] = 0;
			}
			
			xv[2] = xv[1]; xv[1] = xv[0];
		    xv[0] = rawPCM[i];
		    yv[2] = yv[1]; 
		    yv[1] = yv[0];
		    yv[0] =   (short) ((ax[0] * xv[0]) + (ax[1] * xv[1]) + (ax[2] * xv[2]) - (by[1] * yv[0])- (by[2] * yv[1]));
		    
		    rawPCM[i] = (short) (yv[0]*amplitudeScalar);
		}

		return rawPCM;
	}
}