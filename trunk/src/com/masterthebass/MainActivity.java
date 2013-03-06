package com.masterthebass;

import java.math.BigDecimal;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
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
	public final static String FILTERMAN_FILTER_IDS = "com.masterthebass.FILTERMAN_FILTER_IDS";
	public final static String FILTERMAN_FILTER_NAMES = "com.masterthebass.FILTERMAN_FILTER_NAMES";
	
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
	
	private int maxFreq = 5000;
	private int minFreq = 600;
	
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

	private float[] gyroTiltVal;
	private float[] accTiltVal;
	private float tiltCutoff;
	
	private static Handler handler;
	private static final int HANDLER_MESSAGE_BUFFER_FULL = 0;
	private static final int HANDLER_MESSAGE_BUFFER_NOT_INSTANTIATED = 1;

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
 		//tilt        = new TiltCalc(this);
   		//accTiltVal  = new float[3];
   		//gyroTiltVal = new float[3];
   			
   		handler		= new Handler() {
   			@Override
   			public void handleMessage(Message msg) {
   				switch (msg.what) {
   					case HANDLER_MESSAGE_BUFFER_FULL:
   						toggleRecord(null);
   						break;
   					
   					case HANDLER_MESSAGE_BUFFER_NOT_INSTANTIATED:
   						Log.e(LogTag, "Could not instantiate Record Buffer");
   						toggleRecord(null);
   						break;
   					
   					default:
   						Log.w(LogTag, "Unkown handler message received");
   						break;
   				}
   			}
   		};
   	}
   	
   	private void initSensors () {
   		mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);						//Manages Linear Acceleration sensor
   		//mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);							// TODO - I had to change this to get it to work with the galaxy tab
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);					//
		
		oSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);						//Manages Orientation sensor 
		oSensor = oSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);									// TODO - this is DEPRECATED, use instead (https://developer.android.com/reference/android/hardware/SensorManager.html#getOrientation(float[], float[]))
		oSensorManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_FASTEST);					//
		
		
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
		noteFrequency = MidiNote.C2;
		volume = 1.0;
		noteDuration = 0.01;
		maxAmplitude = 1.0;
		minAmplitude = 0.2;
		
		// TODO - these should be the slider values
		maxCutoffFreq = 5000; // = sliderValues[1];
		minCutoffFreq = 150; // = sliderValues[0];

		// Set up low-pass filter
		filterman.enableFilter(0);
		
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
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        fb1 = (ToggleButton)findViewById(R.id.filter1);
        fb2 = (ToggleButton)findViewById(R.id.filter2);
        fb3 = (ToggleButton)findViewById(R.id.filter3);
        fb4 = (ToggleButton)findViewById(R.id.filter4);
        Button record = (Button)findViewById(R.id.buttonrecord);
        Button help = (Button)findViewById(R.id.buttonplay);
        Button settings = (Button)findViewById(R.id.btnSettings);
        scaleLayout(fb1);
        scaleButtons(record);
        scaleButtons(help);	
        scaleButtons(settings);
       
    }
   //scaling play, settings and help buttons
    private void scaleButtons(Button b){
    	ViewGroup.LayoutParams parms = b.getLayoutParams();
    	parms.width = mDisplay.getWidth()/5;
    	parms.height= mDisplay.getWidth()/5;
    	b.setLayoutParams(parms);
    }
    //scaling filter buttons
    private void scaleLayout(ToggleButton b){
    	LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 3*mDisplay.getHeight()/5 - 15, 1);
    	LinearLayout rLGreen = ((LinearLayout) b.getParent());
    	rLGreen.setLayoutParams(parms);
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
    	intent.putExtra(FILTERMAN_FILTER_IDS, filterman.getFiltersList());
    	intent.putExtra(FILTERMAN_FILTER_NAMES, filterman.getFilterNamesList());
    	startActivityForResult(intent,1);
    }
    public void startHelp(View view){
    	Intent intent = new Intent(this, Help.class);
    	startActivity(intent);
    	
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
    View pwView;
    
    private void initiatePopupWindow() {
        try {
            //We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            //Inflate the view from a predefined XML layout
            pwView = inflater.inflate(R.layout.save_popup,
                    (ViewGroup) findViewById(R.id.save_popup));
            
            // create a 300px width and 470px height PopupWindow
            pw = new PopupWindow(pwView, 300, 470, true);
            
            // display the popup in the center
            pw.showAtLocation(pwView, Gravity.CENTER, 0, 0);
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startRecord() {
    	recording = true;
    	
    	if (recordedData == null) {
    		// set max size
    		recordedDataMaxSize = 60 * audioman.getSampleRate();
    		recordedData = ShortBuffer.allocate(recordedDataMaxSize);
    	}
    	
    	recordedData.rewind();
    }
    
    private void stopRecord() {
    	if (recordedData != null) {
	    	initiatePopupWindow();
	    	
	    	// TODO - auto-increment file names
	    	EditText editFileName = (EditText) pwView.findViewById(R.id.editFileName);     	
	    	editFileName.setText(fileman.getPath() + "/audio.PCM");
    	}
    }
    
    public void btnSaveNoClick(View view) {
    	// Clear the buffer
    	recordedData.clear();
    	recording = false;
    	pw.dismiss();
    }
    
    public void btnSaveYesClick(View view) {
    	// Get audio data
    	short[] data = new short[recordedData.position()];
    	recordedData.rewind();
    	recordedData.get(data);
    	
    	// Write it to the file specified
    	EditText editFileName = (EditText) pwView.findViewById(R.id.editFileName);
    	String filename = editFileName.getText().toString();
    	fileman.openFile(filename);
    	fileman.writeBinaryFile(data);
    	
    	// Close the file
    	fileman.closeFile();
    	
    	// Clear the buffer
    	recordedData.clear();
    	recording = false;
    	pw.dismiss();
    }
    
    // For toggling recording of audio
    public void toggleRecord(View view) {
    	
    	Button buttonrecord = (Button) findViewById(R.id.buttonrecord);
    	
    	if (recording) {
    		if (!audioman.isStopped()) {
    			toggleplayonoff();
    		}
    		buttonrecord.setBackgroundResource(R.drawable.rec_button_off);	
    		
    		stopRecord();
    	} else {
    		//record button on
    		buttonrecord.setBackgroundResource(R.drawable.rec_button_on);	
    		startRecord();
    	}
    }
   
    // For toggling play button to start/stop audio
    // TODO - ramp audio volume up/down
    public void toggleplayonoff() {
    	if (audioman.isStopped()) {	
    		startAudio();
		} else {			
			stopAudio();
		}
    }
    
    /*
     * functions that called when one of the
     * filter buttons is pressed
     */
    
    public void filterTopLeft(View view){
    	if(fb1.isChecked()){
    		filterman.enableFilter(filterarray[0]);
    	}else{
    		filterman.disableFilter(filterarray[0]);    		
    	}
    	if (settings[4]) v.vibrate(300);
    	//filter1
    	Log.i(TAG,"clicked 1");
    }
    public void filterTopRight(View view){
    	if(fb2.isChecked()){
    		filterman.enableFilter(filterarray[1]);
    	}else{
    		filterman.disableFilter(filterarray[1]);    		
    	}
    	if (settings[4]) v.vibrate(300);
    	//filter2
    	Log.i(TAG,"clicked 2");
    }
    public void filterBottomLeft(View view){
    	if(fb3.isChecked()){
    		filterman.enableFilter(filterarray[2]);
    	}else{
    		filterman.disableFilter(filterarray[2]);    		
    	}
    	if (settings[4]) v.vibrate(300);
    	//filter3
    	Log.i(TAG,"clicked 3");
    }
    public void filterBottomRight(View view){
    	if(fb4.isChecked()){
    		filterman.enableFilter(filterarray[3]);
    	}else{
    		filterman.disableFilter(filterarray[3]);    		
    	}
    	if (settings[4]) v.vibrate(300);
    	//filter4
    	Log.i(TAG,"clicked 4");
    }
    //*********************gesture code****************************
    
    public static final int gestureDelay = 500;
	private GestureDetector gestureScanner;
	// amount of 0's for the amount of filter names, NEED TO CHANGE
	// TODO - change these to a value not being used by FilterMan
	private static int[] sliderValues = new int[]{0,100};
	private static int longpresson = 0;
	private static int[] filterarray = new int[]{2,3,4,5};
	
	long lastGesture = System.currentTimeMillis();	

	
	public static void addTofilterArray(int filter, int filternum){
		filterarray[filternum] = filter;
		Log.i(LogTag,"the action is " + filterarray[0]);
		Log.i(LogTag,"the action is " + filterarray[1]);
		Log.i(LogTag,"the action is " + filterarray[2]);
		Log.i(LogTag,"the action is " + filterarray[3]);
	}   
	
	
	public static void addSliderValues(int[] a){
		sliderValues = a;
	}
	//detects what button clicked and returns false if none
	private boolean checkFilterButton(int n, float x, float y){
		//TODO: get rid of bullshit factor
		//int bullshitFactor = 100;
		int location[] = new int [2];
		//TODO: change all getX and Y to location
		switch (n){
		case 0:
			fb1.getLocationInWindow(location);
			if ((x > location[0])&&(x < location[0] + fb1.getWidth())&&
		        	(y > location[1])&&(y < location[1] + fb1.getHeight())){
				if ((n != lastButton)||(leftButton == true)){
					fb1.setChecked(!fb1.isChecked());
					filterTopLeft(fb1.getRootView());
					lastButton = n;
				}
				return false;
			}
			break;
		case 1:
			fb2.getLocationInWindow(location);
			Log.d(TAG, "Button X: " + location[0] + " Button Y: " + location[1]);
			if ((x > location[0])&&(x < location[0] + fb2.getWidth())&&
		        	(y > location[1])&&(y < location[1] + fb2.getHeight())){
				Log.d(TAG, "past the conditions");
				if ((n != lastButton)||(leftButton == true)){
					fb2.setChecked(!fb2.isChecked());
					filterTopRight(fb2.getRootView());
					lastButton = n;
				}
				return false;  	
			}
			break;
		case 2:
			fb3.getLocationInWindow(location);
			if ((x > location[0])&&(x < location[0] + fb3.getWidth())&&
		        	(y > location[1])&&(y < location[1] + fb3.getHeight())){
				if ((n != lastButton)||(leftButton == true)){
					fb3.setChecked(!fb3.isChecked());
					filterBottomLeft(fb3.getRootView());
	        		lastButton = n;
				}
	        	return false;
			}
			break;
		case 3:
			fb4.getLocationInWindow(location);
			if ((x > location[0])&&(x < location[0] + fb4.getWidth())&&
		        	(y > location[1])&&(y < location[1] + fb4.getHeight())){
				if ((n != lastButton)||(leftButton == true)){
					fb4.setChecked(!fb4.isChecked());
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
	
	private boolean isInSpeaker(float x, float y){
		int dispX = mDisplay.getWidth()/2;
		int dispY = mDisplay.getHeight()/2 + 30;
		if (Math.sqrt((dispX - x)*(dispX - x)+(dispY-y)*(dispY-y)) > (dispX - 50)){
			return false;
		}
		return true;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		float x = me.getRawX();
    	float y = me.getRawY();
    	//when finger is lifted off screen
    	if (me.getAction() == MotionEvent.ACTION_UP){
    		if (audioman.isPlaying() == true) toggleplayonoff();
    		super.dispatchTouchEvent(me);
    		return true;
    	}
    	//when finger touches the screen
    	if (me.getAction() == MotionEvent.ACTION_DOWN){
    		if (isInSpeaker(x,y)){
    			leftButton = true;
    			toggleplayonoff();
    		}else{
        		super.dispatchTouchEvent(me);
    		}
    		return true;
    	}
    	//if swipe
    	if (!isInSpeaker(x,y)){
    		Log.d(TAG,"hovering over button");
    		int currentButton;
    		// TODO - can we use code that isn't deprecated please?
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
    	} else {
    		leftButton = true;
    	}
	    return true;
	}
	
    @Override
	public boolean onTouchEvent(MotionEvent me)	{
		//return false;
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		float x = e.getRawX();
    	float y = e.getRawY();
    	
		if (settings[4]) v.vibrate(300);
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
		return isNegative;
		//if (settings[4]) v.vibrate(300);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Auto-generated method stub
	}


	//Rounds a float to 'decimalPlace' decimal places
	public static float Round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
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
		
		/*if( mSensor == null ) {
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
	    } else*/ if (event.sensor.equals(oSensor)) {
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
		
	    /*if(oSensorX < 0) {
	    	isNegative = true;
		} else {
	    	isNegative = false;
	    }
	    
	    if(oLastX < 0) {
	    	lIsNegative = true;
	    } else {
	    	lIsNegative = false;
	    }*/
	    
	    oSensorX = Math.abs(oSensorX);
	    double level = oSensorX * 50;
	    
	    //Log.i (LogTag, "oSensorX = "+oSensorX);
	    
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
			
            // TODO - there should be a notion of gravity associated with the cutoff
            // i.e. it should be dependent upon the previous cutoff and the gradient
            /*if (Math.abs(prevTotalAccel - totalAccel) > accelThreshold){	
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
			
			tilt.getAccTilt(accTiltVal);			
			tilt.getGyroTilt(gyroTiltVal);	
			if ( Math.abs(gyroTiltVal[1]) > 0.05) {						//If phone is moving, update tilt value
					tiltCutoff = Math.abs(Round(accTiltVal[1], 3));
			}
			
			newCutoff =  (Math.pow(1.00170489031, (maxFreq - (maxFreq/1.52)*tiltCutoff))+200);
			newAmp = (tiltCutoff*0.65789473684);*/
	    	
	    	// Change the cutoff (shelf) frequency
            //lpf.setCutoffFrequency(newCutoff);
	    	lpf.setCutoffFrequency(level);
	    	Log.i(LogTag, "Setting cutoff frequency to : " + newCutoff);
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
		                	//Log.i (TAG, "Applying filter " + id + " - " + filterman.getFilterName(id));   
		                	sampleData = filterman.applyFilter(id, sampleData);
		                }
			    		
			    		// Send to audio buffer
			            sampleList.add(sampleData);
			            
			            // Add to file buffer if required
			            if (recording) {
			            	if (recordedData != null) {
				            	if (recordedData.hasRemaining()) {
				            		recordedData.put(sampleData);
				            	} else {
				            		handler.sendEmptyMessage(HANDLER_MESSAGE_BUFFER_FULL);
				            	}
			            	} else {
			            		Log.e (LogTag, "Recording was initiated before buffer was instantiated");
			            		handler.sendEmptyMessage(HANDLER_MESSAGE_BUFFER_NOT_INSTANTIATED);
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

