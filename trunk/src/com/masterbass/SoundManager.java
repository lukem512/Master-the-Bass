package com.masterbass;

public class SoundManager{
	
	/* Members */
	
	private Wave wave;

	@SuppressWarnings("unused")
	private final static String LogTag = "SoundManager";
		
	public final static short SILENCE = 0;


	/* Constructor */

	public SoundManager() {
		wave = new SineWave();
	}
	
	public SoundManager(Wave wave) {
		this.wave = wave;
	}

	/* Destructor */

	public void destruct() {
		// Do nothing
	}

	/* Private methods */

	private double[] vmGenerateUnscaledTone(int numSamples, double frequency, double volume, int sampleRate) {
		return wave.generateTone(numSamples, frequency, volume, sampleRate);
	}

	// Generates a 44.1KHz, mono, signed 16-bit PCM tone at a given frequency for a given duration
	private short[] vmGenerateTone(int numSamples, double frequency, double volume, int sampleRate) {
		double unscaledSnd[] = new double[numSamples];
		short generatedSnd[] = new short[numSamples];
		
		unscaledSnd = vmGenerateUnscaledTone(numSamples, frequency, volume, sampleRate);

        // Scale the tone
        for (int i = 0; i < numSamples; i++) {  
            // Scale to max amplitude
        	generatedSnd[i] = (short) (unscaledSnd[i] * Short.MAX_VALUE);
        }
        
        // Return the sound
        return generatedSnd;
	}
	
	/* Public static methods */
	
	// Generate a tone at a given frequency, for a given duration
	// Returns a short array of the sound in 16-bit WAV PCM format
	public short[] generateTone(double duration, double frequency, double volume, int sampleRate) {		
		int numSamples = getSampleLength(duration, sampleRate, frequency);
		short generatedSnd[];

		// Sanity check volume
		if (volume < 0.0) {
			volume = 0.0;
		} else if (volume > 1.0) {
			volume = 1.0;
		}

		generatedSnd = vmGenerateTone(numSamples, frequency, volume, sampleRate);
        
        return generatedSnd;
    }
	
	// Generate a tone at a given frequency, for a given duration
	// Returns a double array of samples between -1.0 and 1.0
	public double[] generateUnscaledTone(double duration, double frequency, double volume, int sampleRate) {		
		int numSamples = getSampleLength(duration, sampleRate, frequency);
		double generatedSnd[];

		// Sanity check volume
		if (volume < 0.0) {
			volume = 0.0;
		} else if (volume > 1.0) {
			volume = 1.0;
		}

		generatedSnd = vmGenerateUnscaledTone(numSamples, frequency, volume, sampleRate);
        
        return generatedSnd;
    }
	
	// 'Generates' silence in 16-bit signed PCM for given duration at given SR
	public static short[] generateSilence(double duration, int sampleRate) {
		int numSamples = getSampleLength(duration, sampleRate);
		short generatedSnd[] = new short[numSamples];
		
		for (int i = 0; i < numSamples; i++) {
			generatedSnd[i] = SILENCE;
		}
		
		return generatedSnd;
	}
	
	// 'Generates' silence in double format for given duration at given SR
	public static double[] generateUnscaledSilence(double duration, int sampleRate) {
		int numSamples = getSampleLength(duration, sampleRate);
		double generatedSnd[] = new double[numSamples];
		
		for (int i = 0; i < numSamples; i++) {
			generatedSnd[i] = SILENCE;
		}
		
		return generatedSnd;
	}
	
	// Mixes two signals
	public static short[] mixTones(short[] a, short[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException ("Tones are not of same length.");
		}
		
		short[] mixed = new short[a.length];
		float max = Short.MIN_VALUE;
		
		// Find the maximum value
		for(int i = 0; i < a.length; i++) {
			if( Math.abs( a[i] + b[i] ) > max ) {
				max = Math.abs(a[i] + b[i]);
			}
		}

		// Scale to that maximum
		for (int i = 0; i < a.length; i++) {
			mixed[i] = (short) Math.round(Short.MAX_VALUE * (a[i] + b[i]) / max) ;
		}
		
		return mixed;
	}
	
	public static short mixSamples(short a, short b) {
		return (short) ((a + b) / 2);
	}
	
	public void setWave (Wave wave) {
		this.wave = wave;
	}
	
	public Wave getWave () {
		return wave;
	}
	
	public void commit () {
		wave.commit();
	}
	
	public static int getSampleLength (double duration, int sampleRate) {
		return getSampleLength(duration, sampleRate, 1);
	}
	
	public static int getSampleLength (double duration, int sampleRate, double frequency) {
		/*int period = (int)((1.0/frequency) * sampleRate);
		int sampleLength = (int) Math.ceil(duration * sampleRate);
		return sampleLength + period - (sampleLength % period);*/
		return (int) Math.ceil(sampleRate * duration);
	}
}