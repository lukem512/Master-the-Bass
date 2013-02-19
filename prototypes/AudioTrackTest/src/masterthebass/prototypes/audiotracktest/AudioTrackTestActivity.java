package masterthebass.prototypes.audiotracktest;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class AudioTrackTestActivity extends Activity {

	private AudioTrack at;
	private int sampleRate;
	private int bufferSize;
	private int encoding;
	private int format;
	private int mode;
	private int stream;
	
	private LinkedList<short[]> sampleList;
	
	private int outBuffSize;
	private float currentAngle;
	private float angleIncrement;
	private float freq;
	
	private int sampleListMaxSize;
	
	private Thread toneGeneratorThread, toneWriterThread;
	
	private final static float twoPI = (2.0f * (float) Math.PI);
	private final static String LogTag = "AudioTrackTest"; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		encoding = AudioFormat.ENCODING_PCM_16BIT;
		format = AudioFormat.CHANNEL_OUT_STEREO;
		mode = AudioTrack.MODE_STREAM;
		stream = AudioManager.STREAM_MUSIC;
		
		freq = 440f;
		sampleRate = 44100;
		
		bufferSize = AudioTrack.getMinBufferSize(sampleRate, format, encoding);
		at = new AudioTrack(stream, sampleRate, format, encoding, bufferSize, mode);
		at.pause();
		
		sampleList = new LinkedList<short[]>();
		sampleListMaxSize = 32;
		
		setContentView(R.layout.activity_audio_track_test);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_audio_track_test, menu);
		return true;
	}
	
	// Listener for onClick event
	public void btnGoClick (View v) {
		if (at.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
			at.pause();
		} else {
			at.play();
			
			if (toneGeneratorThread == null) {
				toneGeneratorThread = new Thread(toneGenerator);
				toneGeneratorThread.start();
			}
			
			if (toneWriterThread == null) {
				toneWriterThread = new Thread(toneWriter);
				toneWriterThread.start();
			}
		}
	}
	
	public short[] tick()
	{
		int size = outBuffSize/2;
	    short[] outBuff = new short[size]; // (buffer size in Bytes) / 2
	    
	    for (int i = 0; i < size; i++) {
	        outBuff[i] = (short) (Short.MAX_VALUE * ((float) Math.sin(currentAngle)));

	        // Update angleIncrement, as the frequency may have changed by now
	        angleIncrement = twoPI * freq / sampleRate;
	        currentAngle = ((currentAngle + angleIncrement) % twoPI);  
	    }
	    
	    return outBuff;     
	}
	
	
	Runnable toneWriter = new Runnable() {
		public void run() {
			//android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
			
			short[] sampleData;
			
			while (!Thread.interrupted()) {
				try {
					sampleData = sampleList.removeFirst();
					at.write(sampleData, 0, sampleData.length);
				}
				catch (NoSuchElementException e) {
					Log.w (LogTag, "Sample buffer is empty");
				}
			}
		}
	};
	
	Runnable toneGenerator = new Runnable()
    {   
		public void run() {      	
        	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
        	
        	short[] sampleData;
        	
        	currentAngle = 0f;
    		outBuffSize = bufferSize;
        	
        	while (!Thread.interrupted()) {
        		if (sampleList.size() < sampleListMaxSize) {
	        		sampleData = tick();
	        		sampleList.add(sampleData);
        		}
        	}
        }
    };

}
