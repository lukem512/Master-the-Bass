package com.masterthebass;

//https://code.google.com/p/audacity/source/browse/audacity-src/trunk/src/effects/Wahwah.cpp?r=11204

public class WahWahFilter extends IIRFilter {
	private static final long serialVersionUID = 7533216475347295857L;
	private static final double twopi = 2 * Math.PI;
	private static int defaultWahLevel = 1;
	private static int defaultMinWahLevel = 1;
	private static int defaultMaxWahLevel = 100;
	private int wahLevel;
	private int maxWahLevel;
	private int minWahLevel;
	private double res;
	
	public WahWahFilter(int ID, String name) {
		super(ID, name);
		setMaxWahLevel(defaultMaxWahLevel);
		setMinWahLevel(defaultMinWahLevel);
		setWahLevel(defaultWahLevel);
		setResonance(2.5);
	}
	
	public void setResonance(double res) {
		// TODO - bounds testing
		this.res = res;
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
		 double lfoskip = (freq * twopi / getSampleRate());
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
		   double omega, sn, cs, alpha;
		   double in, out;
		   
		   int lfoskipsamples = 30;
		   int skipcount = 0;

		   /*if (filteredPCM == null || filteredPCM.length != rawPCM.length) {
				filteredPCM = shortArrayToDoubleArray(rawPCM.clone());
		   } else {
			   for (int i = 0; i < rawPCM.length; i++) {
			      in = rawPCM[i];
			      
			      if ((skipcount++) % lfoskipsamples == 0) {
			         omega = Math.PI * getCutoffFrequency();
			         sn = Math.sin(omega);
			         cs = Math.cos(omega);
			         alpha = sn / (2 * res);
			         b0 = (1 - cs) / 2;
			         b1 = 1 - cs;
			         b2 = (1 - cs) / 2;
			         a0 = 1 + alpha;
			         a1 = -2 * cs;
			         a2 = 1 - alpha;
			      };
			      out = (b0 * in + b1 * xn1 + b2 * xn2 - a1 * yn1 - a2 * yn2) / a0;
			      xn2 = xn1;
			      xn1 = in;
			      yn2 = yn1;
			      yn1 = out;
			   }
			   
			   rawPCM = doubleArrayToShortArray(filteredPCM.clone());
		   }*/
		   
		   return rawPCM;
	}
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		int count = rawPCM.length;
		double[] LFOData = LFO.getSample(getDuration(rawPCM));
		double[] filteredPCM = shortArrayToDoubleArray(rawPCM);
		
		// Apply the simple band-pass filter to each sample.
		for (int i=0;i<count;i++) {
			setCutoffFrequency (map (LFOData[i]));
			// TODO - band-pass filtering
		}
		
		rawPCM = doubleArrayToShortArray(filteredPCM);
		
		return rawPCM;
	}
}