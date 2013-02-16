package com.masterthebass;

import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.annotation.TargetApi;
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
import android.widget.ToggleButton;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends Activity implements OnGestureListener, SensorEventListener {
	// Manager instances
	private AudioOutputManager audioman;
	private SoundManager soundman;
	private FileManager fileman;
	private FilterManager filterman;
	
	// Flag to indicate startup has completed
	private boolean resumeHasRun = false;
	//filters on\off
	private boolean gesture1 = false;
	private boolean gesture2 = false;
	private boolean gesture3 = false;
	private boolean gesture4 = false;
	//settings on\off
	private boolean[] settings;
	/*  settings:
	 *  0 - 3 are filter on/off buttons 
	 *  4 vibration button
	 */
	//"Swipe Up","Swipe Left","Tap","Hold"
	// filter1     filter2    filter3 filter4
	
	Vibrator v;

	public final static String TAG = "com.masterthebass.FILTERS";
	public final static String EXTRA_MESSAGE = "com.masterthebass.MESSAGE";
	public final static String FILTERMAN_CLASS = "com.masterthebass.FILTERMAN_CLASS";
	
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
	private float accelThreshold;
	private float maxGrad;
	
	private float maxAmplitude;
	private float minAmplitude;
	
	private int movingAverageCount;
	private float[] gradMovingAverage;
	
	// Audio generation variables
	private Thread toneGeneratorThread, playThread;
	private boolean tone_stop = true;
	private double base;
	private double vol;
	private double dur;
	
	private int maxCutoffFreq;
	private int minCutoffFreq;
	
	private ToggleButton fb1;
	private ToggleButton fb2;
	private ToggleButton fb3;
	private ToggleButton fb4;
	private boolean toggleChecked[] = {false,false,false,false};
	private int lastButton = 5;
	private boolean leftButton = true;
	
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
		resetThreshold = 10;
		resetCounter = 0;
		accelThreshold = 0.001f;
		maxGrad = 3f;
		
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
   	
   	private void initAudio () {
		base = 50;
		vol = 1.0;
		dur = 0.01;
		maxAmplitude = 1.0f;
		minAmplitude = 0.2f;
		maxCutoffFreq = 3000;
		minCutoffFreq = 150;
   	}
   	
   	/** Activity lifecycle/UI methods */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gestureScanner = new GestureDetector(this,this);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        settings = new boolean[5];
        fb1 = (ToggleButton)findViewById(R.id.filter1);
        fb2 = (ToggleButton)findViewById(R.id.filter2);
        fb3 = (ToggleButton)findViewById(R.id.filter3);
        fb4 = (ToggleButton)findViewById(R.id.filter4);
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
    		initAudio();
    		
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
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    }
    
    @Override
    public void onRestart() {
    	super.onRestart();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    //start the settings activity
    public void startSettings(View view) {
    	Intent intent = new Intent(this, Filtersmenu.class);
    	intent.putExtra(EXTRA_MESSAGE, settings);
    	intent.putExtra(TAG, filterarray);
    	Log.d(LogTag, "Sending FilterArray of size " + filterarray.length);
    	intent.putExtra(FILTERMAN_CLASS, filterman);
    	startActivityForResult(intent,1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 1) {

    		if(resultCode == RESULT_OK){
    			settings = data.getBooleanArrayExtra(Filtersmenu.EXTRA_MESSAGE);
    			filterarray = data.getIntArrayExtra(TAG);
    			Log.i(TAG,"Left: " + filterarray[4] + " Right: "+ filterarray[5]);
    		}

    		if (resultCode == RESULT_CANCELED) {
    			//Write code on no result return 
    		}
    	}
    }
    
    //0 is play background, 1 is pause 
    private int buttonOn = 0;
    
    //for toggling play button to stop
    public void toggleplayonoff(View view){
    	
    	//add a a call to a function so that it plays and stops****
    	Button buttonplay = (Button) findViewById(R.id.buttonplay); 
    	
    	if(buttonOn == 0 ){

    		buttonplay.setBackgroundResource(R.drawable.selector_pause);	
    		buttonOn = 1;
		
    	}else{
    		buttonplay.setBackgroundResource(R.drawable.selector);
    		buttonOn = 0;
    	}  
    	
    	if (tone_stop) {	
			tone_stop = false;
			
			// Play the audio, when the buffer is ready
			playThread = new Thread(playTone);
			playThread.start();			
			
			// generate a tone
			toneGeneratorThread = new Thread(toneGenerator);
			toneGeneratorThread.start();
			
			// set sensor update to true
			writing = true;
		} else {			
			// stop worker threads
			toneGeneratorThread.interrupt();
			playThread.interrupt();
			
			// stop audio
			audioman.stop();
			tone_stop = true;
			
			// set sensor update to false
			writing = false;
		}
    }
    
    /*
     * functions that called when one of the
     * filter buttons is pressed
     */
    
    public void filterTopLeft(View view){
    	//filter1
    	Log.i(TAG,"clicked 1");
    }
    public void filterTopRight(View view){
    	//filter2
    	Log.i(TAG,"clicked 2");
    }
    public void filterBottomLeft(View view){
    	//filter3
    	Log.i(TAG,"clicked 3");
    }
    public void filterBottomRight(View view){
    	//filter4
    	Log.i(TAG,"clicked 4");
    }
    //*********************gesture code****************************
    
    public static final int gestureDelay = 500;
	private GestureDetector gestureScanner;
	// amount of 0's for the amount of filter names, NEED TO CHANGE
	// TODO - change these to a value not being used by FilterMan
	private static int[] filterarray = new int[]{0,0,0,0,0,0};
	private static int longpresson = 0;
	
	long lastGesture = System.currentTimeMillis();	

	
	public static void addTofilterArray(int filter, int filternum){
		filterarray[filternum] = filter;
		Log.i(LogTag,"the action is " + filterarray[0]);
		Log.i(LogTag,"the action is " + filterarray[1]);
		Log.i(LogTag,"the action is " + filterarray[2]);
		Log.i(LogTag,"the action is " + filterarray[3]);
		
	}   
	
	
	//detects what button clicked and returns false if none
	private boolean checkFilterButton(int n, float x, float y){
		int bullshitFactor = 100;
		switch (n){
		case 0:
			if ((x > fb1.getX())&&(x < fb1.getX() + fb1.getWidth())&&
		        	(y > fb1.getY() + bullshitFactor)&&(y < fb1.getY() + fb1.getHeight() + bullshitFactor)){
				if ((n != lastButton)||(leftButton == true)){
			    	fb1.setChecked(!toggleChecked[0]);
			    	toggleChecked[0] = !toggleChecked[0];
					filterTopLeft(fb1.getRootView());
					lastButton = n;
				}
				return false;
			}
			break;
		case 1:
			if ((x > fb2.getX())&&(x < fb2.getX() + fb2.getWidth())&&
		        	(y > fb2.getY() + bullshitFactor)&&(y < fb2.getY() + fb2.getHeight() + bullshitFactor)){
				if ((n != lastButton)||(leftButton == true)){
			    	fb2.setChecked(!toggleChecked[1]);
			    	toggleChecked[1] = !toggleChecked[1];
					filterTopRight(fb2.getRootView());
					lastButton = n;
				}
				return false;  	
			}
			break;
		case 2:
			if ((x > fb3.getX())&&(x < fb3.getX() + fb3.getWidth())&&
		        	(y > fb3.getY() + bullshitFactor)&&(y < fb3.getY() + fb3.getHeight() + bullshitFactor)){
				if ((n != lastButton)||(leftButton == true)){
			    	fb3.setChecked(!toggleChecked[2]);
			    	toggleChecked[2] = !toggleChecked[2];
					filterBottomLeft(fb3.getRootView());
	        		lastButton = n;
				}
	        	return false;
			}
			break;
		case 3:
			if ((x > fb4.getX())&&(x < fb4.getX() + fb4.getWidth())&&
		        	(y > fb4.getY() + bullshitFactor)&&(y < fb4.getY() + fb4.getHeight() + bullshitFactor)){
				if ((n != lastButton)||(leftButton == true)){
			    	fb4.setChecked(!toggleChecked[3]);
			    	toggleChecked[3] = !toggleChecked[3];
					filterBottomRight(fb4.getRootView());
	        		lastButton = n;
				}
	        	return false;
			}
			break;
		default:
			break;
		}
		return true;
	}
	
    @Override
	public boolean onTouchEvent(MotionEvent me)	{
    	if (me.getAction() == MotionEvent.ACTION_UP && longpresson == 1 && settings[3]){
    		filterman.disableFilter(filterarray[3]);		
			gesture4 = false;
			longpresson = 0;
    		
    	}
    	
    	float x = me.getRawX();
    	float y = me.getRawY();
    	int currentButton;
    	if (x < mDisplay.getWidth()/2){
    		//upper left
    		if (y < mDisplay.getHeight()/2) currentButton = 0;
    		//lower left
    		else currentButton = 2;
    	} else {
    		//upper right
    		if (y < mDisplay.getHeight()/2) currentButton = 1;
    		//lower right
    		else currentButton = 3;
    	}
    	leftButton = checkFilterButton(currentButton,x,y);
    	
		//return false;
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		float x = e.getRawX();
    	float y = e.getRawY();
    	
		if (settings[4]) v.vibrate(300);
		//to start playing the bass line
		//if(x > fb1.getX() + fb1.getWidth() && ){}
		Log.i(LogTag, "Down");		
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		//checking whether it is a real tap or accident	
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		
		
		return false;
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
	    	int newCutoff = maxCutoffFreq;
	    	float newAmp = minAmplitude;
			
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
            LowPassFilter lpf = (LowPassFilter) filterman.getFilter (0);
            AmplitudeFilter af = (AmplitudeFilter) filterman.getFilter (1); 
			
            // TODO - there should be a notion of gravity associated with the cutoff
            // i.e. it should be dependent upon the previous cutoff and the gradient
	    	if (Math.abs(prevTotalAccel - totalAccel) > accelThreshold){	
				float grad = 0;	
				
				for (int k = 0; k < movingAverageCount; k++) {
					grad += gradMovingAverage[k];
				}
				
				grad /= movingAverageCount;
				
				if (grad > maxGrad) {
					grad = maxGrad;
				}
	            
				// Calculate new cutoff frequency
				newCutoff = ((int)((Math.abs(grad)*-(maxCutoffFreq/maxGrad)))+maxCutoffFreq+minCutoffFreq);
				
				// Calculate the new amplitude
				float gradAmp = (grad/maxGrad);
				
				if (gradAmp > minAmplitude) {
					if (gradAmp < maxAmplitude) {
						newAmp = Math.abs(gradAmp);
					} else {
						newAmp = maxAmplitude;
					}
				} else {
					newAmp = minAmplitude;
				}
				
				if (newCutoff < minCutoffFreq) {
					newCutoff = minCutoffFreq;
				}
			} else {
				resetCounter++;
				
				if (resetCounter == resetThreshold) {
					newCutoff = maxCutoffFreq;
					newAmp = minAmplitude;
					resetCounter = 0;
				} else {
					newCutoff = lpf.getCutoffFrequency();
					newAmp = af.getAmplitude();
				}
			}
	    	
	    	// Change the cutoff (shelf) frequency
            lpf.setCutoffFrequency(newCutoff);
	    	Log.i(LogTag, "Setting cutoff frequency to : " + newCutoff);
	    	
	    	// Change the volume
	    	af.setAmplitude (newAmp);
	    	Log.i(LogTag, "Setting amplitude to : " + newAmp);
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
            short [] sampleData = new short[samples];
            
            Log.d(LogTag+".toneGenerator", "Started!");
            
            // Get the low-pass filter
            LowPassFilter lpf = (LowPassFilter) filterman.getFilter (0);
            
            // ...and the Amplitude filter
            AmplitudeFilter af = (AmplitudeFilter) filterman.getFilter (1);
            
            while(!tone_stop) {             
            	// generate audio
            	sampleData = soundman.generateToneShort(dur, base, vol, sampleRate);
        		
            	// apply the user-defined filters
        		for (int i = 0; i < 4; i++) {
        			int id = filterarray[i];
        			Filter filter = filterman.getFilter(id);
        			
        			if (filter.getState()) {
        				sampleData = filter.applyFilter(sampleData);
        				Log.d (LogTag, "Applying filter #" + id + " : " + filter.getName());
        			}
        		}
            	
        		// apply the filter for the 'wub' noise
        		sampleData = lpf.applyFilter (sampleData);
        		
        		// apply the filter to change the volume
        		sampleData = af.applyFilter (sampleData);
        		
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

