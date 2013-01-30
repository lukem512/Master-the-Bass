package masterthebass.prototypes.generatedsoundtest;

import android.util.Log;

public class LowPassFilter extends Filter {
	int sampleRate;
	int cutoffFrequency;
	
	public LowPassFilter(int iD, String name) {
		super(iD, name);
		
		// set default sample rate to 44.1KHz
		sampleRate = 44100;
		
		// set default cutoff to 5000Hz
		cutoffFrequency = 5000;
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

	void getLPCoefficientsButterworth2Pole(int samplerate, double cutoff, byte ax[], byte by[])
	{
	    double sqrt2 = 1.4142135623730950488;

	    double QcRaw  = (2 * Math.PI * cutoff) / samplerate; // Find cutoff frequency in [0..PI]
	    double QcWarp = Math.tan(QcRaw); // Warp cutoff frequence
	    double gain = 1 / (1+sqrt2/QcWarp + 2/(QcWarp*QcWarp));
	    by[2] = (byte) ((1 - Math.sqrt(2)/QcWarp + 2/(QcWarp*QcWarp)) * gain);
	    by[1] = (byte) ((2 - 2 * 2/(QcWarp*QcWarp)) * gain);
	    by[0] = 1;
	    ax[0] = (byte) (1 * gain);
	    ax[1] = (byte) (2 * gain);
	    ax[2] = (byte) (1 * gain);
	}
	
	
	@Override
	byte[] applyFilter (byte[] rawPCM){
		byte[] xv = new byte[3];
		byte[] yv = new byte[3];
		int count = rawPCM.length;
		byte[] ax = new byte [3];
		byte[] by = new byte[3];
		getLPCoefficientsButterworth2Pole(sampleRate, cutoffFrequency, ax, by);
		
		 for (int i=0;i<count;i++)
		  {
			 xv[2] = xv[1]; xv[1] = xv[0];
		     xv[0] = rawPCM[i];
		     yv[2] = yv[1]; yv[1] = yv[0];
		     yv[0] =   (byte) (ax[0] * xv[0] + ax[1] * xv[1] + ax[2] * xv[2] - by[1] * yv[0]- by[2] * yv[1]);

		       rawPCM[i] = yv[0];
		   }

		 return rawPCM;
	}

}