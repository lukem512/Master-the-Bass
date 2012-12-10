package masterthebass.prototypes.generatedsoundtest;

import android.util.Log;

public class SoundManager{
	
	/* Members */
	
	private String logTag = "SoundManager";
	
	/* Constructor */
	
	public SoundManager() {
		// TODO
	}
	
	/* Public static methods */

	// Generate a tone at a given frequency, for a given duration
	// Returns a byte array of the sound in 16-bit WAV PCM format
	// TODO - remove clicks
	//		- sine not stopping at ~0
	public static byte[] generateTone(double duration, double frequency, double volume, int sampleRate) {
		int numSamples = (int) Math.ceil(sampleRate * duration);
		double sample[] = new double[numSamples];
		byte generatedSnd[] = new byte[2 * numSamples];
		
		// Sanity check volume
		if (volume < 0.0) {
			volume = 0.0;
		} else if (volume > 1.0) {
			volume = 1.0;
		}

        // Fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequency));
        }

        int i;
        int idx = 0;
        int ramp = numSamples / 20 ;                                    // Amplitude ramp as a percent of sample count
        
        ramp=0;
        //Log.d("genTone", "Ramp is " + ramp + " samples.");
        
        // Ramp amplitude (volume) up to target volume
        for (i = 0; i < ramp; i++) {
            double dVal = sample[i];
            final short val = (short) ((dVal * 32767 * (i/ramp) * volume));
            
            // in 16 bit WAV PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        
        //Log.d("genTone", "Ramped up!");

        while (i < (numSamples - ramp)) {
            double dVal = sample[i];
            final short val = (short) ((dVal * 32767 * volume));
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            
            i++;
        }
        
        //Log.d("genTone", "Generated tone body.");

        // Ramp amplitude (volume) down to 0
        while (i  <numSamples) {
            double dVal = sample[i];
            final short val = (short) ((dVal * 32767 * ((numSamples-i)/ramp) * volume));
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            
            i++;
        }
        
        //Log.d("genTone", "Ramped down!");
        
        return generatedSnd;
    }
}
