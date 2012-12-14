package masterthebass.prototypes.generatedsoundtest;

import android.util.Log;

public class SoundManager{
	private int period;
	
	
	/* Members */
	
	private String logTag = "SoundManager";
	
	/* Constructor */
	
	public SoundManager() {
		resetPeriod();
	}
	
	/* Public static methods */

	// Generate a tone at a given frequency, for a given duration
	// Returns a byte array of the sound in 16-bit WAV PCM format
	public byte[] generateTone(double duration, double frequency, double volume, int sampleRate) {
		int numSamples = (int) Math.ceil(sampleRate * duration);
		double sample[] = new double[numSamples];
		byte generatedSnd[] = new byte[2 * numSamples];
		int i, idx;
		
		double sampleByFreq = (sampleRate/frequency);
		double twopi = (2*Math.PI);
		int volValue = (int) (32767*volume);
		
		// Sanity check volume
		if (volume < 0.0) {
			volume = 0.0;
		} else if (volume > 1.0) {
			volume = 1.0;
		}
		
		Log.d(logTag+".generateTone", "Starting with period of " + period + ".");

        // Generate the sine wave
		idx = 0;
        for (i = 0; i < numSamples; i++) {	
        	// Sine value
            sample[i] = Math.sin(twopi * ((i+period)/sampleByFreq));
            
            // Scale to max amplitude
            sample[i] = sample[i] * volValue;
            
            // Generate 16-bit sample
            final short val = (short) (sample[i]);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        
        period += i;
        
        return generatedSnd;
    }

	// Resets the period of the waveform
	public void resetPeriod() {
		period = 0;
	}
}