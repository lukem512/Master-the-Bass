package masterthebass.prototypes.generatedsoundtest;

import android.util.Log;

public class SoundManager{
	private double Final;
	
	
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
		byte generatedSnd[];

		// Sanity check volume
		if (volume < 0.0) {
			volume = 0.0;
		} else if (volume > 1.0) {
			volume = 1.0;
		}

		generatedSnd = doGenerateTone(numSamples, frequency, volume, sampleRate, Final);
        
        // Save starting offset for next tone
        Final = (numSamples + Final) % (sampleRate/frequency);
        
        return generatedSnd;
    }
	
	private byte[] doGenerateTone (int numSamples, double frequency, double volume, int sampleRate, double offset) {
		double sample = 0.0;
		byte generatedSnd[] = new byte[2 * numSamples];
		int i, idx;

		double sampleByFreq = (sampleRate/frequency);
		double twopi = (2*Math.PI);
		int volValue = (int) (32767*volume);

        // Generate the tone
		idx = 0;
        for (i = 0; i < numSamples; i++) {      	
        	// Sine value
            sample = Math.sin(twopi * ((i + offset)/sampleByFreq));
            
            // Scale to max amplitude
            sample = sample * volValue;
            
            // Generate 16-bit samples
            final short val = (short) sample;
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        
        // Return the sound
        return generatedSnd;
	}
	

	// Resets the period of the waveform
	public void resetPeriod() {
		Final = 0;
	}
}