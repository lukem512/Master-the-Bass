package com.masterthebass;

public class WahWahFilter extends IIRFilter {
	private static final long serialVersionUID = 7533216475347295857L;
	private static final double twopi = 2 * Math.PI;
	private static int defaultWahLevel = 1;
	private static int defaultMinWahLevel = 1;
	private static int defaultMaxWahLevel = 100;
	private int wahLevel;
	private int maxWahLevel;
	private int minWahLevel;
	
	public WahWahFilter(int iD, String name) {
		super(iD, name);
		setMaxWahLevel(defaultMaxWahLevel);
		setMinWahLevel(defaultMinWahLevel);
		setWahLevel(defaultWahLevel);
	}

	public void setWahLevel(int wahLevel) {
		if (wahLevel >= getMinWahLevel()) {
			if (wahLevel <= getMaxWahLevel()) {
				this.wahLevel = wahLevel;
			} else {
				this.wahLevel = getMaxWahLevel();
			}
		} else {
			this.wahLevel = getMinWahLevel();
		}
	}

	public int getWahLevel () {
		return wahLevel;
	}
	
	public void setMaxWahLevel(int maxWahLevel) {
		this.maxWahLevel = maxWahLevel;
	}
	
	public int getMaxWahLevel () {
		return maxWahLevel;
	}
	
	public void setMinWahLevel(int minWahLevel) {
		this.minWahLevel = minWahLevel;
	}
	
	public int getMinWahLevel () {
		return minWahLevel;
	}

	void getLPCoefficientsButterworth2Pole(int samplerate, double cutoff, double ax[], double by[]) {
		 double freq = 1.5f;
		 double depth = 0.7f;
		 double res = 2.5f;
		 double freqofs = 0.3f;
		 double lfoskip = (freq * twopi / (double) getSampleRate());
		 int skipcount = 0;
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
		double[] xv = new double[3];
		double[] yv = new double[3];
		int count = rawPCM.length;
		double[] ax = new double [3];
		double[] by = new double[3];
		
		getLPCoefficientsButterworth2Pole(getSampleRate(), getCutoffFrequency(), ax, by);
		
		for (int i = 0; i < 3; i++) {
			xv[i] = 0;
			yv[i] = 0;
		}
		
		for (int i=0;i<count;i+=getWahLevel()) {
			xv[2] = xv[1]; xv[1] = xv[0];
		    xv[0] = shortToDouble(rawPCM[i]);
		    yv[2] = yv[1]; 
		    yv[1] = yv[0];
		    yv[0] =   (ax[0] * xv[0]) + (ax[1] * xv[1]) + (ax[2] * xv[2]) - (by[1] * yv[0])- (by[2] * yv[1]);
		    rawPCM[i] = doubleToShort(yv[0]);
		}

		return rawPCM;
	}
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		double[] xv = new double[3];
		double[] yv = new double[3];
		int count = rawPCM.length;
		double[] ax = new double [3];
		double[] by = new double[3];
		
		// TODO - set wahlevel using LFO
		
		getLPCoefficientsButterworth2Pole(getSampleRate(), getCutoffFrequency(), ax, by);
		
		for (int i = 0; i < 3; i++) {
			xv[i] = 0;
			yv[i] = 0;
		}
		
		for (int i=0;i<count;i+=getWahLevel()) {
			xv[2] = xv[1]; xv[1] = xv[0];
		    xv[0] = shortToDouble(rawPCM[i]);
		    yv[2] = yv[1]; 
		    yv[1] = yv[0];
		    yv[0] =   (ax[0] * xv[0]) + (ax[1] * xv[1]) + (ax[2] * xv[2]) - (by[1] * yv[0])- (by[2] * yv[1]);
		    rawPCM[i] = doubleToShort(yv[0]);
		}

		return rawPCM;
	}
}