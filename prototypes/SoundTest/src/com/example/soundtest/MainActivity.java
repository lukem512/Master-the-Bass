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
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.res.AssetManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/////////////////////////////////////////////////////////////
//
// TODO
//
// Load file using worker thread starting at onCreate
// Set play button to enabled when file has been loaded
// Play audio in worker thread when play button is pressed
// Stop audio when stop button is pressed
//
/////////////////////////////////////////////////////////////

public class MainActivity extends Activity {
	private AudioTrack audio;
	private int audioBufferSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createAudioTrack();
		
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void btnSoundCtrlClick(View view) {
		Button b = (Button) findViewById(R.id.btnSoundCtrl);
		
		if (b.getText() == getString(R.string.btnSoundCtrl_play)) {
			b.setText(getString(R.string.btnSoundCtrl_stop));
			
			// TODO - spawn a separate thread
			// https://developer.android.com/guide/components/processes-and-threads.html
			try {
				new Thread(new Runnable() {
			        public void run() {
			        	// TODO - play audio here
			        }
			    }).start();
				
				byte[] buffer = readBinaryFile(Environment.getExternalStorageDirectory().getPath()+"/bjork.raw");
				playAudio(buffer);
			} catch (Exception e) {
				e.printStackTrace();
				Toast t = Toast.makeText(this, "Damn, sound didnt work.", Toast.LENGTH_LONG);
				t.show();
			}
		} else {
			stopAudio();
			b.setText(getString(R.string.btnSoundCtrl_play));
		}
		
		// TODO - set callback so we can change text when sound finishes
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
	
	private void createAudioTrack() {
		int streamType = AudioManager.STREAM_MUSIC;
		int sampleRateHz = 8000;
		int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
		int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 16-bit signed
		int mode = AudioTrack.MODE_STREAM;
		
		audioBufferSize = AudioTrack.getMinBufferSize(sampleRateHz, channelConfig, audioFormat);
		
		audio = new AudioTrack(streamType, sampleRateHz, channelConfig, audioFormat, audioBufferSize, mode);
	}
	
	public void playAudio(byte[] pcm) {
		byte[] buffer;
		int i;
		
		audio.play();
		// write in a loop until buffer has been completely written
		for (i=0; i<Math.ceil(pcm.length/audioBufferSize); i++) {
			buffer = new byte[audioBufferSize];
			System.arraycopy(pcm, i*audioBufferSize, buffer, 0, audioBufferSize);
			audio.write(buffer, 0, audioBufferSize);
		}
	}
	
	public void stopAudio() {
		audio.stop();
	}
	
	public void stopAudioImmediately() {
		audio.pause();
		audio.flush();
	}
	
	// Interesting audioTrack functions:
	// setLoopPoints - loop a particular part of the PCM (including infinite looping)
	// attachAuxEffect - attaches and 'auxiliary audio effect' to the sound
	// android.media.audiofx.AudioEffect
}
