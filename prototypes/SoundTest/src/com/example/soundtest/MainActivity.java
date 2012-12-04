package com.example.soundtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	private boolean bufferFilled = false;
	private boolean resumeHasRun = false;
	private byte[] buffer;	
	private SoundManager sm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instantiate SoundManager
		sm = new SoundManager();
		Log.d("onResume", "SoundManager instantiated");
		
		setContentView(R.layout.activity_main);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		sm.pauseAudio();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (!resumeHasRun) {
			// First time onResume is run is when app loads
			
			// Disable buttons until file has been loaded
			Button btnSoundCtrl = (Button) findViewById(R.id.btnSoundCtrl);
			Button btnSoundStop = (Button) findViewById(R.id.btnSoundStop);
			
			btnSoundCtrl.setEnabled(true);
			btnSoundStop.setEnabled(true);
			
			// Spawn a thread to load the audio file
			final Thread tAudioBuffer = new Thread (new Runnable() {
				public void run() {
					while (!bufferFilled) {
						try {
							buffer = readBinaryFile(Environment.getExternalStorageDirectory().getPath()+"/bjork.raw");
							bufferFilled = true;
						} catch (IOException e) {
							e.printStackTrace();
							bufferFilled = false;
						}
					}
					
					Log.d("tAudioBuffer", "Audio file has been loaded into buffer.");
				}
			});
			tAudioBuffer.start();
			
			//Spawn a thread to enable buttons when file is loaded
			final Thread tAudioBufferWatcher = new Thread (new Runnable() {
				public void run() {
					while (!bufferFilled) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
					}
					
					Button btnSoundCtrl = (Button) findViewById(R.id.btnSoundCtrl);
					Button btnSoundStop = (Button) findViewById(R.id.btnSoundStop);
					
					btnSoundCtrl.setEnabled(true);
					btnSoundStop.setEnabled(true);
					
					Log.d("tAudioBufferWatcher", "Buttons enabled!");
				}
			});
			tAudioBufferWatcher.start();
			
			// Set flag to ensure setup code is not repeated
			resumeHasRun = true;
		} else {
			// Subsequent onResume event behaviour
			sm.resumeAudio();
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sm.resumeAudio();
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		sm.pauseAudio();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		sm.stopAudioImmediately();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void btnSoundStopClick(View view) {
		// Stop audio
		sm.stopAudioImmediately();
		
		Button b = (Button) findViewById(R.id.btnSoundCtrl);
		if (b.getText() == getString(R.string.btnSoundCtrl_pause)) {
			b.setText(getString(R.string.btnSoundCtrl_play));
		}
	}
	
	public void btnSoundCtrlClick(View view) {
		Button b = (Button) findViewById(R.id.btnSoundCtrl);
		
		if (b.getText() == getString(R.string.btnSoundCtrl_play)) {
			if (sm.isPaused()) {
				sm.resumeAudio();
			} else {
				// Play the audio
				sm.playAudio(buffer);
			}
			
			// Set button text to reflect audio playing
			b.setText(getString(R.string.btnSoundCtrl_pause));
		} else {
			sm.pauseAudio();
			
			// Set button text to reflect audio pausing
			b.setText(getString(R.string.btnSoundCtrl_play));
		}
	}
	
	// Read binary file
	
	public byte[] readBinaryFile(String filename) throws IOException {
		File fp = new File(filename);
		int fpLen = (int) fp.length();
		FileInputStream fis = new FileInputStream(fp);
		return pumpBinaryFile(fis, fpLen);
	}
	
	public byte[] pumpBinaryFile(InputStream in, int size) throws IOException {
		int bufferSize = 1024, done = 0;
	    byte[] buffer = new byte[bufferSize];
	    byte[] result = new byte[size];
	    
	    while (done < size) {
	        int read = in.read(buffer);
	        if (read == -1) {
	            throw new IOException("Something went horribly wrong");
	        }
	        System.arraycopy(buffer, 0, result, done, read);
	        done += read;
	    }
	    
	    in.close();
	    
	    return result;
	}
}
