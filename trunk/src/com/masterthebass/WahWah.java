package com.masterthebass;

public class WahWah extends IIRFilter {
	private static final long serialVersionUID = 7533216475347295857L;
	private static final double twopi = 2 * Math.PI;
	
	public WahWah(int iD, String name) {
		super(iD, name);
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
		
		for (int i=0;i<count;i+=getCutoffFrequency()) {
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