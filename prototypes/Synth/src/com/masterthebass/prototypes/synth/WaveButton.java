package com.masterthebass.prototypes.synth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class WaveButton extends Button {
	
	private int index = 0;
	private final int defaultIndex = 0;

	private WaveType[] waves;

    private onWaveChangeListener mOnWaveChangeListener = null;

    public interface onWaveChangeListener {
        void onClick(View v);
    }
	
	public WaveButton(Context context) {
		super(context);
		construct(defaultIndex);
	}
	
	public WaveButton(Context context, WaveType wave) {
		super(context);
		construct(wave);
	}
	
	public WaveButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		construct(defaultIndex);
	}
	
	public WaveButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		construct(defaultIndex);
	}
	
	private void construct(WaveType wave) {
		setWave(wave);
		construct (index);
	}
	
	private void construct(int waveIndex) {
		index = waveIndex;
		waves = WaveType.values();
		updateButtonText();
		
		this.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				index = (index+1) % waves.length;
				
				// Invoke the onWaveChanged listener
                if(mOnWaveChangeListener != null) {
                	mOnWaveChangeListener.onClick(v);
                }
                
                // TODO - update UI!
				// This should, ideally, be a pretty picture of the wave
				updateButtonText();
			}
		});
	}
	
	private void updateButtonText() {
		setText(""+waves[index]);
	}

    public void setOnWaveChangeListener(onWaveChangeListener cl) {
    	mOnWaveChangeListener = cl;
    }
	
	public WaveType getWave() {
		return waves[index];
	}
	
	public void setWave(WaveType wave) {
		for (int i = 0; i < waves.length; i++) {
			if (waves[i] == wave) {
				index = i;
			}
		}
	}
}
