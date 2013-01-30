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

/*	void getLPCoefficientsButterworth2Pole(int samplerate, int cutoff, double ax[], double by[])
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
	}*/
	
	public static byte[] lowPass(byte[] rawPCM, byte[] prev) {
		byte ALPHA = (byte) 0.2;
        if (rawPCM==null || prev==null) 
            throw new NullPointerException("input and prev float arrays must be non-NULL");
        if (rawPCM.length!=prev.length) 
            throw new IllegalArgumentException("input and prev must be the same length");

        for ( int i=0; i<rawPCM.length; i++ ) {
            prev[i] = (byte) (prev[i] + ALPHA * (rawPCM[i] - prev[i]));
        }
        return prev;
    }
	
	@Override
	byte[] applyFilter (byte[] rawPCM){
		   
			int count = rawPCM.length;
			byte[] prev = new byte[count];
		    rawPCM = lowPass(rawPCM, prev);
		    return rawPCM;
		    
		    
		    /**
		     * Filter the given input against the previous values and return a low-pass filtered result.
		     * 
		     * @param input float array to smooth.
		     * @param prev float array representing the previous values.
		     * @return float array smoothed with a low-pass filter.
		     */
		    
	}

}