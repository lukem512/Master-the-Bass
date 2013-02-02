package com.masterthebass;

public class AmplitudeFilter extends Filter {
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
		// multiply each sample by the amplitude
		for (short sample : rawPCM) {
			sample = (short) (sample * amplitude);
		}
		
		return rawPCM;
	}
	
	@Override
	public byte[] applyFilter (byte[] rawPCM) {
		// TODO!
		
		return rawPCM;
	}
}
