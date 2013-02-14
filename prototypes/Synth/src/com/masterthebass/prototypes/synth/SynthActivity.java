package com.masterthebass.prototypes.synth;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class SynthActivity extends Activity {
	
	private AudioOutputManager am;
	private SoundManager sm;
	private DepthLFO depthLFO;
	private Oscillator rateLFO;
	
	private float noteDuration = 0.5f;
	
	private final String TAG = "Synth";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instantiate managers
		am = new AudioOutputManager();
		sm = new SoundManager();	
		
		// Instantiate Oscillators
		depthLFO = new DepthLFO (am);
		rateLFO = new Oscillator (am);
		
		setContentView(R.layout.synth_activity);
		
		// Set up radio group
		initialiseRadioButtons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.synth_activity, menu);
		return true;
	}
	
	private void playSound (float frequency, float duration) {
		int sampleRate = am.getSampleRate();
		int samples = (int) Math.ceil(sampleRate * duration);
        short[] sampleData = new short[samples];
        double[] depthLFOData = new double[samples];
        
        // TODO - apply the rate (frequency) LFO to the frequency
        // this might require some further modification however!!
        
        Log.i (TAG, "Generating sound");
        sampleData = sm.generateTone(duration, frequency, 1.0, sampleRate);
        
        // Mix the depth (volume) LFO
        Log.i (TAG, "Oscillating sound using Depth LFO");
        depthLFOData = depthLFO.getDepthSample(duration);
        
        if (depthLFO.isStarted()) {
	        for (int i = 0; i < sampleData.length; i++) {
	        	sampleData[i] = (short) (sampleData[i] * ((depthLFOData[i]+1)/2));
	        }
        }
        
        // Apply the filter(s) (if needed)
        // TODO!
        
        Log.i(TAG, "Playing sound");
        am.playImmediately();
        
        Log.i(TAG, "Buffering sound");
        am.buffer(sampleData);
	}
	
	// Radio button handlers

	private void initialiseRadioButtons () {
		// This will get the radiogroup
		RadioGroup rGroup = (RadioGroup)findViewById(R.id.radioGroupWaveType);
		
		// This will get the radiobutton in the radiogroup that is checked
		RadioButton checkedRadioButton = (RadioButton)rGroup.findViewById(rGroup.getCheckedRadioButtonId());
		
		// This overrides the radiogroup onCheckListener
		rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
		    public void onCheckedChanged(RadioGroup rGroup, int checkedId)
		    {
		        // This will get the radiobutton that has changed in its check state
		        RadioButton checkedRadioButton = (RadioButton)rGroup.findViewById(checkedId);
		        
		        // This puts the value (true/false) into the variable
		        boolean isChecked = checkedRadioButton.isChecked();
		        
		        // If the radiobutton that has changed in check state is now checked...
		        if (isChecked)
		        {
		        	WaveType wave;
		        	
		        	switch (checkedRadioButton.getId()) {
		        		case R.id.radiobtnSaw:
			        		wave = WaveType.SAW_TOOTH;
			        		break;
			        		
		        		case R.id.radiobtnSquare:
			        		wave = WaveType.SQUARE;
			        		break;
			        		
		        		case R.id.radiobtnHarmonicSquare:
			        		wave = WaveType.HARMONIC_SQUARE;
			        		break;
		        		
		        		case R.id.radiobtnSine:
		        		default:
		        			wave = WaveType.SINE;
		        			break;
		        	}
		        	
		        	Log.i(TAG, "Setting wave type to " + wave);
		            sm.setWaveType(wave);
		        }
		    }
		});
	}
	
	// Button handlers
	
	public void btnAClick (View v) {
		// Play A4
		Log.i(TAG, "Playing A4");
		playSound (MidiNote.A4, noteDuration);
		Log.i(TAG, "Done!");
	}
	
	public void btnBClick (View v) {
		// Play B4
		Log.i(TAG, "Playing B4");
		playSound (MidiNote.B4, noteDuration);
		Log.i(TAG, "Done!");
	}
	
	public void btnCClick (View v) {
		// Play C4
		Log.i(TAG, "Playing C4");
		playSound (MidiNote.C4, noteDuration);
		Log.i(TAG, "Done!");
	}
	
	public void btnDClick (View v) {
		// Play D4
		Log.i(TAG, "Playing D4");
		playSound (MidiNote.D4, noteDuration);
		Log.i(TAG, "Done!");
	}
	
	public void btnEClick (View v) {
		// Play E4
		Log.i(TAG, "Playing E4");
		playSound (MidiNote.E4, noteDuration);
		Log.i(TAG, "Done!");
	}
	
	public void btnFClick (View v) {
		// Play F4
		Log.i(TAG, "Playing F4");
		playSound (MidiNote.F4, noteDuration);
		Log.i(TAG, "Done!");
	}
	
	public void btnGClick (View v) {
		// Play G4
		Log.i(TAG, "Playing G4");
		playSound (MidiNote.G4, noteDuration);
		Log.i(TAG, "Done!");
	}
	
	public void toggleBtnLPFClick (View v) {
		ToggleButton btn = (ToggleButton) findViewById(R.id.toggleBtnLPF);
		
		// TODO
		
		if (btn.isChecked()) {
			// Enable LPF!
		} else {
			// Disable LPF!
		}
	}
	
	public void toggleBtnDepthLFOClick (View v) {
		ToggleButton btn = (ToggleButton) findViewById(R.id.toggleBtnDepthLFO);
		
		if (btn.isChecked()) {
			// Enable Depth LFO!
			depthLFO.start();
		} else {
			// Disable Depth LFO!
			depthLFO.stop();
		}
	}
	
	public void toggleBtnRateLFOClick (View v) {
		ToggleButton btn = (ToggleButton) findViewById(R.id.toggleBtnRateLFO);
		
		if (btn.isChecked()) {
			// Enable Rate LFO!
			rateLFO.start();
		} else {
			// Disable Rate LFO!
			rateLFO.stop();
		}
	}

}
