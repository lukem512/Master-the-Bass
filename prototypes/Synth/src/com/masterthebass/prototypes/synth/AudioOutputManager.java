package com.masterthebass.prototypes.synth;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

//Interesting audioTrack functions:
//setLoopPoints - loop a particular part of the PCM (including infinite looping)
//attachAuxEffect - attaches and 'auxiliary audio effect' to the sound
//android.media.audiofx.AudioEffect

public class AudioOutputManager implements AudioTrack.OnPlaybackPositionUpdateListener {

	/* Members */

	private AudioTrack audio;	

	private int nativeSampleRate;
	private int bufferSize;
	private int minBufferSize;

	private int mode = AudioTrack.MODE_STREAM;

	private final int maxRetries = 16;

	private final String LogTag = "AudioOutputManager";

	/* Constructor */

	public AudioOutputManager() {
		// Grab data from hardware
		getSampleRateFromHardware();

		// Instantiate audio manager
		audio = createAudioTrack(AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,/* 1.0,*/ mode);
		audio.stop();
		
		Log.d(LogTag+".ctor", "AudioOutputManager object constructed.");
	}

	/* Private methods */

	private void getSampleRateFromHardware() {
		nativeSampleRate = AudioTrack.getNativeOutputSampleRate(mode);
		Log.d(LogTag+".getSampleRateFromHardware", "Native sample rate is " + nativeSampleRate + " Hz.");
	}

	private AudioTrack createAudioTrack(int channel, int format, double bufDuration, int mode) {
		return createAudioTrack (channel, format, (int) (bufDuration * nativeSampleRate), mode);
	}

	private AudioTrack createAudioTrack(int channel, int format, int mode) {
		minBufferSize = getMinimumBufferSizeInSamples (channel, format);
		
		bufferSize = minBufferSize;
		Log.d(LogTag+".createAudioTrack", "Setting buffer size to " + minBufferSize + " bytes.");
		
		return new AudioTrack (AudioManager.STREAM_MUSIC, nativeSampleRate, channel, format, minBufferSize, mode);
	}

	private AudioTrack createAudioTrack(int channel, int format, int bufSize, int mode) {
		minBufferSize = getMinimumBufferSizeInSamples (channel, format);
		Log.w(LogTag+".createAudioTrack", "Minimum buffer size is " + minBufferSize + " samples.");

		if (bufSize < minBufferSize) {
			bufSize = minBufferSize;
		}

		bufferSize = bufSize;
		Log.d(LogTag+".createAudioTrack", "Setting buffer size to " + bufferSize + " samples.");

		return new AudioTrack (AudioManager.STREAM_MUSIC, nativeSampleRate, channel, format, bufSize, mode);
	}

	private void stopStreaming() {
		pause();
		audio.flush();
		audio.stop();
		Log.d(LogTag+".stopStreaming", "Paused, flushed and stopped audio.");
	}
	
	private int getMinimumBufferSizeInSamples(int channel, int format) {
		return AudioTrack.getMinBufferSize(nativeSampleRate, channel, format)/2;
	}

	/* Public methods */

	public void play() {
		audio.play();
		Log.d(LogTag+".play", "Set audio to playing.");
	}

	public void stop() {
		if (mode == AudioTrack.MODE_STREAM) {
			stopStreaming();
		} else {
			audio.stop();
		}
		Log.d(LogTag+".stop", "Set audio to stopped.");
	}

	public void pause() {
		audio.pause();
		Log.d(LogTag+".pause", "Set audio to paused.");
	}

	public boolean isPaused() {
		return (audio.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) ? true : false;
	}

	public boolean isPlaying() {
		return (audio.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) ? true : false;
	}

	public boolean isStopped() {
		return (audio.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) ? true : false;
	}

	public int getSampleRate() {
		return nativeSampleRate;
	}
	
	public void buffer(short[] pcm) {
		int length, pos, written, prev, retries;

		//Log.i(LogTag+".buffer", "Writing " + pcm.length + " into audio buffer of size " + bufferSize + ".");

		pos = 0;
		prev = 0;
		retries = 0;
		
		while ((pos < pcm.length) /*&& (retries < maxRetries)*/) {			
			//Log.i(LogTag+".buffer", "At position " + pos + ".");

			// Ensure there are enough bytes left to copy		
			if (pcm.length < (pos + bufferSize)) {
				length = pcm.length - pos;
			} else {
				length = bufferSize;
			}

			try {
				written = audio.write(pcm, pos, length);
			}
			catch (IllegalStateException e) {
				Log.e(LogTag+".buffer", "Could not write to AudioTrack.");
				e.printStackTrace();
				return;
			}

			pos += written;
			
			if (written == prev) {
				retries++;
			} else {
				prev = written;
			}

			//Log.i(LogTag+".buffer", "Wrote " + written + " bytes successfully, " + (pcm.length - pos) + " remaining.");

			if (Thread.interrupted()) {
				Log.i(LogTag+".buffer", "Thread was interrupted.");
				return;
			}
		}

		//Log.i(LogTag+".buffer", "Exiting...");
	}

	/* AudioTrack.OnPlaybackPositionUpdateListener methods */

	@Override
	public void onMarkerReached(AudioTrack track) {
		// TODO - callbacks
	}

	@Override
	public void onPeriodicNotification(AudioTrack track) {
		// TODO - callbacks
	}

}