package com.masterthebass.prototypes.synth;

public class AmplitudeFilter extends Filter {
	private static final long serialVersionUID = -4543222800830811103L;
	private float amplitude;
	private final static float defaultAmplitude = 1.0f;

	public AmplitudeFilter(int ID, String name) {
		super(ID, name);
		
		// set default amplitude value
		setAmplitude (defaultAmplitude);
	}
	
	public AmplitudeFilter(int ID, String name, float amplitude) {
		super(ID, name);
		
		// set amplitude value
		setAmplitude (amplitude);
	}
	
	public void setAmplitude (float amplitude) {
		// check bounds
		if (amplitude > 1) {
			amplitude = 1;
		} else if (amplitude < 0) {
			amplitude = 0;
		}
		
		this.amplitude = amplitude;
	}
	
	public float getAmplitude () {
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
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {
		int length = rawPCM.length;
		double[] LFOData = LFO.getSample(getDuration(rawPCM));
		double depth = LFO.getDepth();
		
		// multiply each sample by the oscillating amplitude
		for (int i = 0; i < length; i++) {
			rawPCM[i] = (short) (rawPCM[i] * (LFOData[i]+depth)/2);
		}
		
		return rawPCM;
	}
}

