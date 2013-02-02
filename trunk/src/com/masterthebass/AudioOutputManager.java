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

	private int numBytesBuffered;

	private int nativeSampleRate;
	private int bufferSize;
	private int minBufferSize;

	private int mode = AudioTrack.MODE_STREAM;

	private String logTag = "AudioOutputManager";

	/* Constructor */

	public AudioOutputManager() {
		// Grab data from hardware
		getSampleRateFromHardware();

		// Instantiate audio manager
		audio = createAudioTrack(AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 1.0, mode);
		audio.stop();

		// Set flags
		numBytesBuffered = 0;
		
		Log.d(logTag+".ctor", "AudioOutputManager object constructed.");
	}

	/* Private methods */

	private void getSampleRateFromHardware() {
		nativeSampleRate = AudioTrack.getNativeOutputSampleRate(mode);
		Log.d(logTag+".getSampleRateFromHardware", "Native sample rate is " + nativeSampleRate + " Hz.");
	}

	private AudioTrack createAudioTrack(int channel, int format, double bufDuration, int mode) {
		return createAudioTrack (channel, format, (int) (bufDuration * nativeSampleRate), mode);
	}

	private AudioTrack createAudioTrack(int channel, int format, int mode) {
		minBufferSize = AudioTrack.getMinBufferSize(nativeSampleRate, channel, format);
		return new AudioTrack (AudioManager.STREAM_MUSIC, nativeSampleRate, channel, format, minBufferSize, mode);
	}

	private AudioTrack createAudioTrack(int channel, int format, int bufSize, int mode) {
		minBufferSize = AudioTrack.getMinBufferSize(nativeSampleRate, channel, format);
		Log.w(logTag+".createAudioTrack", "Minimum buffer size is " + minBufferSize + " bytes.");

		if (bufSize < minBufferSize) {
			bufSize = minBufferSize;
		}

		bufferSize = bufSize;
		Log.d(logTag+".createAudioTrack", "Setting buffer size to " + bufferSize + " bytes.");

		return new AudioTrack (AudioManager.STREAM_MUSIC, nativeSampleRate, channel, format, bufSize, mode);
	}

	private void stopStreaming() {
		pause();
		audio.flush();
		audio.stop();
		Log.d(logTag+".stopStreaming", "Paused, flushed and stopped audio.");
	}

	/* Public methods */

	public boolean play() {
		if (numBytesBuffered >= minBufferSize) {
			audio.play();
			Log.d(logTag+".play", "Set audio to playing.");
			return true;
		} else {
			Log.w(logTag+".play", "Not enough samples to play yet.");
			Log.w(logTag+".play", "Got " + numBytesBuffered + " need " + minBufferSize + ".");
			return false;
		}
	}

	public void stop() {
		if (mode == AudioTrack.MODE_STREAM) {
			stopStreaming();
		} else {
			audio.stop();
		}
		Log.d(logTag+".stop", "Set audio to stopped.");
	}

	public void pause() {
		audio.pause();
		Log.d(logTag+".pause", "Set audio to paused.");
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
		//return nativeSampleRate;
		return 44100;
	}

	public void buffer(byte[] pcm) {
		byte[] buffer;
		int length, pos, written;

		//Log.d(logTag+".buffer", "Writing " + pcm.length + " into audio buffer of size " + bufferSize + ".");

		pos = 0;
		while (pos < pcm.length) {			
			//Log.d(logTag+".buffer", "At position " + pos + ".");

			// Ensure there are enough bytes left to copy
			if (pcm.length < (pos + bufferSize)) {
				length = pcm.length - pos;
			} else {
				length = bufferSize;
			}

			buffer = new byte[length];
			System.arraycopy(pcm, pos, buffer, 0, length);

			try {
				written = audio.write(buffer, 0, length);
			}
			catch (IllegalStateException e) {
				Log.e(logTag+".buffer", "Could not write to AudioTrack.");
				e.printStackTrace();
				return;
			}

			pos += written;
			numBytesBuffered += written;

			//Log.d(logTag+".buffer", "Wrote " + written + " bytes successfully, " + (pcm.length - pos) + " remaining.");

			if (Thread.interrupted()) {
				Log.d(logTag+".buffer", "Thread was interrupted.");
				return;
			}
		}

		//Log.d(logTag+".buffer", "Exiting...");
	}
	
	public void buffer(short[] pcm) {
		byte[] bytepcm = new byte[pcm.length * 2];
		int idx = 0;
		
		// convert to byte[]
		for (int i = 0; i < pcm.length; i = i+2) {
			bytepcm[idx++] = (byte) (pcm[i] & 0x00ff);
			bytepcm[idx++] = (byte) ((pcm[i] & 0xff00) >>> 8);
		}
		
		// call buffer with byte array
		buffer (bytepcm);
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