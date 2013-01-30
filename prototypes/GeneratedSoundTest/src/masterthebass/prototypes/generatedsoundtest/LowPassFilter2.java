package masterthebass.prototypes.generatedsoundtest;

import android.util.Log;

public class LowPassFilter2 extends Filter {
	int sampleRate;
	int cutoffFrequency;
	
	public LowPassFilter2(int iD, String name) {
		super(iD, name);
		
		// set default sample rate to 44.1KHz
		sampleRate = 44100;
		
		// set default cutoff to 5000Hz
		cutoffFrequency = 1000;
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
		if (cutoffFrequency > 0) {
			this.cutoffFrequency = cutoffFrequency;
		}
	}
	
	public int getCutoffFrequency () {
		return cutoffFrequency;
	}

	void getLPCoefficientsButterworth2Pole(int samplerate, int cutoff, double ax[], double by[])
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
	    by[0] = gain;
	    ax[0] = (double) (1 * gain);
	    ax[1] = (double) (2 * gain);
	    ax[2] = (double) (1 * gain);
	}
	
	
	@Override
	byte[] applyFilter (byte[] rawPCM){
		short[] xv = new short[5];
		short[] yv = new short[5];
		int count = rawPCM.length;
		double[] ax = new double [3];
		double[] by = new double[3];
		byte newbyte;
		getLPCoefficientsButterworth2Pole(sampleRate, cutoffFrequency, ax, by);
		
		 for (int i=0;i<count;i++)
		  {
			 
	   }

	 return rawPCM;
	}

}