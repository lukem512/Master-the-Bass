package com.masterthebass.prototypes.synth;

public class SoundManager{
	/* Members */

	@SuppressWarnings("unused")
	private final String LogTag = "SoundManager";
	private WaveType waveType = WaveType.SINE;
	private double Final;

	/* Constructor */

	public SoundManager() {
		resetOffset ();
	}

	/* Destructor */

	public void destruct() {
		// Do nothing
	}

	/* Private methods */

	private double[] vmGenerateUnscaledTone(int numSamples, double frequency, double volume, int sampleRate, double offset, WaveType wave) {
		double sample;
		double generatedSnd[] = new double[numSamples];
		int sampleNumber = 0;
		int samplesPerPeriod = (int) (sampleRate*(1.0/frequency));

		double sampleByFreq = (sampleRate/frequency);
		double twopi = (2*Math.PI);
		
		for (int i = 0; i < numSamples; i++) {  
    		double x = ((i + offset)/sampleByFreq);
    		double twopix = twopi * x;
    		
            switch (wave) {
            		// Sine wave
	            	default:
	            	case SINE:
	            		sample = Math.sin(twopix);
	            		break;
	       
	            	// Square wave
	            	case SQUARE:
	            		if (sampleNumber < (samplesPerPeriod/2)) {
	            			sample = 1.0;
	            		}  else  {
	            			sample = -1.0;
	            		}
	            		break;
	            		
            		// Square wave with harmonics
		            // i.e. constructed from sine waves using FT
	            	case HARMONIC_SQUARE:
	            		sample = Math.sin(twopix) + Math.sin(3*twopix)/3 + Math.sin(5*twopix)/5 + Math.sin(7*twopix)/7 + Math.sin(9*twopix)/9;
	            		break;
	               
	            	// Saw-tooth wave
	            	case SAW_TOOTH:
		            	sample = 1.0 * (x - Math.floor(x + 0.5));
		              	break;
		              	
		            // Straight Triangle wave
	            	case TRIANGLE:
	            		sample = Math.abs(1.0 - x % (2*1.0));
	            		break;
	            		
	            	// Concave Triangle wave
	            	case CONC_TRIANGLE:
	            		sample = Math.pow(Math.abs(sampleNumber - 1.0), 2.0);
	            		break;
	            		
	            	// Convex Triangle wave
	            	case CONV_TRIANGLE:
	            		sample = Math.pow(Math.abs(sampleNumber - 1.0), 0.5);
	            		break;
            }
            
            // Increment sample number
            // this is modulo the number of samples per period
            sampleNumber = (sampleNumber + 1) % samplesPerPeriod;
            
            // Apply volume scalar
            generatedSnd[i] = sample * volume;
        }
        
        // Return the sound
        return generatedSnd;
	}

	// Generates a 44.1KHz, mono, signed 16-bit PCM tone at a given frequency for a given duration
	private short[] vmGenerateTone(int numSamples, double frequency, double volume, int sampleRate, double offset, WaveType wave) {
		double unscaledSnd[] = new double[numSamples];
		short generatedSnd[] = new short[numSamples];
		
		unscaledSnd = vmGenerateUnscaledTone(numSamples, frequency, volume, sampleRate, offset, wave);

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
		int numSamples = (int) Math.ceil(sampleRate * duration);
		short generatedSnd[];

		// Sanity check volume
		if (volume < 0.0) {
			volume = 0.0;
		} else if (volume > 1.0) {
			volume = 1.0;
		}

		generatedSnd = vmGenerateTone(numSamples, frequency, volume, sampleRate, Final, waveType);
        
        // Save starting offset for next tone
        Final = (numSamples + Final) % (sampleRate/frequency);
        
        return generatedSnd;
    }
	
	// Generate a tone at a given frequency, for a given duration
	// Returns a double array of samples between -1.0 and 1.0
	public double[] generateUnscaledTone(double duration, double frequency, double volume, int sampleRate) {		
		int numSamples = (int) Math.ceil(sampleRate * duration);
		double generatedSnd[];

		// Sanity check volume
		if (volume < 0.0) {
			volume = 0.0;
		} else if (volume > 1.0) {
			volume = 1.0;
		}

		generatedSnd = vmGenerateUnscaledTone(numSamples, frequency, volume, sampleRate, Final, waveType);
        
        // Save starting offset for next tone
        Final = (numSamples + Final) % (sampleRate/frequency);
        
        return generatedSnd;
    }
	
	// 'Generates' silence in 16-bit signed PCM for given duration at given SR
	public short[] generateSilence(double duration, int sampleRate) {
		int numSamples = (int) Math.ceil(sampleRate * duration);
		short generatedSnd[] = new short[numSamples];
		
		for (int i = 0; i < numSamples; i++) {
			generatedSnd[i] = 0;
		}
		
		return generatedSnd;
	}
	
	// 'Generates' silence in double format for given duration at given SR
	public double[] generateUnscaledSilence(double duration, int sampleRate) {
		int numSamples = (int) Math.ceil(sampleRate * duration);
		double generatedSnd[] = new double[numSamples];
		
		for (int i = 0; i < numSamples; i++) {
			generatedSnd[i] = 0;
		}
		
		return generatedSnd;
	}
	
	// Mixes two signals
	// This is as easy as summing each sample and clipping
	public short[] mixTones(short[] a, short[] b) {
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
	
	public void setWaveType (WaveType waveType) {
		this.waveType = waveType;
	}
	
	public WaveType getWaveType () {
		return waveType;
	}

	// Resets the offset of the waveform
	private void resetOffset() {
		Final = 0;
	}
}