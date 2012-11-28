package com.example.soundtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/////////////////////////////////////////////////////////////
//
// TODO
//
// Callback when audio playback is complete to update button
//
/////////////////////////////////////////////////////////////

public class MainActivity extends Activity {
	private AudioTrack audio = null;
	private int audioBufferSize;
	private boolean bufferFilled = false;
	private boolean resumeHasRun = false;
	private boolean audioPaused = false;
	private byte[] buffer;
	private Thread tAudioPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		pauseAudio();
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
			resumeAudio();
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		resumeAudio();
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		pauseAudio();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopAudioImmediately();
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
		stopAudioImmediately();
		
		Button b = (Button) findViewById(R.id.btnSoundCtrl);
		if (b.getText() == getString(R.string.btnSoundCtrl_pause)) {
			b.setText(getString(R.string.btnSoundCtrl_play));
		}
	}
	
	public void btnSoundCtrlClick(View view) {
		Button b = (Button) findViewById(R.id.btnSoundCtrl);
		
		if (b.getText() == getString(R.string.btnSoundCtrl_play)) {
			if (audioPaused) {
				resumeAudio();
			} else {
				// Instantiate AudioTrack object
				createAudioTrack();
				
				// Spawn a separate audio playing thread
				if (bufferFilled) {
					tAudioPlayer = new Thread(new Runnable() {
				        public void run() {
				        	playAudio(buffer);
				        }
				    });
					tAudioPlayer.start();
				}
			}
			
			// Set button text to reflect audio playing
			b.setText(getString(R.string.btnSoundCtrl_pause));
		} else {
			pauseAudio();
			
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
	
	// AudioTrack wrapper functions
	
	// Sets up an AudioTrack object
	// and populates the global instance
	private void createAudioTrack() {		
		int streamType = AudioManager.STREAM_MUSIC;
		int sampleRateHz = 8000;
		int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
		int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 16-bit signed
		int mode = AudioTrack.MODE_STREAM;
		
		// Get buffer size from API
		audioBufferSize = AudioTrack.getMinBufferSize(sampleRateHz, channelConfig, audioFormat);
		
		// Ensure old resources are released
		stopAudioImmediately();
		
		// Create new AudioTrack
		audio = new AudioTrack(streamType, sampleRateHz, channelConfig, audioFormat, audioBufferSize, mode);
	}
	
	private void releaseAudioTrack() {
		if (audio != null) {
			audio.release();
			audio = null;
		}
	}
	
	// Plays a PCM byte array by streaming it
	// to an AudioTrack object
	public void playAudio(byte[] pcm) {
		byte[] buffer;
		int i;
		
		// Begin playback
		if (audio != null) {
			try {
				audio.play();
			}
			catch (IllegalStateException e) {
				e.printStackTrace();
				return;
			}
		}
			
		// Write in a loop until buffer has been completely written
		// Includes an internal check to see if the audio track has been
		// released.	
		for (i=0; i<Math.ceil(pcm.length/audioBufferSize); i++) {
			buffer = new byte[audioBufferSize];
			System.arraycopy(pcm, i*audioBufferSize, buffer, 0, audioBufferSize);
			
			// Loop if soft paused
			do {
				if (audio != null) {
					if (!audioPaused) {
						try {
							audio.write(buffer, 0, audioBufferSize);
						}
						catch (IllegalStateException e) {
							e.printStackTrace();
							return;
						}
					}
				} else {
					// The AudioTrack has been killed, stop attempting playback
					return;
				}
			} while (audioPaused);
		}
		
		// Stop playing after the buffer has been exhausted
		// i.e. at the end of the audio
		if (audio != null) {
			audio.stop();
		}
	}
	
	// Pauses audio playback
	public void pauseAudio() {
		if (audio != null) {
			softPauseAudio();
			audio.pause();
		}
	}
	
	public void resumeAudio() {
		if (audio != null) {
			softResumeAudio();
			audio.play();
		}
	}
	
	public void softPauseAudio() {
		if (audio != null) {
			audioPaused = true;
		}
	}
	
	// Resumes audio playback
	public void softResumeAudio() {
		if (audio != null) {
			audioPaused = false;
		}
	}
	
	// Stops the audio playback.
	// This is immediate for stored audio
	// and at the end of the written buffer
	// for streamed audio. Resources are
	// release after the audio stops.
	public void stopAudio() {
		if (audio != null) {
			audioPaused = false;
			audio.stop();
			
			// Kill worker thread
			tAudioPlayer.interrupt();
			tAudioPlayer = null;
			
			// Release assets
			releaseAudioTrack();
		}
	}
	
	// Immediately stops the audio playback for streamed audio
	// This requires pausing the audio and flushing
	// the buffer - as merely stopping will play
	// until the buffer has been played.
	public void stopAudioImmediately() {
		if (audio != null) {
			audio.pause();
			audio.flush();
			stopAudio();
		}
	}
	
	// Interesting audioTrack functions:
	// setLoopPoints - loop a particular part of the PCM (including infinite looping)
	// attachAuxEffect - attaches and 'auxiliary audio effect' to the sound
	// android.media.audiofx.AudioEffect
}
