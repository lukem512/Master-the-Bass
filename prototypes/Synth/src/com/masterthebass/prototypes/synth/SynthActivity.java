package com.masterthebass.prototypes.synth;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.masterthebass.prototypes.synth.WaveButton.onWaveChangeListener;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

// TODO	- ABILITY TO MIX NOTES
//
//		- rate modulation
//		- maintain button results when activity is recreated
//		- ability to change settings for oscillators (rate/depth)
//		- ability to change settings for keyboard (master volume, envelope)

public class SynthActivity extends Activity {
	
	private AudioOutputManager am;
	private SoundManager sm;
	private FilterManager fm;
	
	private Oscillator LFO1;
	private Oscillator LFO2;
	
	private final static int numNotes = 7;
	private double[] noteFreq = new double[numNotes];
	private boolean[] noteDown = new boolean[numNotes];
	
	private Thread generatorThread;
	private Thread writerThread;
	
	private final int NOTEA = 0;
	private final int NOTEB = 1;
	private final int NOTEC = 2;
	private final int NOTED = 3;
	private final int NOTEE = 4;
	private final int NOTEF = 5;
	private final int NOTEG = 6;
	
	private final static double noteDuration = 0.05;
	private double volume = 1.0;
	
	private LinkedList<short[]> sampleList;
	private int sampleListMaxSize;
	
	private final static String TAG = "Synth";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instantiate managers
		am = new AudioOutputManager();
		sm = new SoundManager();	
		fm = new FilterManager();
		
		// Instantiate Oscillators
		LFO1 = new Oscillator (am, WaveType.SINE, volume, 5);
		LFO2 = new Oscillator (am, WaveType.SINE, volume, 3);
		
		// Attach to filters
		fm.attachOscillator(0, LFO1); // Low Pass Filter
		fm.attachOscillator(1, LFO2); // Amplitude Filter
		
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
		
		// Initialise the controls for the components
		intialiseButtons();
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
		/*if (writerThread != null) {
			writerThread.interrupt();
			writerThread = null;
		}*/

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
	
	private void intialiseButtons() {
		WaveButton b;
		
		b = (WaveButton) findViewById(R.id.btnKeyboardWave);		
		b.setOnWaveChangeListener(new keyboardOnWaveChangeListener());
		
		b = (WaveButton) findViewById(R.id.btnOscOneWave);		
		b.setOnWaveChangeListener(new oscOneOnWaveChangeListener());
		
		b = (WaveButton) findViewById(R.id.btnOscTwoWave);		
		b.setOnWaveChangeListener(new oscTwoOnWaveChangeListener());
	}
	
	private void checkAudioThreadsInitialised() {	
		// Create the buffer
		sampleList = new LinkedList<short[]>();
		sampleListMaxSize = 8;
		
		// Start up the threads
		if (writerThread == null) {
			writerThread = new Thread(writerThreadObj, "Writer Thread");
			writerThread.setDaemon(true);
			writerThread.start();
		}

		if (generatorThread == null) {
			generatorThread = new Thread(generatorThreadObj, "Generator Thread");
			generatorThread.setDaemon(true);
			generatorThread.start();
		}
		
		// Set the audio to playing
		am.play();
	}
	
	// Button handlers
	
	private class keyboardOnWaveChangeListener implements onWaveChangeListener {
		@Override
		public void onClick(View v) {
			WaveButton b = (WaveButton) findViewById(R.id.btnKeyboardWave);		
			sm.setWaveType(b.getWave());
			Log.i (TAG, "Setting keyboard wave.");
		}
	}
	
	private class oscOneOnWaveChangeListener implements onWaveChangeListener {
		@Override
		public void onClick(View v) {
			WaveButton b = (WaveButton) findViewById(R.id.btnOscOneWave);		
			LFO1.setWaveType(b.getWave());
			Log.i (TAG, "Setting LFO1 wave.");
		}
	}
	
	private class oscTwoOnWaveChangeListener implements onWaveChangeListener {
		@Override
		public void onClick(View v) {
			WaveButton b = (WaveButton) findViewById(R.id.btnOscTwoWave);		
			LFO2.setWaveType(b.getWave());
			Log.i (TAG, "Setting LFO2 wave.");
		}
	}
	
