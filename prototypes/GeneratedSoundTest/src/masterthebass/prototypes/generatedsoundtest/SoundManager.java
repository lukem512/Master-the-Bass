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
        
        Log.d("genTone", "Ramp is " + ramp + " samples.");
        
        for (i = 0; i< ramp; ++i) {                                     // Ramp amplitude up (to avoid clicks)
            double dVal = sample[i];
                                                                        // Ramp up to maximum
            final short val = (short) ((dVal * 32767 * (i/ramp) * volume));
                                                                        // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        
        Log.d("genTone", "Ramped up!");

        for (i = i; i< numSamples - ramp; ++i) {                        // Max amplitude for most of the samples
            double dVal = sample[i];
                                                                        // scale to maximum amplitude
            final short val = (short) ((dVal * 32767 * volume));
                                                                        // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        
        Log.d("genTone", "Generated tone body.");

        for (i = i; i< numSamples; ++i) {                               // Ramp amplitude down
            double dVal = sample[i];
                                                                        // Ramp down to zero
            final short val = (short) ((dVal * 32767 * ((numSamples-i)/ramp) * volume));
                                                                        // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        
        Log.d("genTone", "Ramped down!");
        
        return generatedSnd;
    }
}
