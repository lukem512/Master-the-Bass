package com.masterthebass;

import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class RunActivity extends Activity {
	private AudioOutputManager am;
	private FileManager fm;
	private String logTag = "RunActivity";
	private boolean resumeHasRun, tone_stop;
	
	private LinkedList<byte[]> sampleList;
	private Thread tToneGenerate, tToneBuffer, tToneLoad;
	
	private double dur = 0.1;
	private double base = 400.0;
	private double vol = 0.1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Instantiate manager
        am = new AudioOutputManager();
        fm = new FileManager();
        
        // Set up linked list of samples
        sampleList = new LinkedList<byte[]>();
        
        // Starting state should be paused
        // for start/stop button to function
        am.pause();
        
        // Set flags
        resumeHasRun = false;
        tone_stop = false;
        
        setContentView(R.layout.activity_run);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
		
    	// Ensure certain code only runs when application is first
    	// loaded - such as prebuffering the audio file
		if (!resumeHasRun) {
			tToneLoad = new Thread(ToneLoad);
			tToneLoad.start();
			
			tToneGenerate = new Thread(ToneGenerate);
			//tToneGenerate.start();
			
			tToneBuffer = new Thread(ToneBuffer);
			tToneBuffer.start();
			
			// Set flag such that this code only runs once
			resumeHasRun = true;
		} else {
			
		}
    }
    
    @Override
	public void onDestroy() {
		super.onDestroy();
    	tToneLoad.interrupt();
		tToneBuffer.interrupt();
		tToneGenerate.interrupt();
	}
    
    Runnable ToneLoad = new Runnable () {
		private String threadTag = "tFileLoader";
		public void run() {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			
			byte[] buffer;
			try {
				// Read audio file from SD card
				buffer = fm.readBinaryFile(FileManager.getSDPath() + "/wub.wav");
			} catch (IOException e) {
				Log.e(logTag+"."+threadTag+".run", "Could not read PCM audio file.");
				e.printStackTrace();
				return;
			}
			
			Log.d(logTag+"."+threadTag+".run", "Audio file successfully loaded.");
			
			sampleList.add(buffer);
			
			Log.d(logTag+"."+threadTag+".run", "Audio file successfully buffered.");
		}
	};
		
	Runnable ToneGenerate = new Runnable () {
		public void run() {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			
			int sampleRate = am.getSampleRate();
			int samples = (int) Math.ceil(sampleRate * dur);
	        byte [] sampleData = new byte[samples];
			int i = 0;
	        boolean up = true;
	        
	        while(!tone_stop) {             
	        	if (i == 20 || i == -20) {
	            	up = !up;
	            }
	        	
	        	//Log.d ("toneBuf", "Generating frequency.");
	        	for (int j=0; j<10; j++) {
	        		sampleData = SoundManager.generateTone(dur, base+i, vol, sampleRate);
	        		sampleList.add(sampleData);
	        		
	        		if (Thread.interrupted()) {
						Log.d(logTag+".ToneGenerate", "Tone generating thread interrupted.");
	                	return;
	                }
	        	}
	        	
	        	//Log.d("toneBuf", "Wrote frequency " + (base+i) + " to buffer.");
	            
	            if (up) i++; else i--;
	        }
		}
	};
    
    Runnable ToneBuffer = new Runnable() {   
        public void run() {      	
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            
            byte[] sampleData;
            
            while(!tone_stop) {             
            	try {
            		sampleData = (byte[]) sampleList.removeFirst();
            		am.buffer(sampleData);
            		Log.d(".ToneBuffer", "Wrote frequency to buffer.");
            	}
            	catch (NoSuchElementException e) {
            		Log.w(".ToneBuffer", "Sample list is empty!");
            	}
                
				if (Thread.interrupted()) {
					Log.d(".ToneBuffer", "Tone buffering thread interrupted.");
                	return;
                }
            }
        }
    };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
		return true;
	}
	
	public void btnSoundCtrlClick(View view) {
		Button b = (Button) findViewById(R.id.btnSoundCtrl);
		
		if (am.isPaused()) {
			if (am.play()) {
				b.setText(getString(R.string.soundoff));
			}
		} else {
			am.pause();
			b.setText(getString(R.string.soundon));
		}
	}
	
	public void btnSoundStopClick(View view) {
		Button b = (Button) findViewById(R.id.btnSoundCtrl);
		
		if (!am.isStopped()) {
			tone_stop = true;
			am.stop();
			
			tToneBuffer.interrupt();
			tToneLoad.interrupt();
			
			b.setText(getString(R.string.soundon));
			am.pause();
		}
	}
}
