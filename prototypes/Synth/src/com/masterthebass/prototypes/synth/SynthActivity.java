package com.masterthebass.prototypes.synth;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

// TODO	- rate modulation
//		- maintain button results when activity is recreated
//		- drop-down box for note waveform
//		- ability to change settings for oscillators

public class SynthActivity extends Activity {
	
	private AudioOutputManager am;
	private SoundManager sm;
	private FilterManager fm;
	private Oscillator depthLFO;
	private Oscillator lowPassFilterLFO;
	
	private final int numNotes = 7;
	private float[] noteFreq = new float[numNotes];
	private boolean[] noteDown = new boolean[numNotes];
	
	private Thread generatorThread = null;
	private Thread playerThread = null;
	
	private final int NOTEA = 0;
	private final int NOTEB = 1;
	private final int NOTEC = 2;
	private final int NOTED = 3;
	private final int NOTEE = 4;
	private final int NOTEF = 5;
	private final int NOTEG = 6;
	
	private float noteDuration = 1f;
	private float volume = 0.7f;
	
	private final String TAG = "Synth";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instantiate managers
		am = new AudioOutputManager();
		sm = new SoundManager();	
		fm = new FilterManager();
		
		// Instantiate Oscillators
		lowPassFilterLFO = new Oscillator (am, WaveType.SINE, volume, 2f);
		depthLFO = new Oscillator (am, WaveType.SINE, volume/2, 2f);
		
		// Attach to filters
		fm.attachOscillator(0, lowPassFilterLFO);
		fm.attachOscillator(1, depthLFO);
		
		// Set up notes
		noteFreq[NOTEA] = MidiNote.A4;
		noteFreq[NOTEB] = MidiNote.B4;
		noteFreq[NOTEC] = MidiNote.C4;
		noteFreq[NOTED] = MidiNote.D4;
		noteFreq[NOTEE] = MidiNote.E4;
		noteFreq[NOTEF] = MidiNote.F4;
		noteFreq[NOTEG] = MidiNote.G4;
		
		for (int i = NOTEA; i <= NOTEG; i++) {
			noteDown[i] = false;
		}
		
		setContentView(R.layout.synth_activity);
		
		// Start up the threads
		if (playerThread == null) {
			playerThread = new Thread(playerThreadObj);
			playerThread.start();
		}

		if (generatorThread == null) {
			generatorThread = new Thread(generatorThreadObj);
			generatorThread.start();
		}
		
		// Set up radio group
		initialiseRadioButtons();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	    setContentView(R.layout.synth_activity);
	    
	    // TODO - called when screen rotates/keyboard shown and hidden
	}
	
	@Override
    public void onStop() {
    	super.onStop();
    	
    	// interrupt audio threads
		if (playerThread != null) {
			playerThread.interrupt();
			playerThread = null;
		}

		if (generatorThread != null) {
			generatorThread.interrupt();
			generatorThread = null;
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.synth_activity, menu);
		return true;
	}
	
	// Radio button handlers

	private void initialiseRadioButtons () {
		// This will get the radiogroup
		RadioGroup rGroup = (RadioGroup)findViewById(R.id.radioGroupWaveType);
		
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
			        		
		        		case R.id.radiobtnTriangle:
			        		wave = WaveType.TRIANGLE;
			        		break;
			        	
		        		case R.id.radiobtnConcTriangle:
			        		wave = WaveType.CONC_TRIANGLE;
			        		break;
			        	
		        		case R.id.radiobtnConvTriangle:
			        		wave = WaveType.CONV_TRIANGLE;
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
		noteDown[NOTEA] = !noteDown[NOTEA];
		Log.i (TAG, "Note A is " + noteDown[NOTEA]);
	}
	
	public void btnBClick (View v) {
		noteDown[NOTEB] = !noteDown[NOTEB];
		Log.i (TAG, "Note B is " + noteDown[NOTEB]);
	}
	
	public void btnCClick (View v) {
		noteDown[NOTEC] = !noteDown[NOTEC];
		Log.i (TAG, "Note C is " + noteDown[NOTEC]);
	}
	
	public void btnDClick (View v) {
		noteDown[NOTED] = !noteDown[NOTED];
		Log.i (TAG, "Note D is " + noteDown[NOTED]);
	}
	
	public void btnEClick (View v) {
		noteDown[NOTEE] = !noteDown[NOTEE];
		Log.i (TAG, "Note E is " + noteDown[NOTEE]);
	}
	
	public void btnFClick (View v) {
		noteDown[NOTEF] = !noteDown[NOTEF];
		Log.i (TAG, "Note F is " + noteDown[NOTEF]);
	}
	
	public void btnGClick (View v) {
		noteDown[NOTEG] = !noteDown[NOTEG];
		Log.i (TAG, "Note G is " + noteDown[NOTEG]);
	}
	
	public void toggleBtnLPFClick (View v) {
		ToggleButton btn = (ToggleButton) findViewById(R.id.toggleBtnLPF);
		
		Log.i (TAG, "Toggling Low Pass Filter");
		
		if (btn.isChecked()) {
			// Enable LPF!
			fm.enableFilter(0);
		} else {
			// Disable LPF!
			fm.disableFilter(0);
		}
	}
	
	public void toggleBtnDepthLFOClick (View v) {
		ToggleButton btn = (ToggleButton) findViewById(R.id.toggleBtnDepthLFO);
		
		if (btn.isChecked()) {
			// Enable Depth LFO!
			fm.enableFilter(1);
		} else {
			// Disable Depth LFO!
			fm.disableFilter(1);
		}
	}
	
	Runnable playerThreadObj = new Runnable() {
		public void run() {
			boolean running = true;
			
			Log.i(TAG+".playerThread", "Started!");
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
			while (running) {
				while (!am.isPlaying()) {
					try {
						if (am.play()) {
							break;
						} else {
							Thread.sleep(80);
						}
					} catch (InterruptedException e) {
						Log.i(TAG+".playerThread", "Play thread interruped.");
						running = false;
						break;
					}
				}
				
				if (Thread.interrupted()) {
					Log.i(TAG+".playThread", "Play thread interruped.");
					running = false;
	            }
			}
			
			Log.i(TAG+".playerThread", "Shutting down...");
		}
	};

	Runnable generatorThreadObj = new Runnable() {
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
			
			int sampleRate = am.getSampleRate();
            boolean running = true;
            
            Log.i(TAG+".generatorThread", "Started!");
            
            while (running) {
            	// Generate silence to mix onto
            	short[] sampleData = sm.generateSilence(noteDuration, sampleRate);
            	
	        	// Generate audio
            	for (int i = NOTEA; i <= NOTEG; i++) {
            		if (noteDown[i]) {
            			Log.i (TAG, "Mixing note " + i);
            			short[] noteSampleData = sm.generateTone(noteDuration, noteFreq[i], volume, sampleRate);
            			sampleData = sm.mixTones(sampleData, noteSampleData);
            		}
        		}
            	
            	// Apply filters
                int[] filterIDs = fm.getEnabledFiltersList();
                
                for (int id : filterIDs) {
                	Log.i (TAG, "Applying filter " + id + " - " + fm.getFilterName(id));   
                	sampleData = fm.applyFilter(id, sampleData);
                }
	    		
	    		// Send to audio buffer
	    		am.buffer(sampleData);
		        
		        if (Thread.interrupted()) {
					Log.i(TAG+".generatorThread", "Tone generator thread interrupted.");
					running = false;
	            }
            }
            
            Log.i(TAG+".generatorThread", "Shutting down...");
		}
	};
	
}
