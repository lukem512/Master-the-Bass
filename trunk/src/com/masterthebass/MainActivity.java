package com.masterthebass;

import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Intent;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.PopupWindow;
import android.widget.Toast;

// TODO - ensure audio is still smooth - it seems not to be.

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
	private boolean writing;
	private boolean recording;
	
	private WindowManager mWindowManager;
	private Display mDisplay;
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private SensorManager oSensorManager;
	private Sensor oSensor;
	
	private TiltCalc tilt;
	
	private int i, resetThreshold, resetCounter;
	private double accelThreshold;
	private double maxGrad;
	
	private double maxAmplitude;
	private double minAmplitude;
	
	private int movingAverageCount;
	private double[] gradMovingAverage;
	
	// Audio generation variables
	private Thread generatorThread;
	private Thread writerThread;
	
	private LinkedList<short[]> sampleList;
	private int sampleListMaxSize;
	
	private ShortBuffer recordedData;
	private int recordedDataMaxSize;
	
	private boolean playing = true;
	
	private double noteDuration;
	private double volume;
	private double noteFrequency;
	
	private int maxCutoffFreq;
	private int minCutoffFreq;
	
	private float[] tiltDegree;
	private float[] tiltval;
	private float tiltCutoff;
	

	// Log output tag
	private final static String LogTag = "Main";
	
	/** Private helper methods */
	   
   	private void instantiate() {
   		audioman 	= new AudioOutputManager();
   		soundman	= new SoundManager();
   		fileman 	= new FileManager();
   		filterman 	= new FilterManager();
   		tilt        = new TiltCalc(this);
   		tiltval     = new float[3];
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
		gradMovingAverage = new double[movingAverageCount];
		
		for (int k = 0; k < movingAverageCount; k++) {
			gradMovingAverage[k] = 0;
		}
   	}
   	
   	private void initAudio () {
   		// Set up default values
		noteFrequency = MidiNote.C3;
		volume = 1.0;
		noteDuration = 0.05;
		maxAmplitude = 1.0;
		minAmplitude = 0.2;
		maxCutoffFreq = 5000;
		minCutoffFreq = 150;

		// Set up low-pass filter
		filterman.enableFilter(0);
		
		// Set up amplitude filter
		//filterman.enableFilter(1);
		
		// Run the audio threads
		startAudioThreads();
   	}
   	
   	/** Activity lifecycle/UI methods */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gestureScanner = new GestureDetector(this,this);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        settings = new boolean[5];
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
    	
    	// interrupt audio threads
    	stopAudioThreads();
    }
    
    //start the settings activity
    public void startSettings(View view) {
    	Intent intent = new Intent(this, Filtersmenu.class);
    	intent.putExtra(EXTRA_MESSAGE, settings);
    	//put slider values and settings
    	int[] set = new int[filterarray.length + 2];
    	System.arraycopy(sliderValues, 0, set, 0, sliderValues.length);
    	System.arraycopy(filterarray, 0, set, 2, filterarray.length);
    	Log.d(TAG, "sliderVal: " + sliderValues[0] + " set val: "+set[0]);
    	intent.putExtra(TAG, set);
    	Log.d(LogTag, "Sending FilterArray of size " + filterarray.length);
    	intent.putExtra(FILTERMAN_CLASS, filterman);
    	startActivityForResult(intent,1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 1) {

    		if(resultCode == RESULT_OK){
    			settings = data.getBooleanArrayExtra(Filtersmenu.EXTRA_MESSAGE);
    		//	filterarray = data.getIntArrayExtra(TAG);
    		}

    		if (resultCode == RESULT_CANCELED) {
    			//Write code on no result return 
    		}
    	}
    }
    
    //0 is play background, 1 is pause 
    private int buttonOn = 0;
    
    private void startAudio() {				
		// Start playing!
		audioman.play();
		
		// set flag
		playing = true;
		
		// set sensor update to true
		writing = true;
    }
    
    private void startAudioThreads() {	
    	// Create the buffer
    	if (sampleList == null) {
			sampleList = new LinkedList<short[]>();
			sampleListMaxSize = 8;
    	}
		
		// Start up the threads
		if (writerThread == null) {
			writerThread = new Thread(writerThreadObj, "Writer Thread");
			writerThread.setDaemon(true);
			writerThread.start();
		}

		if (generatorThread == null) {
			generatorThread = new Thread(generatorThreadObj, "Generator Thread");
			generatorThread.setDaemon(true);
			generatorThread.start();
		}
    }
    
    private void stopAudio() {
		// stop audio
		audioman.stop();
		
		// set flag
		playing = false;
		
		// set sensor update to false
		writing = false;
    }
    
    private void stopAudioThreads() {   
    	if (writerThread != null) {
			writerThread.interrupt();
			writerThread = null;
		}

		if (generatorThread != null) {
			generatorThread.interrupt();
			generatorThread = null;
		}
    }
    
    private PopupWindow pw;
    
    private void initiatePopupWindow() {
        try {
            //We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            //Inflate the view from a predefined XML layout
            View layout = inflater.inflate(R.layout.save_popup,
                    (ViewGroup) findViewById(R.id.save_popup));
            
            // create a 300px width and 470px height PopupWindow
            pw = new PopupWindow(layout, 300, 470, true);
            
            // display the popup in the center
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startRecord() {
    	recording = true;
    	fileman.openFile(fileman.getSDPath(), "masterthebass.pcm");
    	
    	if (recordedData == null) {
    		// set max size
    		recordedDataMaxSize = audioman.getSampleRate();
    		recordedData = ShortBuffer.allocate(recordedDataMaxSize);
    	}
    	
    	recordedData.rewind();
    }
    
    private void stopRecord() {
    	initiatePopupWindow();
    }
    
    public void btnSaveNoClick(View view) {
    	// Close the file
    	fileman.closeFile();
    	recordedData.clear();
    	recording = false;
    	pw.dismiss();
    }
    
    public void btnSaveYesClick(View view) {
    	// Write data to file
    	short[] data = new short[recordedData.position()];
    	Log.i (LogTag, "Got " + data.length + " samples to buffer");
    	recordedData.rewind();
    	recordedData.get(data);
    	fileman.writeBinaryFile(data);
    	
    	// Close the file
    	fileman.closeFile();
    	recordedData.clear();
    	recording = false;
    	pw.dismiss();
    }
    
    //for toggling record
    public void toggleRecord(View view) {
    	if (recording) {
    		if (!audioman.isStopped()) {
    			toggleplayonoff(view);
    		}
    		stopRecord();
    	} else {
    		startRecord();
    	}
    }
    
    //for toggling play button to stop
    public void toggleplayonoff(View view) {
    	//add a a call to a function so that it plays and stops****
    	Button buttonplay = (Button) findViewById(R.id.buttonplay); 
    	
    	if(buttonOn == 0 ) {
    		buttonplay.setBackgroundResource(R.drawable.selector_pause);	
    		buttonOn = 1;
    	} else {
    		buttonplay.setBackgroundResource(R.drawable.selector);
    		buttonOn = 0;
    	}  
    	
    	if (audioman.isStopped()) {	
    		startAudio();
		} else {			
			stopAudio();
		}
    }
    
    //*********************gesture code****************************
    
    public static final int gestureDelay = 500;
	private GestureDetector gestureScanner;
	private static final String[] gesturearray = new String[]{"Swipe Up","Swipe Left","Tap","Hold"};	
	// amount of 0's for the amount of filter names, NEED TO CHANGE
	// TODO - change these to a value not being used by FilterMan
	private static int[] filterarray = new int[]{0,0,0,0,0,0};
	private static int[] sliderValues = new int[]{0,100};
	private static int longpresson = 0;
	
	long lastGesture = System.currentTimeMillis();
	
	public static void addTogestureArray(CharSequence gesture,int gesturenum){
		gesturearray[gesturenum] = (String) gesture;
		Log.i(LogTag,"the gesture is " + gesturearray[0]);
		Log.i(LogTag,"the gesture is " + gesturearray[1]);
		Log.i(LogTag,"the gesture is " + gesturearray[2]);
		Log.i(LogTag,"the gesture is " + gesturearray[3]);
	}
	
	public static void addTofilterArray(int filter, int filternum){
		filterarray[filternum] = filter;
		Log.i(LogTag,"the action is " + filterarray[0]);
		Log.i(LogTag,"the action is " + filterarray[1]);
		Log.i(LogTag,"the action is " + filterarray[2]);
		Log.i(LogTag,"the action is " + filterarray[3]);
		
	}   
	
	
	public static void addSliderValues(int[] a){
		sliderValues = a;
		Log.d(TAG,"Left: " + sliderValues[0] + " Right: "+ sliderValues[1]);
	}
	
    @Override
	public boolean onTouchEvent(MotionEvent me)	{
    	if (me.getAction() == MotionEvent.ACTION_UP && longpresson == 1 && settings[3]){
    		filterman.disableFilter(filterarray[3]);		
			gesture4 = false;
			longpresson = 0;
    		
    	}
		//return false;
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		if (settings[4]) v.vibrate(300);
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
	
			//change this depending on whether the toggles are on for this gesture
				Toast toast = Toast.makeText(getApplicationContext(), "Hold", Toast.LENGTH_SHORT);
				toast.show();
				if(settings[3]){
							
					if(gesture4 == false){
						filterman.enableFilter(filterarray[3]);	
						longpresson = 1;
						gesture4 = true;
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
						if (settings[4]) v.vibrate(300);
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Right", Toast.LENGTH_SHORT);
						toast.show();
						if(settings[1]){
							filterman.disableFilter(filterarray[1]);	
							gesture2 = false;
						}
						
				Log.i(LogTag,"Swipe Right");
			} else if (distanceX > 10)
			{					
						if (settings[4]) v.vibrate(300);
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Left", Toast.LENGTH_SHORT);
						toast.show();
						if(settings[1]){
							if(gesture2 == false){
								filterman.enableFilter(filterarray[1]);							
								gesture2 = true;
							}				
						}
				
				Log.i(LogTag,"Swipe Left");
			}
			if (distanceY < -10)
			{
				if (settings[4]) {
					v.vibrate(300);
				}
				
				Toast toast = Toast.makeText(getApplicationContext(), "Swipe Down", Toast.LENGTH_SHORT);
				toast.show();
				
				if(settings[0]){
					filterman.disableFilter(filterarray[0]);							
					gesture1 = false;
				}
					
				Log.i(LogTag,"Swipe Down");
			} else if (distanceY > 10)
			{
				
				if (settings[4]) v.vibrate(300);
				Toast toast = Toast.makeText(getApplicationContext(), "Swipe Up", Toast.LENGTH_SHORT);
				toast.show();
						
				if(settings[0]){
					if(gesture1 == false){
						filterman.enableFilter(filterarray[0]);							
						gesture1 = true;
					}	
				}
			}
			
			Log.i(LogTag,"Swipe Up");
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
		//if (settings[4]) v.vibrate(300);
		
		Toast toast = Toast.makeText(getApplicationContext(), "Tap", Toast.LENGTH_SHORT);
		toast.show();
		
		if(settings[2]){
		
			if(gesture3 == false){
				filterman.enableFilter(filterarray[2]);
				
				gesture3 = true;
			}else{
				filterman.disableFilter(filterarray[2]);		
				gesture3 = false;
			}
		}
				
		Log.i(LogTag, "Single tap up");
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
	    	double newCutoff = maxCutoffFreq;
	    	double newAmp = minAmplitude;
			
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
	    		double grad = 0;	
				
				for (int k = 0; k < movingAverageCount; k++) {
					grad += gradMovingAverage[k];
				}
				
				grad /= movingAverageCount;
				
				if (grad > maxGrad) {
					grad = maxGrad;
				}
	            
				// Calculate new cutoff frequency
				newCutoff = (((Math.abs(grad)*-(maxCutoffFreq/maxGrad)))+maxCutoffFreq+minCutoffFreq);
				
				// Calculate the new amplitude
				double gradAmp = (grad/maxGrad);
				
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
	    	//Log.i(LogTag, "Setting amplitude to : " + newAmp);
		}
	    
	    prevTotalAccel = totalAccel;
	    useTimeA = !useTimeA;
	}
	
	/** Audio threads **/
	
	Runnable writerThreadObj = new Runnable() {
		public void run() {
			boolean running = true;
			short[] sampleData;
			
			Log.i(TAG+".writerThread", "Started!");
			
			while (running) {
				try {
					sampleData = sampleList.removeFirst();
					audioman.buffer(sampleData);
				}
				catch (NoSuchElementException e) {
					//Log.w (TAG+".writerThread", "Sample buffer is empty.");
				}
				
				if (Thread.interrupted()) {
					Log.i(TAG+".writerThread", "Tone buffering thread interrupted.");
					running = false;
	            }
			}
			
			Log.i(TAG+".writerThread", "Shutting down...");
		}
	};

	Runnable generatorThreadObj = new Runnable() {
		public void run() {
			//android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
			
			int sampleRate = audioman.getSampleRate();
            boolean running = true;
            short[] sampleData;
            
            // Generate silence to mix onto
            short[] silence = soundman.generateSilence(noteDuration, sampleRate);
            
            Log.i(TAG+".generatorThread", "Started!");
            
            while (running) {
            	if (playing) {
            		if (sampleList.size() < sampleListMaxSize) {
            			// Generate the tone
            			sampleData = soundman.generateTone(noteDuration, noteFrequency, volume, sampleRate);
            			soundman.commit();
	            	
		            	// Apply filters
		                int[] filterIDs = filterman.getEnabledFiltersList();
		                
		                for (int id : filterIDs) {
		                	Log.i (TAG, "Applying filter " + id + " - " + filterman.getFilterName(id));   
		                	sampleData = filterman.applyFilter(id, sampleData);
		                }
			    		
			    		// Send to audio buffer
			            sampleList.add(sampleData);
			            
			            // Add to file buffer if required
			            if (recording) {
			            	if (recordedData.hasRemaining()) {
			            		recordedData.put(sampleData);
			            	} else {
			            		// TODO - set recording to false and prompt user to save
			            		// THIS CAN'T BE DONE BY CALLING stopRecord() DIRECTLY
			            		// AS THE POPUP CAN'T BE SHOWN BY A THREAD OTHER THAN THE UI
			            	}
			            }
            		}
            	}
            	
            	if (Thread.interrupted()) {
					Log.i(TAG+".generatorThread", "Tone generator thread interrupted.");
					running = false;
	            }
            }
            
            Log.i(TAG+".generatorThread", "Shutting down...");
		}
	};
}