	public void btnAClick (View v) {
		noteDown[NOTEA] = !noteDown[NOTEA];
		Log.i (TAG, "Note A is " + noteDown[NOTEA]);
		checkAudioThreadsInitialised();
	}
	
	public void btnBClick (View v) {
		noteDown[NOTEB] = !noteDown[NOTEB];
		Log.i (TAG, "Note B is " + noteDown[NOTEB]);
		checkAudioThreadsInitialised();
	}
	
	public void btnCClick (View v) {
		noteDown[NOTEC] = !noteDown[NOTEC];
		Log.i (TAG, "Note C is " + noteDown[NOTEC]);
		checkAudioThreadsInitialised();
	}
	
	public void btnDClick (View v) {
		noteDown[NOTED] = !noteDown[NOTED];
		Log.i (TAG, "Note D is " + noteDown[NOTED]);
		checkAudioThreadsInitialised();
	}
	
	public void btnEClick (View v) {
		noteDown[NOTEE] = !noteDown[NOTEE];
		Log.i (TAG, "Note E is " + noteDown[NOTEE]);
		checkAudioThreadsInitialised();
	}
	
	public void btnFClick (View v) {
		noteDown[NOTEF] = !noteDown[NOTEF];
		Log.i (TAG, "Note F is " + noteDown[NOTEF]);
		checkAudioThreadsInitialised();
	}
	
	public void btnGClick (View v) {
		noteDown[NOTEG] = !noteDown[NOTEG];
		Log.i (TAG, "Note G is " + noteDown[NOTEG]);
		checkAudioThreadsInitialised();
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
	
	// Threads for audio generation and playback
	
	Runnable writerThreadObj = new Runnable() {
		public void run() {
			boolean running = true;
			short[] sampleData;
			
			Log.i(TAG+".writerThread", "Started!");
			
			while (running) {
				try {
					sampleData = sampleList.removeFirst();
					am.buffer(sampleData);
				}
				catch (NoSuchElementException e) {
					//Log.w (TAG+".writerThread", "Sample buffer is empty.");
				}
				
				if (Thread.interrupted()) {
					Log.i(TAG+".writerThread", "Tone buffering thread interrupted.");
					running = false;
	            }
			}
			
			Log.i(TAG+".writerThread", "Shutting down...");
		}
	};

	Runnable generatorThreadObj = new Runnable() {
		public void run() {
			sm = new SoundManager();
			//android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
			
			int sampleRate = am.getSampleRate();
            boolean running = true;
            boolean sampleModified = false;
            short[] sampleData;
            
            // Generate silence to mix onto
            short[] silence = sm.generateSilence(noteDuration, sampleRate);
            
            Log.i(TAG+".generatorThread", "Started!");
            
            while (running) {
            	if (sampleList.size() < sampleListMaxSize) {
            		// Start with a silent 'wave'
	            	sampleData = silence;
	            	
		        	// Generate audio
	            	// TODO - this audio mixing doesn't sound nice
	            	for (int i = NOTEA; i <= NOTEG; i++) {
	            		if (noteDown[i]) {
	            			Log.i (TAG, "Mixing note " + i);
	            			short[] noteSampleData = sm.generateTone(noteDuration, noteFreq[i], volume, sampleRate);
	            			sampleData = sm.mixTones(sampleData, noteSampleData);
	            			sampleModified = true;
	            		}
	        		}
	            	
	            	// Commit the changes to sound manager
	            	sm.commit();
	            	
	            	// Apply filters
	                int[] filterIDs = fm.getEnabledFiltersList();
	                
	                for (int id : filterIDs) {
	                	Log.i (TAG, "Applying filter " + id + " - " + fm.getFilterName(id));   
	                	sampleData = fm.applyFilter(id, sampleData);
	                	sampleModified = true;
	                }
		    		
		    		// Send to audio buffer
	                if (sampleModified) {
		                sampleList.add(sampleData);	
	                }
	                
	                // Reset flag
	                sampleModified = false;
            	}
            	
            	if (Thread.interrupted()) {
					Log.i(TAG+".generatorThread", "Tone generator thread interrupted.");
					running = false;
	            }
            }
            
            Log.i(TAG+".generatorThread", "Shutting down...");
		}
	};
	
}
