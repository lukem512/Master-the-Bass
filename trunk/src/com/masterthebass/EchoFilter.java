package com.masterthebass;

import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

// Ref: http://audacity.sourceforge.net/manual-1.2/effects_delay.html
//		http://www.interruptor.ch/scottmadigan.html

public class EchoFilter extends Filter {

	private static final long serialVersionUID = 8469215254812193872L;
	private static final double defaultEchoTime = 1;
	private static final double defaultMaxEchoTime = 10;
	private static final double defaultMinEchoTime = 0;
	private static final double defaultPercentageRange = 80;
	private static final double maxPercentageRange = 100;
	private static final double minPercentageRange = 0;
	private static final String LogTag = "EchoFilter";
	private double maxEchoTime;
	private double minEchoTime;
	private double echoTime;
	private double percentageRange;
	private LinkedBlockingQueue<Short> echoQueue;

	public EchoFilter(int ID, String name) {
		super(ID, name);		
		construct(defaultEchoTime, defaultPercentageRange);
	}
	
	public EchoFilter(int ID, String name, double time) {
		super(ID, name);		
		construct(time, defaultPercentageRange);
	}
	
	public EchoFilter(int ID, String name, double time, double range) {
		super(ID, name);		
		construct(time, range);
	}
	
	private void construct(double time, double range) {
		maxEchoTime = defaultMaxEchoTime;
		minEchoTime = defaultMinEchoTime;
		
		setEchoTime(time);
		setPercentageRange(range);
	}
	
	public void setPercentageRange(double range) {
		if (range >= minPercentageRange) {
			if (range <= maxPercentageRange) {
				percentageRange = range;
			} else {
				percentageRange = maxPercentageRange;
			}
		} else {
			percentageRange = minPercentageRange;
		}
	}
	
	public double getPercentageRange() {
		return percentageRange;
	}
	
	public void setEchoTime(double time) {
		if (time >= minEchoTime) {
			if (time <= maxEchoTime) {
				echoTime = time;
			} else {
				echoTime = maxEchoTime;
			}
		} else {
			echoTime = minEchoTime;
		}
		
		// TODO - if the buffer is now longer, this could simply set the offset pointer
		// and the data wouldn't be lost
		// - and if it was shorter the most 
		resetEchoBuffer();
	}
	
	public double getEchoTime() {
		return echoTime;
	}
	
	private void resetEchoBuffer() {		
		int size = (int) Math.ceil(echoTime * getSampleRate());
		echoQueue = new LinkedBlockingQueue<Short>(size);
		short[] silence = SoundManager.generateSilence(echoTime, getSampleRate());
		for (short s : silence) {
			echoQueue.add(s);
		}
	}
	
	private short getPercentageOfSignal(short s) {
		double normalizedPercentageRange = (percentageRange / 100);
		return (short) (s * normalizedPercentageRange);
	}
	
	@Override
	public void enable() {
		if (echoQueue == null) {
			resetEchoBuffer();
		}
	}
	
	@Override
	public void disable() {
		echoQueue.clear();
	}		
	
	@Override
	public short[] applyFilter (short[] rawPCM){		
		for (int i = 0; i < rawPCM.length; i++) {
			if (echoQueue.offer(getPercentageOfSignal(rawPCM[i]))) {
				try {
					rawPCM[i] = SoundManager.mixSamples(rawPCM[i], echoQueue.take());
				} catch (InterruptedException e) {
					Log.w (LogTag, "Interruped whilst performing echo");
					return rawPCM;
				}
			} else {
				Log.w (LogTag, "Could not add sample to echo queue");
			}
		}

		return rawPCM;
	}

}
