package com.masterthebass;

public class AmplitudeFilter extends Filter {
	private static final long serialVersionUID = -4543222800830811103L;
	private double amplitude;
	private final static double defaultAmplitude = 1.0;

	public AmplitudeFilter(int ID, String name) {
		super(ID, name);
		
		// set default amplitude value
		setAmplitude (defaultAmplitude);
	}
	
	public AmplitudeFilter(int ID, String name, double amplitude) {
		super(ID, name);
		
		// set amplitude value
		setAmplitude (amplitude);
	}
	
	public void setAmplitude (double newAmp) {
		// check bounds
		if (newAmp > 1) {
			newAmp = 1;
		} else if (newAmp < 0) {
			newAmp = 0;
		}
		
		this.amplitude = newAmp;
	}
	
	public double getAmplitude () {
		return amplitude;
	}

	@Override
	public short[] applyFilter (short[] rawPCM) {
		int length = rawPCM.length;
		
		// multiply each sample by the amplitude
		for (int i = 0; i < length; i++) {
			rawPCM[i] = (short) (rawPCM[i] * amplitude);
		}
		
		return rawPCM;
	}
}
