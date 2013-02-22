package com.masterthebass;

import android.util.Log;

// TODO	- clean up duplicated code in apply* functions

public class LowPassFilter extends Filter {
	private static final long serialVersionUID = 7533216475347295857L;
	private double cutoffFrequency;
	private double maxCutoffFrequency;
	private double minCutoffFrequency;
	private final static double amplitudeScalar = 5;
	private final static double defaultCutoff = 5000;
	private final static double defaultMaxCutoff = 5000;
	private final static double defaultMinCutoff = 0;
	
	public LowPassFilter(int iD, String name) {
		super(iD, name);
		
		// set default cutoff to 5000Hz
		cutoffFrequency = defaultCutoff;
		
		// set default bounds
		maxCutoffFrequency = defaultMaxCutoff;
		minCutoffFrequency = defaultMinCutoff;
	}
	
	public void setCutoffFrequency (double newCutoff) {
		if (newCutoff > 0) {
			this.cutoffFrequency = newCutoff;
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
	
	public double getCutoffFrequency () {
		return cutoffFrequency;
	}
	
	public double getMaxCutoffFrequency () {
		return maxCutoffFrequency;
	}
	
	public double getMinCutoffFrequency () {
		return minCutoffFrequency;
	}
	
	// maps the current oscillation value
	// to a cutoff frequency between the bounds
	private float map(double oscillation) {		
		return (float) ((oscillation * (maxCutoffFrequency - minCutoffFrequency)) + minCutoffFrequency);
	}

	private void getLPCoefficientsButterworth2Pole(int samplerate, double cutoff, double ax[], double by[]) {
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
}