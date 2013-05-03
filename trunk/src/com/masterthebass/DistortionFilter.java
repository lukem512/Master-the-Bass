package com.masterthebass;

// Ref: http://stackoverflow.com/questions/5083088/java-audio-effects-distortion-algorithm-on-android
// The main processSample code is not owned by Master the Bass.

public class DistortionFilter extends Filter {
	private static final long serialVersionUID = 3158043451366784378L;
	private final static double th=1.0/3.0; 
	private final static  double multiplier = 1.0/0x7fff;

	public DistortionFilter(int ID, String name) {
		super(ID, name);
	}
	
	private short processSample (short s) {
	    double out = 0.0;
	    
		double in = multiplier*s;
        double absIn = java.lang.Math.abs(in);
        if(absIn<th){
            out=(s*2*multiplier);
        }
        else if(absIn<2*th) {
            if(in>0)out= (3-(2-in*3)*(2-in*3))/3;
            else if(in<0)out=-(3-(2-absIn*3)*(2-absIn*3))/3;
        }
        else if(absIn>=2*th) {
            if(in>0)out=1;
            else if(in<0)out=-1;
        }
        return (short)(out/multiplier);
	}

	@Override
	public short[] applyFilter (short[] rawPCM) {
		int count = rawPCM.length;

		for (int i = 0; i < count; i++) {
			rawPCM[i] = processSample(rawPCM[i]);
		}
		
		return rawPCM;
	}
	
	@Override
	public short[] applyFilterWithOscillator (short[] rawPCM, Oscillator LFO) {		
		return applyFilter(rawPCM);
	}
}
