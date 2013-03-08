package com.masterthebass;

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

	private final static double rampUpLength = 0.5;
	private boolean rampUpAmplitude;
	
	private int mode = AudioTrack.MODE_STREAM;

	private final String LogTag = "AudioOutputManager";

	/* Constructor */

	public AudioOutputManager() {
		// Grab data from hardware
		getSampleRateFromHardware();

		// Instantiate audio manager
		audio = createAudioTrack(AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,/* 1.0,*/ mode);
		
		// Set volume ramp flag
		rampUpAmplitude = false;
	}

	/* Private methods */

	private void getSampleRateFromHardware() {
		nativeSampleRate = AudioTrack.getNativeOutputSampleRate(mode);
	}

	private AudioTrack createAudioTrack(int channel, int format, double bufDuration, int mode) {
		return createAudioTrack (channel, format, (int) (bufDuration * nativeSampleRate), mode);
	}

	private AudioTrack createAudioTrack(int channel, int format, int mode) {
		minBufferSize = getMinimumBufferSizeInSamples (channel, format);
		bufferSize = minBufferSize;
		
		return new AudioTrack (AudioManager.STREAM_MUSIC, nativeSampleRate, channel, format, minBufferSize, mode);
	}

	private AudioTrack createAudioTrack(int channel, int format, int bufSize, int mode) {
		minBufferSize = getMinimumBufferSizeInSamples (channel, format);

		if (bufSize < minBufferSize) {
			bufSize = minBufferSize;
		}

		bufferSize = bufSize;

		return new AudioTrack (AudioManager.STREAM_MUSIC, nativeSampleRate, channel, format, bufSize, mode);
	}

	private void stopStreaming() {
		pause();
		audio.flush();
		audio.stop();	
	}
	
	private int getMinimumBufferSizeInSamples(int channel, int format) {
		return AudioTrack.getMinBufferSize(nativeSampleRate, channel, format)/2;
	}

	/* Public methods */

	public void play() {
		audio.play();
		rampUpAmplitude = true;
	}

	public void stop() {
		if (mode == AudioTrack.MODE_STREAM) {
			stopStreaming();
		} else {
			audio.stop();
		}
	}

	public void pause() {
		audio.pause();
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
		int length, pos, written;
		pos = 0;
		
		while (pos < pcm.length) {			
			// Ensure there are enough bytes left to copy		
			if (pcm.length < (pos + bufferSize)) {
				length = pcm.length - pos;
			} else {
				length = bufferSize;
			}
			
			// Volume need adjusting?
			if (rampUpAmplitude) {
				int samples = (int) (rampUpLength * nativeSampleRate);
				
				if (samples > length) {
					samples = length;
				}
				
				double volume = 0;
				double volumeIncrement = (1.0/samples);
				
				for (int i = 0; i < samples; i++) {
					pcm[pos+i] = (short) (pcm[pos+i] * volume);
					volume += volumeIncrement;
				}
				
				rampUpAmplitude = false;
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
			
			if (Thread.interrupted()) {
				return;
			}
		}
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