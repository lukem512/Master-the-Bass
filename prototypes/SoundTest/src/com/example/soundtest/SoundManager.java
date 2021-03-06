package com.example.soundtest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/////////////////////////////////////////////////////////////
//
// TODO
//
// Callback when audio playback is complete to update button
// Are all the if (audio != null) checks needed in playAudio()?
//
/////////////////////////////////////////////////////////////

//Interesting audioTrack functions:
// setLoopPoints - loop a particular part of the PCM (including infinite looping)
// attachAuxEffect - attaches and 'auxiliary audio effect' to the sound
// android.media.audiofx.AudioEffect

public class SoundManager implements AudioTrack.OnPlaybackPositionUpdateListener {
	private AudioTrack audio = null;
	private int audioBufferSize;
	private boolean audioPaused = false;
	private Thread tAudioPlayer = null;
	
	/* Constructor */
	
	public SoundManager() {
		// Create a track to speed up initial playback
		createAudioTrack();
		
		// Set worker thread to null
		tAudioPlayer = null;
		
		Log.d("SoundManager", "Constructed successfully!");
	}
	
	/* AudioTrack wrapper functions */
	
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
		
		// Hook callbacks
		audio.setPlaybackPositionUpdateListener(this);
	}
	
	private void releaseAudioTrack() {
		if (audio != null) {
			audio.release();
			audio = null;
		}
	}
	
	// Plays a PCM byte array by streaming it
	// to an AudioTrack object
	public void playAudio(final byte[] pcm) {
		tAudioPlayer = new Thread(new Runnable() {
	        public void run() {
	        	playAudioThread(pcm);
	        }
	    });
		tAudioPlayer.start();
	}
	
	private void playAudioThread(byte[] pcm) {
		byte[] buffer;
		int i, length, pos;
		
		// Instantiate audio if needed
		if (audio == null) {
			createAudioTrack();
		}
		
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
		
		// Set up the callback notifier for when the playback completes
		if (audio != null) {
			audio.setNotificationMarkerPosition(pcm.length); // TODO - this doesn't work
		}
		
		pos = 0;
			
		// Write in a loop until buffer has been completely written
		// Includes an internal check to see if the audio track has been
		// released.	
		for (i=0; i<Math.ceil(pcm.length/audioBufferSize); i++) {
			if (pcm.length < (pos + audioBufferSize)) {
				length = pcm.length - pos;
			} else {
				length = audioBufferSize;
			}
			
			buffer = new byte[length];
			System.arraycopy(pcm, pos, buffer, 0, length);
			
			// Loop if soft paused
			do {
				if (audio != null) {
					if (!isPaused()) {
						try {
							audio.write(buffer, 0, length);
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
			} while (isPaused());
			
			pos += length;
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
	
	// Resumes audio playback
	public void resumeAudio() {
		if (audio != null) {
			softResumeAudio();
			audio.play();
		}
	}
	
	// Pauses audio using a flag
	public void softPauseAudio() {
		if (audio != null) {
			audioPaused = true;
		}
	}
	
	// Resumes audio using a flag
	public void softResumeAudio() {
		if (audio != null) {
			audioPaused = false;
		}
	}
	
	
	// Returns the state of the audio playback
	public boolean isPaused() {
		return audioPaused;
	}
	
	
	// Stops the audio playback.
	// This is immediate for stored audio
	// and at the end of the written buffer
	// for streamed audio. Resources are
	// release after the audio stops.
	public void stopAudio() {
		if (audio != null) {
			// Stop playback
			audio.stop();
			
			Log.d("stopAudio", "Audio stopped.");
			
			// Kill worker thread
			if (tAudioPlayer != null) {
				Log.d("stopAudio", "Killing worker thread...");
				tAudioPlayer.interrupt();
				Log.d("stopAudio", "Dead!");
			}
			tAudioPlayer = null;
			
			// Release assets
			releaseAudioTrack();
			
			// Reset flag
			audioPaused = false;
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
	
	// Returns the time taken to reach a frame in milliseconds
	// given the sample rate
	public int framesToMillis(int frames, int sampleRate) {
		return (int) Math.round((frames / sampleRate) * 1000.0);
	}
	
	/* AudioTrack.OnPlaybackPositionUpdateListener Methods */

	@Override
	public void onMarkerReached(AudioTrack track) {	
		Log.d("onMarkerReached", "Playback has completed.");
	}

	@Override
	public void onPeriodicNotification(AudioTrack track) {
		// Auto-generated method stub
	}
}
