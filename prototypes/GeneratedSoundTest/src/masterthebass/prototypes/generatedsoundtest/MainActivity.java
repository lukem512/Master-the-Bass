package masterthebass.prototypes.generatedsoundtest;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	private SoundManager sm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instantiate SoundManager
		sm = new SoundManager();
		
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
		byte[] pcm;
		int numFreq = 20;
		double base = 400;
		double vol = 0.7;
		double dur = 0.2;
		
		sm.playAudio();
		
		// TODO - generate a tone
		for (int i=0; i<=numFreq; i++) {
			pcm = genTone(dur, base+i, vol);
			sm.bufferAudio(pcm);
		}
		
		for (int i=numFreq; i>=0; i--) {
			pcm = genTone(dur, base+i, vol);
			sm.bufferAudio(pcm);
		}
		
		for (int i=0; i<=numFreq; i++) {
			pcm = genTone(dur, base-i, vol);
			sm.bufferAudio(pcm);
		}
		
		for (int i=numFreq; i>=0; i--) {
			pcm = genTone(dur, base-i, vol);
			sm.bufferAudio(pcm);
		}
	}

	// Generate a tone that is duration seconds long at specified frequency
	// Volume is specified between 0.0 and 1.0, where 1.0 is maximum
	// http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
	private byte[] genTone(double duration, double frequency, double volume) {
		int sampleRate = sm.getSampleRate();
		int numSamples = (int) Math.ceil(sampleRate * duration);
		double sample[] = new double[numSamples];
		byte generatedSnd[] = new byte[2 * numSamples];
		
		// sanity check volume
		if (volume < 0.0) {
			volume = 0.0;
		} else if (volume > 1.0) {
			volume = 1.0;
		}
		
		int idx = 0;
		
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequency));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767 * volume));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
        
        return generatedSnd;
    }
}
