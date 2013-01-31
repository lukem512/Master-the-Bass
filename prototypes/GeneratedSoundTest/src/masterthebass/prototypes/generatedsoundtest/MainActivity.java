package masterthebass.prototypes.generatedsoundtest;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	private SoundManager sm;
	private AudioOutputManager am;
	private FilterManager fm;
	private LinkedList<byte[]> sampleList;
	private Thread toneGeneratorThread, playThread, bufferThread;
	private boolean tone_stop = true;
	private double base = 30;
	private double vol = 0.7;
	private double dur = 0.05;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instantiate managers
		am = new AudioOutputManager();
		sm = new SoundManager();
		fm = new FilterManager();
		sampleList = new LinkedList<byte[]>();
		
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	// Listener for btnCtrl click event
	public void btnCtrlOnClick(View view) {
		Button b = (Button) findViewById(R.id.button1);
		
		if (tone_stop) {	
			// Play the audio, when the buffer is ready
			playThread = new Thread(playTone);
			playThread.start();
			
			bufferThread = new Thread(toneBufferer);
			bufferThread.start();
			
			
			// generate a tone
			tone_stop = false;
			toneGeneratorThread = new Thread(toneGenerator);
			toneGeneratorThread.start();
			
			// set text
			b.setText(getString(R.string.btnCtrl_text_stop));
		} else {			
			// stop worker thread
			toneGeneratorThread.interrupt();
			playThread.interrupt();
			bufferThread.interrupt();
			
			// stop audio
			am.stop();
			tone_stop = true;
			
			// set text
			b.setText(getString(R.string.btnCtrl_text_go));
		}
	}
	
	Runnable playTone = new Runnable() {
		public void run() {
			while (!am.play()) {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				try {
					Thread.sleep(80);
				} catch (InterruptedException e) {
					Log.d("playTone", "Play thread interruped.");
					return;
				}
			}
			Log.d("playTone", "Tone playback started.");
		}
	};
	
	Runnable toneBufferer = new Runnable() {
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
			
			int sampleRate = am.getSampleRate();
			int samples = (int) Math.ceil(sampleRate * dur);
            byte [] sampleData = new byte[samples];
            
            FileManager fileman = new FileManager();
            
            // Get the low-pass filter
            LowPassFilter f = (LowPassFilter) fm.getFilter (0);
            
            // DEBUGGING
         //   Log.d("toneBuf", "Got filter \""+f.getName()+"\"\n");
            
            int i = 0;
            boolean up = false;
            while(!tone_stop)
            {             
            	if (i == 50 || i == 0) {
                	up = !up;
                }
            	
            	Log.d ("toneBuf", "Generating frequency.");
            	
            	for (int j=0; j<1; j++) {
            		sampleData = sm.generateTone(dur, base, vol, sampleRate);
            		
            		// apply the filter
            		sampleData = f.applyFilter (sampleData);
            		
            		sampleList.add(sampleData);
            		fileman.appendBinaryFile(FileManager.getSDPath(), "test.wav", sampleData);
            		
            		if (Thread.interrupted()) {
    					Log.d("toneBuf", "Tone buffering thread interrupted.");
                    	return;
                    }
            	}
            	
            	f.setCutoffFrequency((i*100));
            	Log.d("toneBuf", "I = " + (i));
            	
            	Log.d("toneBuf", "Wrote frequency " + (base+i) + " to sample list.");
                
                if (up) i++; else i--;
            }
		}
	};
	
	Runnable toneGenerator = new Runnable()
    {   
        public void run() {      	
        	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
        	
            byte[] sampleData;
            while(!tone_stop)
            {             
            	try {
            		sampleData = (byte[]) sampleList.removeFirst();
            		am.buffer(sampleData);
            		Log.d("toneGen", "Wrote generated frequency to buffer.");
            	}
            	catch (NoSuchElementException e) {
            		//Log.w("toneGen", "Sample list is empty!");
            	}
                
				if (Thread.interrupted()) {
					Log.d("toneGen", "Tone generation thread interrupted.");
                	return;
                }
            }
            
            Log.d("toneGen", "Exiting...");
        }
    };

}
