package masterthebass.prototypes.generatedsoundtest;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	private AudioOutputManager am;
	private Thread toneGeneratorThread, playThread;
	private boolean tone_stop = true;
	private double base = 400;
	private double vol = 0.7;
	private double dur = 0.2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instantiate managers
		am = new AudioOutputManager();
		
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
	
	Runnable toneGenerator = new Runnable()
    {   
        public void run()
        {      	
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            
            int samples = (int) Math.ceil(am.getSampleRate() * dur);
            byte [] noiseData = new byte[samples];

            int i = 0;
            boolean up = true;
            while(!tone_stop)
            {             
            	if (i == 20 || i == -20) {
                	up = !up;
                }
            	
            	noiseData = SoundManager.generateTone(dur, base+i, vol, am.getSampleRate());
            	am.buffer(noiseData);
            	
            	Log.d("toneGen", "Wrote frequency " + (base+i) + " to buffer.");
                
                if (up) i++; else i--;
                
				if (Thread.interrupted()) {
					Log.d("toneGen", "Tone generation thread interrupted.");
                	return;
                }
            }
        }
    };

}
