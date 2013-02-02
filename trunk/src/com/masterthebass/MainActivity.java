package com.masterthebass;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Display;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity implements OnGestureListener, SensorEventListener {
	// Manager instances
	private AudioOutputManager audioman;
	private SoundManager soundman;
	private FileManager fileman;
	private FilterManager filterman;
	
	// Flag to indicate startup has completed
	private boolean resumeHasRun = false;
	
	// Sensor variables
	private float totalAccel, prevTotalAccel;
	private long timeA, timeB;
	private boolean useTimeA = true;
	
	private SensorEvent calibrate;
	
	private float mSensorX, mSensorY, mSensorZ, oSensorX; 
	private float mLastX, mLastY, mLastZ, oLastX;
	
	private float calx;
	private float caly;
	private float calz;
	
	private boolean isNegative, lIsNegative;
	private boolean oSensorErrorLogged, mSensorErrorLogged;
	private boolean writing = false;
	
	private WindowManager mWindowManager;
	private Display mDisplay;
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private SensorManager oSensorManager;
	private Sensor oSensor;
	
	private int i, resetThreshold, resetCounter;
	
	private int movingAverageCount;
	private float[] gradMovingAverage;
	
	// Audio generation variables
	private Thread toneGeneratorThread, playThread;
	private boolean tone_stop = true;
	private double base = 50;
	private double vol = 1.0;
	private double dur = 0.01;
	
	// Log output tag
	private final static String LogTag = "Main";
	
	/** Private helper methods */
	   
   	private void instantiate() {
   		audioman 	= new AudioOutputManager();
   		soundman	= new SoundManager();
   		fileman 	= new FileManager();
   		filterman 	= new FilterManager();
   	}
   	
   	private void initSensors () {
   		mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);						//Manages Linear Acceleration sensor
   		//mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);							// TODO - I had to change this to get it to work with the galaxy tab
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);					//
		
		oSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);						//Manages Orientation sensor 
		oSensor = oSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);									// TODO - this is DEPRECATED, use instead (https://developer.android.com/reference/android/hardware/SensorManager.html#getOrientation(float[], float[]))
		oSensorManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_FASTEST);					//
		
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();
		
		oSensorErrorLogged = false;
		mSensorErrorLogged = false;
		
		writing = false;
		
		i = 0;
		resetThreshold = 4;
		resetCounter = 0;
		
		calx = 0;
		caly = 0;
		calz = 0;
		
		prevTotalAccel = 0;
		
		movingAverageCount = 10;
		gradMovingAverage = new float[movingAverageCount];
		
		for (int k = 0; k < movingAverageCount; k++) {
			gradMovingAverage[k] = 0;
		}
   	}
   	
   	/** Activity lifecycle/UI methods */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gestureScanner = new GestureDetector(this,this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
    	getMenuInflater().inflate(R.menu.activity_main, menu);
		Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
        return true;
    }
	
    @Override
    public void onStart() {
    	super.onStart();
    	
    	// Called after onCreate() or onRestart()
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// User or OS has navigated back to Activity
    	
    	if (!resumeHasRun) {
    		// Run on first resume, called directly after onCreate() after loading
    		instantiate();
    		initSensors();
    		
    		// Print some debugging
    		Log.d(LogTag+".onResume", "The filter IDs list is...");
    		int[] IDs = filterman.getFiltersList();
    		
    		for (int i = 0; i < IDs.length; i++) {
    			Log.d(LogTag+".onResume", i + ": " + filterman.getFilterName(IDs[i]) + " has ID #" + IDs[i]);
    		}
    		
    		// Set flag
    		resumeHasRun = true;
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	// Another Activity comes into foreground
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    	// App closed due to lack of memory
    }
    
    @Override
    public void onRestart() {
    	super.onRestart();
    	
    	// User has navigated back to Activity
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// App has been forcibly closed by OS
    }
   
   	/** Button onClick handlers */
    
    /** Called when the user clicks the Settings button */
    public void btnSettingsClick (View view) {
    	Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
    }
    
    public void btnToggleClick (View view) {
    	Button b = (Button) findViewById(R.id.btnToggle);
		
		if (tone_stop) {	
			tone_stop = false;
			
			// Play the audio, when the buffer is ready
			playThread = new Thread(playTone);
			playThread.start();			
			
			// generate a tone
			toneGeneratorThread = new Thread(toneGenerator);
			toneGeneratorThread.start();
			
			// set text
			b.setText(getString(R.string.btnPlay_stop_text));
			
			// set sensor update to true
			writing = true;
		} else {			
			// stop worker threads
			toneGeneratorThread.interrupt();
			playThread.interrupt();
			
			// stop audio
			audioman.stop();
			tone_stop = true;
			
			// set text
			b.setText(getString(R.string.btnPlay_play_text));
			
			// set sensor update to false
			writing = false;
		}
    }
    
    //*********************gesture code****************************
    
    public static final int gestureDelay = 500;
	private GestureDetector gestureScanner;
	private static final String[] gesturearray = new String[]{"NULL","NULL","NULL","NULL"};	
	private static final String[] actionarray = new String[]{"NULL","NULL","NULL","NULL"};
	
	long lastGesture = System.currentTimeMillis();
	
	public static void addTogestureArray(CharSequence gesture,int gesturenum){
		gesturearray[gesturenum] = (String) gesture;
		Log.i(LogTag,"the gesture is " + gesturearray[0]);
		Log.i(LogTag,"the gesture is " + gesturearray[1]);
		Log.i(LogTag,"the gesture is " + gesturearray[2]);
		Log.i(LogTag,"the gesture is " + gesturearray[3]);
	}
	
	public static void addToactionArray(CharSequence action, int actionnum){
		actionarray[actionnum] = (String) action;
		Log.i(LogTag,"the action is " + actionarray[0]);
		Log.i(LogTag,"the action is " + actionarray[1]);
		Log.i(LogTag,"the action is " + actionarray[2]);
		Log.i(LogTag,"the action is " + actionarray[3]);
		
	}   
	
    @Override
	public boolean onTouchEvent(MotionEvent me)	{
		//return false;
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Log.i(LogTag, "Down");		
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		Log.i(LogTag, "Fling");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		for(int i = 0; i<4;i++){
			if(gesturearray[i] == "Hold"){
				Toast toast = Toast.makeText(getApplicationContext(), "Hold", Toast.LENGTH_SHORT);
				toast.show();
			}
		}
		Log.i(LogTag, "Long Press");
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
	
		if ((lastGesture + gestureDelay) < System.currentTimeMillis())
		{
			lastGesture = System.currentTimeMillis();

			if (distanceX < -10)
			{
				for(int i = 0; i<4;i++){
					if(gesturearray[i] == "Swipe Right"){
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Right", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
				Log.i(LogTag,"Swipe Right");
			} else if (distanceX > 10)
			{
				for(int i = 0; i<4;i++){
					if(gesturearray[i] == "Swipe Left"){
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Left", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
				Log.i(LogTag,"Swipe Left");
			}
			if (distanceY < -10)
			{
				for(int i = 0; i<4;i++){
					if(gesturearray[i] == "Swipe Down"){
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Down", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
				Log.i(LogTag,"Swipe Down");
			} else if (distanceY > 10)
			{
				for(int i = 0; i<4;i++){
					if(gesturearray[i] == "Swipe Up"){
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Up", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
				Log.i(LogTag,"Swipe Up");
			}
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		//checking whether it is a real tap or accident
		Log.i(LogTag, "Show press");	
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		for(int i = 0; i<4;i++){
			if(gesturearray[i] == "Tap"){
				Toast toast = Toast.makeText(getApplicationContext(), "Tap", Toast.LENGTH_SHORT);
				toast.show();
			}
		}
		Log.i(LogTag, "Single tap up");
		return false;
	}
	 //start the filter activity
	 public void filters(View view) {
	     Intent intent = new Intent(this, Filtersmenu.class);
	     startActivity(intent);
	    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// Ensure we have sensors!
		if( oSensor == null ) {
			if (!oSensorErrorLogged) {
				Log.w(LogTag, "No orientation sensor.");
				mSensorErrorLogged = true;
			}
			return;
    	}
		
		if( mSensor == null ) {
			if (!mSensorErrorLogged) {
				Log.w(LogTag, "No accelerometer found.");
				mSensorErrorLogged = true;
			}
			return;
		} 
		
		// Process!
	    if (event.sensor.equals(mSensor))
	    {
			Sensor source = event.sensor;
			calibrate = event;
			final float NOISE = (float) 1.0;
			
			switch (mDisplay.getRotation())
			{
		        case Surface.ROTATION_0:
		            mSensorX = -event.values[0];
		            mSensorY = -event.values[1];
		            mSensorZ = -event.values[2];
		            break;
		        case Surface.ROTATION_90:
		            mSensorX = event.values[1];
		            mSensorY = -event.values[0];
		            mSensorZ = event.values[2];
		            break;
		        case Surface.ROTATION_180:
		            mSensorX = event.values[0];
		            mSensorY = event.values[1];
		            mSensorZ = event.values[2];
		            break;
		        case Surface.ROTATION_270:
		            mSensorX = -event.values[1];
		            mSensorY = event.values[0];
		            mSensorZ = -event.values[2];
		            break;
			}
			
			float deltaX = Math.abs(mLastX - mSensorX);
			float deltaY = Math.abs(mLastY - mSensorY);
			float deltaZ = Math.abs(mLastZ - mSensorZ);
			
			if (deltaX < NOISE)
				deltaX = mLastX;
			else
				deltaX = mSensorX;
			if (deltaY < NOISE)
				deltaY = mLastY;
			else
				deltaY = mSensorY;
			if (deltaZ < NOISE)
				deltaZ = mLastZ;
			else
				deltaY = mSensorZ;
			
			totalAccel = (float) Math.sqrt((deltaX - calx) * (deltaX - calx) +
					  (deltaY - caly) * (deltaY - caly) +
					  (deltaZ - calz) * (deltaZ - calz));			
	    } else if (event.sensor.equals(oSensor)) {
	    	oLastX = oSensorX;
	    	
	    	switch (mDisplay.getRotation())
			{
			    case Surface.ROTATION_0:
		            oSensorX = -event.values[0];
		            break;
		        case Surface.ROTATION_90:
		            oSensorX = event.values[1];
		            break;
		        case Surface.ROTATION_180:
		            oSensorX = event.values[0];
		            break;
		        case Surface.ROTATION_270:
		            oSensorX = -event.values[1];
		            break;
			}
	    }
		
	    if(oSensorX < 0) {
	    	isNegative = true;
		} else {
	    	isNegative = false;
	    }
	    
	    if(oLastX < 0) {
	    	lIsNegative = true;
	    } else {
	    	lIsNegative = false;
	    }
	    
	    if (useTimeA) {
			timeA = System.currentTimeMillis() ;
		} else {
			timeB = System.currentTimeMillis() ;
		}
	    
	    if(writing)
		{
	    	long dTime;
	    	int newCutoff = 0;
			
			if (useTimeA) {
				dTime = (timeA - timeB);
			} else {
				dTime = (timeB - timeA);
			}
			
			if (dTime < 1) {
				dTime = 1;
			}
			
			// Add to moving average
			gradMovingAverage[i % movingAverageCount] = (totalAccel - prevTotalAccel)/(dTime);
			i++;
			
			// Get the low-pass filter
            LowPassFilter f = (LowPassFilter) filterman.getFilter (0);
			
	    	if (prevTotalAccel != totalAccel) {	
				float grad = 0;
				
				for (int k = 0; k < movingAverageCount; k++) {
					grad += gradMovingAverage[k];
				}
				
				grad /= movingAverageCount;
	            
				// Set new cutoff frequency
	            newCutoff = (int)(Math.abs(grad)*3000);
			} else {
				resetCounter++;
				
				if (resetCounter == resetThreshold) {
					newCutoff = 0;
					resetCounter = 0;
				}
			}
	    	
	    	// Change the cutoff (shelf) frequency
            f.setCutoffFrequency(newCutoff);
            Log.i(LogTag, "Setting cutoff frequency to : " + newCutoff);
		}
	    
	    prevTotalAccel = totalAccel;
	    useTimeA = !useTimeA;
	}
	
	/** Audio threads **/
	
	Runnable playTone = new Runnable() {
		public void run() {
			Log.d(LogTag+".playTone", "Started!");
			
			while (!audioman.play()) {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				try {
					Thread.sleep(80);
				} catch (InterruptedException e) {
					Log.d(LogTag+".playTone", "Play thread interruped.");
					return;
				}
			}
			
			Log.d(LogTag+".playTone", "Tone playback started.");
			Log.d(LogTag+".playTone", "Shutting down...");
		}
	};
	
	Runnable toneGenerator = new Runnable() {
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
			
			int sampleRate = audioman.getSampleRate();
			int samples = (int) Math.ceil(sampleRate * dur);
            byte [] sampleData = new byte[samples];
            
            Log.d(LogTag+".toneGenerator", "Started!");
            
            // Get the low-pass filter
            LowPassFilter f = (LowPassFilter) filterman.getFilter (0);
            
            while(!tone_stop) {             
            	// generate audio
            	sampleData = soundman.generateToneByte(dur, base, vol, sampleRate);
        		
        		// apply the filter
        		sampleData = f.applyFilter (sampleData);
        		
        		// send to audio buffer
        		audioman.buffer(sampleData);
        		
        		if (Thread.interrupted()) {
					Log.d(LogTag+".toneGenerator", "Tone buffering thread interrupted.");
                	return;
                }
            }
            
            Log.d(LogTag+".toneGenerator", "Shutting down...");
		}
	};
}

