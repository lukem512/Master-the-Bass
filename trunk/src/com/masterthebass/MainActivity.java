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
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class MainActivity extends Activity implements SensorEventListener {
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
	 *  5 calibration
	 */
	
	Vibrator v;

	public final static String TAG = "com.masterthebass.FILTERS";
	public final static String EXTRA_MESSAGE = "com.masterthebass.MESSAGE";
	public final static String FILTERMAN_FILTER_NAMES = "com.masterthebass.FILTERMAN_FILTER_NAMES";
	public final static int NUM_SETTINGS = 6;

	// Sensor variables
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private boolean calibrating = false, calibrated = false;
    private float con = 180/(float)Math.PI;
    private float det;

    private float[] mR = new float[9];
    private float[] mRCal = new float[9];
    private float[] mRNew = new float[9];
    private float[] mRInv = new float[9];
    private float[] mOrientation = new float[3];
	
	private boolean writing;
	private boolean recording;
	
	private WindowManager mWindowManager;
	private Display mDisplay;
	private Point screenSize;
	private Point screenQuadrantBoundary;
	
	private SensorManager mSensorManager;
	
	private int movingAverageCount;
	private double[] gradMovingAverage;
	
	// Audio generation variables
	private Thread generatorThread;
	private Thread writerThread;
	
	private LinkedList<short[]> sampleList;
	private int sampleListMaxSize;
	
	private ShortBuffer recordedData;
	private int recordedDataMaxSize;
	
	private double noteDuration;
	private double volume;
	private double noteFrequency;
	
	private double maxCutoffFreq;
	private double minCutoffFreq;

	private static Handler handler;
	private static final int HANDLER_MESSAGE_BUFFER_FULL = 0;
	private static final int HANDLER_MESSAGE_BUFFER_NOT_INSTANTIATED = 1;

	private ToggleButton fb1;
	private ToggleButton fb2;
	private ToggleButton fb3;
	private ToggleButton fb4;
	private int lastButton = 5;
	private boolean leftButton = true;
	private Button speaker;
	
	// Log output tag
	private final static String LogTag = "Main";
	
	/** Private helper methods */
	   
   	@SuppressLint("HandlerLeak")
	private void instantiate() {
   		audioman 	= new AudioOutputManager();
   		soundman	= new SoundManager();
   		fileman 	= new FileManager();
   		filterman 	= new FilterManager();
   		soundman.setWave(new HarmonicSquareWave());
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
   		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
		
		writing = false;
		
		movingAverageCount = 10;
		gradMovingAverage = new double[movingAverageCount];
		
		for (int k = 0; k < movingAverageCount; k++) {
			gradMovingAverage[k] = 0;
		}
   	}
   	
	private void setLowPassFilterCutoffFrequencies() {
   		minCutoffFreq = sliderValues[0];
   		maxCutoffFreq = sliderValues[1];
   		
   		LowPassFilter lpf = (LowPassFilter) filterman.getFilter(0);
   		lpf.setMinCutoffFrequency(minCutoffFreq);
   		lpf.setMaxCutoffFrequency(maxCutoffFreq);
   	}
   	
   	private void initLowPassFilter() {
   		filterman.enableFilter(0);
   		setLowPassFilterCutoffFrequencies();
   	}
   	
   	private void initAudio () {
   		// Set up default values
		noteFrequency = MidiNote.C2;
		volume = 1.0;
		noteDuration = 0.01;
		
		// Set up low-pass filter
		initLowPassFilter();
		
		// Run the audio threads
		startAudioThreads();
   	}
   	
   	/** Activity lifecycle/UI methods */
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        settings = new boolean[NUM_SETTINGS];
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        
        screenSize = new Point();
        screenQuadrantBoundary = new Point();
        
        // TODO - use non-deprecated code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
        	mDisplay.getSize(screenSize);
        } else {
        	screenSize.x = mDisplay.getWidth();
    	  	screenSize.y = mDisplay.getHeight();
        }
        screenQuadrantBoundary.x = screenSize.x/2;
        screenQuadrantBoundary.y = screenSize.y/2;
        
        initiateLayout();   
    }
    //scaling layout for different displays
    private void initiateLayout(){
    	fb1 = (ToggleButton)findViewById(R.id.filter1);
        fb2 = (ToggleButton)findViewById(R.id.filter2);
        fb3 = (ToggleButton)findViewById(R.id.filter3);
        fb4 = (ToggleButton)findViewById(R.id.filter4);
        speaker = (Button)findViewById(R.id.buttonspeaker);
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
    	parms.width = screenSize.x/5;
    	parms.height= screenSize.x/5;
    	b.setLayoutParams(parms);
    }
    //scaling filter buttons and speaker
    private void scaleLayout(ToggleButton b){
    	LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 3*screenSize.y/5 - 15, 1);
    	LinearLayout rLGreen = ((LinearLayout) b.getParent());
    	rLGreen.setLayoutParams(parms);
    	//scaling speaker
    	ViewGroup.LayoutParams parm = speaker.getLayoutParams();
    	parm.width = (int)(screenSize.x/1.5);
    	parm.height = parm.width;
    	speaker.setLayoutParams(parm);
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
    	Bundle bundle = new Bundle();
    	bundle.putBooleanArray("settings", settings);
    	bundle.putIntArray("sliderValues", sliderValues);
    	bundle.putIntArray("filters", filterarray);
    	bundle.putStringArray("FILTERMAN_FILTER_NAMES", filterman.getFilterNamesList());
    	intent.putExtras(bundle);
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
    			sliderValues = data.getIntArrayExtra(TAG);
    			setLowPassFilterCutoffFrequencies();
    			// TODO - clean this up.
    			if (settings[5] == true) {
    				calibrating = true;
    			}
    		}

    		if (resultCode == RESULT_CANCELED) {
    			//Write code on no result return 
    		}
    	}
    }
    
    
    private void startAudio() {				
		// Start playing!
		audioman.play();
		
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
            // We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            // Inflate the view from a predefined XML layout
            pwView = inflater.inflate(R.layout.save_popup,
                    (ViewGroup) findViewById(R.id.save_popup));
            
            // Hack to stop the app crashing when text is deleted
            EditText editFileName = (EditText) pwView.findViewById(R.id.editFileName);
            editFileName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            
            // Create a 300px width and 470px height PopupWindow
            pw = new PopupWindow(pwView, 300, 470, true);
            
            // Display the popup in the center
            pw.showAtLocation(pwView, Gravity.CENTER, 0, 0);
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startRecord() {    	
    	// Set up buffer
    	if (recordedData == null) {
    		// set max size
    		recordedDataMaxSize = 60 * audioman.getSampleRate();
    		recordedData = ShortBuffer.allocate(recordedDataMaxSize);
    	}
    	recordedData.rewind();
    	
    	// Set flag to true
    	recording = true;
    }
    
    private void stopRecord() {
    	if (recordedData != null) {
    		// Set flag to false
        	// This will stop generating silence!
        	recording = false;
    		
	    	initiatePopupWindow();
	    	
	    	// TODO - auto-increment file names
	    	EditText editFileName = (EditText) pwView.findViewById(R.id.editFileName);     	
	    	editFileName.setText("audio.PCM");
    	}
    }
    
    public void btnSaveNoClick(View view) {    	
    	// Clear the buffer
    	recordedData.clear();
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
    	fileman.openFile(fileman.getPath(),filename);
    	fileman.writeBinaryFile(data);
    	
    	// Close the file
    	fileman.closeFile();
    	
    	// Clear the buffer
    	recordedData.clear();
    	pw.dismiss();
    }
    
    // For toggling recording of audio
    public void toggleRecord(View view) {
    	
    	Button buttonrecord = (Button) findViewById(R.id.buttonrecord);
    	
    	if (recording) {
    		buttonrecord.setBackgroundResource(R.drawable.rec_button_off);	
    		stopRecord();
    	} else {
    		//record button on
    		buttonrecord.setBackgroundResource(R.drawable.rec_button_on);	
    		startRecord();
    	}
    }
   
    // For toggling play button to start/stop audio
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
    }
    public void filterTopRight(View view){
    	if(fb2.isChecked()){
    		filterman.enableFilter(filterarray[1]);
    	}else{
    		filterman.disableFilter(filterarray[1]);    		
    	}
    	if (settings[4]) v.vibrate(300);
    }
    public void filterBottomLeft(View view){
    	if(fb3.isChecked()){
    		filterman.enableFilter(filterarray[2]);
    	}else{
    		filterman.disableFilter(filterarray[2]);    		
    	}
    	if (settings[4]) v.vibrate(300);
    }
    public void filterBottomRight(View view){
    	if(fb4.isChecked()){
    		filterman.enableFilter(filterarray[3]);
    	}else{
    		filterman.disableFilter(filterarray[3]);    		
    	}
    	if (settings[4]) v.vibrate(300);
    }
    //*********************gesture code****************************
    
    private static final int defaultMinSliderValue = 0;
	private static final int defaultMaxSliderValue = 5000;
	private static int[] sliderValues = new int[]{defaultMinSliderValue,defaultMaxSliderValue};
	private static int[] filterarray = new int[]{1,3,4,5};
	
	public static void addTofilterArray(int filter, int filternum){
		filterarray[filternum] = filter;
	}   
	
	public static void addSliderValues(int[] a){
		sliderValues = a;
	}
	
	//detects what button clicked and returns false if none
	private boolean checkFilterButton(int n, float x, float y){
		int location[] = new int [2];
		
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
				if ((x > location[0])&&(x < location[0] + fb2.getWidth())&&
			        	(y > location[1])&&(y < location[1] + fb2.getHeight())){
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
		int dispX = screenQuadrantBoundary.x;
		int dispY = screenQuadrantBoundary.y;
		if (Math.sqrt((dispX - x)*(dispX - x)+(dispY-y)*(dispY-y)) > (speaker.getLayoutParams().width/2)){
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
    		int currentButton;
    		if (x < screenQuadrantBoundary.x){
    			//upper left
    			if (y < screenQuadrantBoundary.y) currentButton = 0;
    			//lower left
    			else currentButton = 2;
    		} else {
    			//upper right
    			if (y < screenQuadrantBoundary.y) currentButton = 1;
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
		if(writing)
		{
	    	if (event.sensor == mAccelerometer) {
	            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
	            mLastAccelerometerSet = true;
	        } else if (event.sensor == mMagnetometer) {
	            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
	            mLastMagnetometerSet = true;
	        }
	        if (mLastAccelerometerSet && mLastMagnetometerSet) {
	        	if( calibrating )
	        	{
	    			SensorManager.getRotationMatrix(mRCal, null, mLastAccelerometer, mLastMagnetometer);
	        		SensorManager.getOrientation(mRCal, mOrientation);
	        		det = 	 (mRCal[0]*(mRCal[4]*mRCal[8]-mRCal[5]*mRCal[7]))
	        				-(mRCal[3]*(mRCal[1]*mRCal[8]-mRCal[2]*mRCal[7]))
	        				+(mRCal[6]*(mRCal[1]*mRCal[5]-mRCal[2]*mRCal[4]));
	        		mRInv[0] = (1/det)*(mRCal[4]*mRCal[8]-mRCal[7]*mRCal[5]); 	 mRInv[1] = (-1)*(1/det)*(mRCal[1]*mRCal[8]-mRCal[7]*mRCal[2]); mRInv[2] = (1/det)*(mRCal[1]*mRCal[5]-mRCal[4]*mRCal[2]);
	        		mRInv[3] = (-1)*(1/det)*(mRCal[3]*mRCal[8]-mRCal[6]*mRCal[5]); mRInv[4] = (1/det)*(mRCal[0]*mRCal[8]-mRCal[6]*mRCal[2]); 	  mRInv[5] = (-1)*(1/det)*(mRCal[0]*mRCal[5]-mRCal[3]*mRCal[2]);
	        		mRInv[6] = (1/det)*(mRCal[3]*mRCal[7]-mRCal[6]*mRCal[4]); 	 mRInv[7] = (-1)*(1/det)*(mRCal[0]*mRCal[7]-mRCal[6]*mRCal[1]); mRInv[8] = (1/det)*(mRCal[0]*mRCal[4]-mRCal[3]*mRCal[1]);
	        		
	        		// Set flags
	        		calibrating = false;
	        		calibrated = true;
	        		
	        		// Indicate calibration complete!
	        		Toast toast = Toast.makeText(getApplicationContext(), "Calibration successful!", Toast.LENGTH_SHORT);
	        		toast.show();
	        	}
	        	else
	        	{
	        		if(calibrated)
	        		{
	        		SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
	        		mRNew[0] = mRInv[0]*mR[0]+mRInv[1]*mR[3]+mRInv[2]*mR[6]; mRNew[1] = mRInv[0]*mR[1]+mRInv[1]*mR[4]+mRInv[2]*mR[7]; mRNew[2] = mRInv[0]*mR[2]+mRInv[1]*mR[5]+mRInv[2]*mR[8];
	        		mRNew[3] = mRInv[3]*mR[0]+mRInv[4]*mR[3]+mRInv[5]*mR[6]; mRNew[4] = mRInv[3]*mR[1]+mRInv[4]*mR[4]+mRInv[5]*mR[7]; mRNew[5] = mRInv[3]*mR[2]+mRInv[4]*mR[5]+mRInv[5]*mR[8];
	        		mRNew[6] = mRInv[6]*mR[0]+mRInv[7]*mR[3]+mRInv[8]*mR[6]; mRNew[7] = mRInv[6]*mR[1]+mRInv[7]*mR[4]+mRInv[8]*mR[7]; mRNew[8] = mRInv[6]*mR[2]+mRInv[7]*mR[5]+mRInv[8]*mR[8];
		            SensorManager.getOrientation(mRNew, mOrientation);
	        		}
	        		else
	        		{
	        			SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
			            SensorManager.getOrientation(mR, mOrientation);
	        		}
	        	}
	        }
	    	
	        // Compute the new cutoff frequency
	        // The sensor returns a value between 0 and 90
	    	double newCutoff = minCutoffFreq + Math.abs(mOrientation[1]*con) * ((maxCutoffFreq - minCutoffFreq)/90);
	    	
	    	// Get the left-right orientation
	    	// This is a value between -180 and 180
	    	// TODO - change frequency based upon this value?
	    	double setFreq = (mOrientation[2]*con);
	    	//Log.i(LogTag, "Setting frequency to : " + setFreq); 
			
			// Get the low-pass filter
            LowPassFilter lpf = (LowPassFilter) filterman.getFilter (0);
	    	
	    	// Change the cutoff (shelf) frequency
	    	lpf.setCutoffFrequency(newCutoff);
	    	//Log.i(LogTag, "Setting cutoff frequency to : " + newCutoff);
		}
	}
	
	/** Audio threads **/
	
	Runnable writerThreadObj = new Runnable() {
		public void run() {
			boolean running = true;
			short[] sampleData;

			while (running) {
				try {
					sampleData = sampleList.removeFirst();
					audioman.buffer(sampleData);
				}
				catch (NoSuchElementException e) {
					// Do nothing
				}
				
				if (Thread.interrupted()) {
					running = false;
	            }
			}
		}
	};

	Runnable generatorThreadObj = new Runnable() {
		public void run() {
			int sampleRate = audioman.getSampleRate();
            boolean running = true;
            boolean skipRecord;
            short[] sampleData, silenceData;
            
            silenceData = new short[1];
            silenceData[0] = SoundManager.SILENCE;
            
            while (running) {
            	// Set sample to silence
            	sampleData = silenceData;
            	
            	// Set skip record flag
            	skipRecord = false;
            	
            	// Generate audio
            	if (audioman.isPlaying()) {
            		if (sampleList.size() < sampleListMaxSize) {
            			// Generate the tone
            			sampleData = soundman.generateTone(noteDuration, noteFrequency, volume, sampleRate);
            			soundman.commit();
	            	
		            	// Apply filters
		                int[] filterIDs = filterman.getEnabledFiltersList();
		                
		                for (int id : filterIDs) {  
		                	sampleData = filterman.applyFilter(id, sampleData);
		                }
			    		
			    		// Send to audio buffer
			            sampleList.add(sampleData);
            		} else {
            			// Don't add silence
            			skipRecord = true;
            		}
            	}
            	
            	// Add to file buffer if required
	            if (recording && !skipRecord) {
	            	if (recordedData != null) {
		            	if ((recordedData.position() + sampleData.length) < recordedData.limit()) {
		            		recordedData.put(sampleData);
		            	} else {
		            		handler.sendEmptyMessage(HANDLER_MESSAGE_BUFFER_FULL);
		            	}
	            	} else {
	            		Log.e (LogTag, "Recording was initiated before buffer was instantiated");
	            		handler.sendEmptyMessage(HANDLER_MESSAGE_BUFFER_NOT_INSTANTIATED);
	            	}
	            }
            	
            	if (Thread.interrupted()) {
					running = false;
	            }
            }
		}
	};
}

