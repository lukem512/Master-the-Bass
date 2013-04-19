package com.masterthebass;

public final class MidiNote {
	
	/* Note names */
	
	public final static float A0 = 27.5f;
	public final static float B0 = 30.868f;
	
	public final static float C1 = 32.703f;
	public final static float D1 = 36.708f;
	public final static float E1 = 41.203f;
	public final static float F1 = 43.654f;
	public final static float G1 = 48.999f;
	public final static float A1 = 55f;
	public final static float B1 = 61.735f;
	
	public final static float C2 = 65.406f;
	public final static float D2 = 73.416f;
	public final static float E2 = 82.407f;
	public final static float F2 = 87.307f;
	public final static float G2 = 97.999f;
	public final static float A2 = 110f;
	public final static float B2 = 123.47f;
	
	public final static float C3 = 130.81f;
	public final static float D3 = 146.83f;
	public final static float E3 = 164.81f;
	public final static float F3 = 174.61f;
	public final static float G3 = 196f;
	public final static float A3 = 220f;
	public final static float B3 = 246.94f;
	
	public final static float C4 = 261.63f;
	public final static float D4 = 293.67f;
	public final static float E4 = 329.63f;
	public final static float F4 = 349.23f;
	public final static float G4 = 392f;
	public final static float A4 = 440f;
	public final static float B4 = 493.88f;
	
	public final static float C5 = 523.25f;
	public final static float D5 = 587.33f;
	public final static float E5 = 659.26f;
	public final static float F5 = 698.46f;
	public final static float G5 = 783.99f;
	public final static float A5 = 880f;
	public final static float B5 = 987.77f;
	
	public final static float C6 = 1046.5f;
	public final static float D6 = 1174.7f;
	public final static float E6 = 1318.5f;
	public final static float F6 = 1396.9f;
	public final static float G6 = 1568f;
	public final static float A6 = 1760f;
	public final static float B6 = 1975.5f;
	
	public final static float C7 = 2093f;
	public final static float D7 = 2349.3f;
	public final static float E7 = 2637f;
	public final static float F7 = 2793f;
	public final static float G7 = 3136f;
	public final static float A7 = 3520f;
	public final static float B7 = 3951.1f;
	
	public final static float C8 = 4186f;
	
	/* Conversion methods */
	
	// Lots of the conversion heavily utilises: http://www.musicdsp.org/showone.php?id=125
	
	private static final String[] notes = new String[] {"C ","C#","D ","D#","E ","F ","F#","G ","G#","A ","A#","B "};
	
	private static boolean noteValid(int n) {
		if (n >= 0) {
			if (n <= 119) {
				return true;
			}
		}
		return false;
	}
	
	public static double getNoteFrequency (int n) {
		return A4 * Math.pow(2,(n-57.0)/12.0);
	}

	public static int getNoteFromFrequency (double f) {
		return (int) (Math.round(12*(Math.log(f/A4))/Math.log(2))+57);
	}
	
	public static int upOctave (int n) {
		int newNote = n + 12;

		if (noteValid(newNote)) {
			return newNote;
		} else {
			return n;
		}
	}

	public static int downOctave (int n) {
		int newNote = n - 12;

		if (noteValid(newNote)) {
			return newNote;
		} else {
			return n;
		}
	}
	
	public static int upSemiTone (int n) {
		int newNote = n + 1;

		if (noteValid(newNote)) {
			return newNote;
		} else {
			return n;
		}
	}

	public static int downSemiTone (int n) {
		int newNote = n - 1;

		if (noteValid(newNote)) {
			return newNote;
		} else {
			return n;
		}
	}
}
