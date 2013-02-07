package masterthebass.prototypes.generatedsoundtest;

import android.util.Log;

public class LowPassFilter extends Filter {
	private int sampleRate;
	private int cutoffFrequency;
	
	public LowPassFilter(int iD, String name) {
		super(iD, name);
		
		// set default sample rate to 44.1KHz
		sampleRate = 44100;
		
		// set default cutoff to 5000Hz
		cutoffFrequency = 2000;
	}

	public void setSampleRate (int sampleRate) {
		if (sampleRate > 0) {
			this.sampleRate = sampleRate;
		}
	}
	
	public int getSampleRate () {
		return sampleRate;
	}
	
	public void setCutoffFrequency (int cutoffFrequency) {
		if (cutoffFrequency >= 0) {
			this.cutoffFrequency = cutoffFrequency;
		}
	}
	
	public int getCutoffFrequency () {
		return cutoffFrequency;
	}

	public void getLPCoefficientsButterworth2Pole(int samplerate, int cutoff, double ax[], double by[])
	{
	    double sqrt2 = 1.4142135623730950488;
	    double PI      = 3.1415926535897932385;
	    double QcRaw  = (2 * PI * cutoff) / samplerate; // Find cutoff frequency in [0..PI]
		Log.d("QcRaw - ", QcRaw+"\n");
	    double QcWarp = Math.tan(QcRaw); // Warp cutoff frequence
		Log.d("QxWarp - ", QcWarp+"\n");
	    double gain = 1 / (1+sqrt2/QcWarp + 2/(QcWarp*QcWarp));
		Log.d("gain - ", gain+"\n");
	    by[2] = (double) ((1 - sqrt2/QcWarp + 2/(QcWarp*QcWarp)) * gain);
	    by[1] = (double) ((2 - 2 * 2/(QcWarp*QcWarp)) * gain);
	    by[0] = 1;
	    ax[0] = (double) (1 * gain);
	    ax[1] = (double) (2 * gain);
	    ax[2] = (double) (1 * gain);
	}
	
	
	@Override
	public byte[] applyFilter (byte[] rawPCM){
		short[] xv = new short[3];
		short[] yv = new short[3];
		int count = rawPCM.length;
		double[] ax = new double [3];
		double[] by = new double[3];
		getLPCoefficientsButterworth2Pole(sampleRate, cutoffFrequency, ax, by);
		xv[0] = 0;
		xv[1] = 0;
		xv[2] = 0;
		yv[0] = 0;
		yv[1] = 0;
		yv[2] = 0;	
		for (int i=0;i<count;i++){
			xv[2] = xv[1]; xv[1] = xv[0];
			xv[0] = rawPCM[i];
		    yv[2] = yv[1]; 
		    yv[1] = yv[0];
		    yv[0] =   (short) ((ax[0] * xv[0]) + (ax[1] * xv[1]) + (ax[2] * xv[2]) - (by[1] * yv[0])- (by[2] * yv[1]));
		    rawPCM[i] =(byte) yv[0];
		 }
		 return rawPCM;
	}

}