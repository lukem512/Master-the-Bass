package com.masterbass;

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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v4.view.MotionEventCompat;

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class MainActivity extends Activity implements SensorEventListener,OnSeekBarChangeListener {
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

	public final static String TAG = "com.masterbass.FILTERS";
	public final static String EXTRA_MESSAGE = "com.masterbass.MESSAGE";
	public final static String FILTERMAN_FILTER_NAMES = "com.masterbass.FILTERMAN_FILTER_NAMES";
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
	
	// Audio generation variables
	private Thread generatorThread;
	private Thread writerThread;
	
	private LinkedList<short[]> sampleList;
	private int sampleListMaxSize;
	
	private ShortBuffer recordedData;
	private int recordedDataMaxSize;
	
	private double noteDuration;
	private double volume;
	private int octaveNumber;
	private int noteNumber;
	
	private double maxCutoffFreq;
	private double minCutoffFreq;

	
	
	private static Handler handler;
	private static final int HANDLER_MESSAGE_BUFFER_FULL = 0;
	private static final int HANDLER_MESSAGE_BUFFER_NOT_INSTANTIATED = 1;

	private Button speaker;
	private ToggleButton fb1;
	private ToggleButton fb2;
	private ToggleButton fb3;
	private ToggleButton fb4;
	private int lastButton = 5;
	private boolean leftButton = true;
	
	private Wave[] waves;
	
	private int topbarEnd;
	private int bottombarStart;
	
	// Log output tag
	private final static String LogTag = "Main";
	
	/** Private helper methods */
	   
   	@SuppressLint("HandlerLeak")
	private void instantiate() {
   		
   		audioman 	= new AudioOutputManager();
   		soundman	= new SoundManager();
   		fileman 	= new FileManager();
   		filterman 	= new FilterManager();
   		soundman.setWave(new SineWave());
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
   						Log.w(LogTag, "Unknown handler message received");
   						break;
   				}
   			}
   		};
   	}
   	
	private void initialiseWavesArray() {
		waves = new Wave[6];
		
		waves[0] = new SineWave();
		waves[1] = new SquareWave();
		waves[2] = new HarmonicSquareWave();
		waves[3] = new TriangleWave();
		waves[4] = new RisingSawToothWave();
		waves[5] = new FallingSawToothWave();
	}
   	
   	private void initSensors () {
   		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
		
		writing = false;
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
   		octaveNumber = 24;
		noteNumber = 0;
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
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
        	mDisplay.getSize(screenSize);
        } else {
        	screenSize.x = mDisplay.getWidth();
    	  	screenSize.y = mDisplay.getHeight();
        }
        screenQuadrantBoundary.x = screenSize.x/2;
        screenQuadrantBoundary.y = screenSize.y/2;
        SeekBar bar = (SeekBar)findViewById(R.id.seekBarFrequency); // make seekbar object
        bar.setOnSeekBarChangeListener(this); // set seekbar listener.
        bar.setMax(12); // 12 semi-tones / octave
        bar.setProgress(0); // set to initially be note 24\
        
        initiateLayout(); 
        initialiseWavesArray();
    }
    //scaling layout for different displays
    private void initiateLayout(){
    	fb1 = (ToggleButton)findViewById(R.id.filter1);
        fb2 = (ToggleButton)findViewById(R.id.filter2);
        fb3 = (ToggleButton)findViewById(R.id.filter3);
        fb4 = (ToggleButton)findViewById(R.id.filter4);
        Button record = (Button)findViewById(R.id.buttonrecord);
        Button help = (Button)findViewById(R.id.buttonplay);
        Button settings = (Button)findViewById(R.id.btnSettings);
        speaker = (Button)findViewById(R.id.speaker);
        Button octaveup = (Button)findViewById(R.id.octaveup);
        Button octavedown = (Button)findViewById(R.id.octavedown);
        SeekBar seek = (SeekBar)findViewById(R.id.seekBarFrequency);
        Button wavef = (Button)findViewById(R.id.btnWave);
      //  wavef.setText("Sine Wave");

        

        RelativeLayout.LayoutParams parms,lparms,bparms;
        
        //filters
        LinearLayout l = (LinearLayout)findViewById(R.id.filters);
        lparms = (RelativeLayout.LayoutParams)l.getLayoutParams();
    	double coefficient = 0.9177; //real image height/width
    	lparms.width = screenSize.x;
    	lparms.height = (int)(coefficient*screenSize.x);
        l.setLayoutParams(lparms);
        topbarEnd = screenSize.y/2 - lparms.height/2;
        bottombarStart = screenSize.y/2 + lparms.height/2;
        
        //speaker
        parms = (RelativeLayout.LayoutParams)speaker.getLayoutParams();
        parms.height = 7*screenSize.x/10;
        parms.width = parms.height;
        speaker.setLayoutParams(parms);
        
        //buttons
        scaleButtons(settings);
        scaleButtons(help);
        scaleButtons(record);
        scaleButtons(octaveup);
        scaleButtons(octavedown);
        parms = (RelativeLayout.LayoutParams)settings.getLayoutParams();
        //this is horrible, but works
        parms.topMargin = (screenSize.y/2 - lparms.height/2)/2 - parms.height/2;
        parms.leftMargin = screenSize.x/2 - parms.width/2;
        settings.setLayoutParams(parms);
        
        bparms = (RelativeLayout.LayoutParams)help.getLayoutParams();
        bparms.leftMargin = (screenSize.x/2 - bparms.width/2)/2 - bparms.width/2;
        help.setLayoutParams(bparms);
        
        parms = (RelativeLayout.LayoutParams)record.getLayoutParams();
        parms.leftMargin = (screenSize.x/2 - parms.width/2)/2 - parms.width/2;
        record.setLayoutParams(parms);
        
        //parms = (RelativeLayout.LayoutParams)octavedown.getLayoutParams();
        //parms.topMargin = (screenSize.y/2-lparms.height/2)/2 - parms.height/2;
        //octavedown.setLayoutParams(parms);
        
        parms = (RelativeLayout.LayoutParams)seek.getLayoutParams();
        parms.topMargin = bparms.height/2 - 20;
        seek.setLayoutParams(parms);
    }
    
   //scaling play, settings and help buttons
    private void scaleButtons(Button b){
    	ViewGroup.LayoutParams parms = b.getLayoutParams();
    	parms.width = screenSize.x/5;
    	parms.height= screenSize.x/5;
    	b.setLayoutParams(parms);
    }
    //scaling filter buttons and speaker
    private void scaleFilters(RelativeLayout.LayoutParams parms){
    	double coefficient = 0.9177; //real image height/width
    	parms.width = screenSize.x;
    	parms.height = (int)(coefficient*screenSize.x);
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
    	Intent intent = new Intent(this, FiltersMenu.class);
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

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 1) {
    		if(resultCode == RESULT_OK){
    			settings = data.getBooleanArrayExtra(FiltersMenu.EXTRA_MESSAGE);
    			sliderValues = data.getIntArrayExtra(TAG);
    			setLowPassFilterCutoffFrequencies();
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
    
    // When octave up button is pressed
    public void octaveUp(View view){
    	octaveNumber = MidiNote.upOctave(octaveNumber);
    	Log.i(LogTag, "octaveNumber = " + octaveNumber);
    }
    
    private int waveArray = 0;
    //When wave button is pressed
    public void changeWave(View view){
    	Button b = (Button)view;
    	//String wave = b.getText().toString();
    	if (waveArray==0){
    		b.setBackgroundResource(R.drawable.square_wave);
    		soundman.setWave(new SquareWave());
    		waveArray = 1;
    	}
    		//get the picture for the new wave here
		else if (waveArray==1){
			b.setBackgroundResource(R.drawable.harm_square_wave);
			soundman.setWave(new HarmonicSquareWave());
			waveArray = 2;
		}
		else if (waveArray==2){
			b.setBackgroundResource(R.drawable.sawtooth_wave);
			soundman.setWave(new FallingSawToothWave());
			waveArray =3;
		}
		else if (waveArray==3){
			b.setBackgroundResource(R.drawable.sin_wave);
			soundman.setWave(new SineWave());
			waveArray = 0;
		}
    }
    
    // When octave down button is pressed
    public void octaveDown(View view){
    	octaveNumber = MidiNote.downOctave(octaveNumber);
    	Log.i(LogTag, "octaveNumber = " + octaveNumber);
    }
    
    //*****methods for main slider*****
	//Gets value while user drags the thumb (gets called only if value is changed!)
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		noteNumber = progress;
		//Log.i(LogTag, "noteNumber = " + noteNumber);
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Do nothing
	}

	//Seek Bar value when user lifts his finger off screen
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Do nothing
		
	}
    
    //*********************gesture code****************************
    
    private static final int defaultMinSliderValue = 0;
	private static final int defaultMaxSliderValue = 200;
	private static int[] sliderValues = new int[]{defaultMinSliderValue,defaultMaxSliderValue};
	private static int[] filterarray = new int[]{1,2,3,4};
	
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
		if (Math.sqrt((dispX - x)*(dispX - x)+(dispY-y)*(dispY-y)) > speaker.getWidth()/2){
			return false;
		}
		return true;
	}
	
	private boolean isInFilters(float x, float y){
		if ((y > bottombarStart)||(y < topbarEnd)) return false;
		else return true;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		int pointNum = me.getPointerCount();
		int mActivePointerId;
		int pointerIndex;
		int speakerPointerId = 0;
		boolean infiltertouch = false;
		int action = MotionEventCompat.getActionMasked(me);
		int actionIndex = me.getActionIndex();
		float x = MotionEventCompat.getX(me, actionIndex);
    	float y = MotionEventCompat.getY(me, actionIndex);
		//Log.i(TAG,actionToString(action)+"X: "+x+"Y: "+y);
		//check for multitouch
		if (pointNum > 1){
			//go through each pointer
			for (int i = 0; i < pointNum; i++){
				mActivePointerId = me.getPointerId(i);
				pointerIndex = me.findPointerIndex(mActivePointerId);
				infiltertouch = infiltertouch | isInFilters(me.getX(pointerIndex),me.getY(pointerIndex));
				if (infiltertouch){
					//identify which one touches the speaker
					if (isInSpeaker(me.getX(pointerIndex),me.getY(pointerIndex))){
						speakerPointerId = mActivePointerId;
					}
				}
				boolean temp = isInFilters(MotionEventCompat.getX(me, pointerIndex),MotionEventCompat.getY(me, pointerIndex));
				if ((action == MotionEvent.ACTION_MOVE)&&(!temp)){
					MotionEvent me2 = MotionEvent.obtain(me);
					float x2 = MotionEventCompat.getX(me, pointerIndex);
					float y2 = MotionEventCompat.getY(me, pointerIndex);
					me2.setLocation(x2, y2);
					super.dispatchTouchEvent(me2);
					//return true;
				}
			}
		} else{
			infiltertouch = isInFilters(x,y);
		}
		//if none of the pointers are in filters 
		if (!infiltertouch){
			super.dispatchTouchEvent(me);
			return true;
		}
    	//when any finger is lifted off screen
    	if ((action == MotionEvent.ACTION_UP)||(action == MotionEvent.ACTION_POINTER_UP)){
    		//if speaker pointer lifted
    		if (actionIndex == me.findPointerIndex(speakerPointerId)){
                Log.i(TAG,"audioman.isPlaying() is " + audioman.isPlaying() + ". If true, toggling now");
    			if (audioman.isPlaying() == true) toggleplayonoff();
    			Log.i(TAG,"UP!");
    			super.dispatchTouchEvent(me);
    			return true;
    		}
    		if (pointNum == 1){
    			super.dispatchTouchEvent(me);
    			return true;
    		}
    		//if non-speaker pointer lifted
			MotionEvent me2 = MotionEvent.obtain(me);
			float x2 = MotionEventCompat.getX(me, actionIndex);
			float y2 = MotionEventCompat.getY(me, actionIndex);
			int action2 = MotionEvent.ACTION_UP;
			me2.setLocation(x2, y2);
			me2.setAction(action2);
			super.dispatchTouchEvent(me2);
			return true;
    	}
    	//when finger touches the screen
    	if (action == MotionEvent.ACTION_POINTER_DOWN){
    		if (isInSpeaker(me.getX(actionIndex),me.getY(actionIndex))){
    			leftButton = true;
    			toggleplayonoff();
    		} else {
    			MotionEvent me2 = MotionEvent.obtain(me);
    			float x2 = MotionEventCompat.getX(me, actionIndex);
    			float y2 = MotionEventCompat.getY(me, actionIndex);
    			int action2 = MotionEvent.ACTION_DOWN;
    			me2.setLocation(x2, y2);
    			me2.setAction(action2);
    			super.dispatchTouchEvent(me2);
    			return true;
    		}
    	}
    	if (action == MotionEvent.ACTION_DOWN){
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

	public static String actionToString(int action) {
	    switch (action) {
	                
	        case MotionEvent.ACTION_DOWN: return "Down";
	        case MotionEvent.ACTION_MOVE: return "Move";
	        case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
	        case MotionEvent.ACTION_UP: return "Up";
	        case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
	        case MotionEvent.ACTION_OUTSIDE: return "Outside";
	        case MotionEvent.ACTION_CANCEL: return "Cancel";
	    }
	    return "";
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
			
			// Get the low-pass filter
            LowPassFilter lpf = (LowPassFilter) filterman.getFilter (0);
	    	
	    	// Change the cutoff (shelf) frequency
	    	lpf.setCutoffFrequency(newCutoff);
	    	//Log.i(LogTag, "Setting cutoff frequency to : " + newCutoff);
		}
	}
	
	/** Audio threads **/
	
	Runnable writerThreadObj = new Runnable() {
		@Override
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
		@Override
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
            			sampleData = soundman.generateTone(noteDuration, MidiNote.getNoteFrequency(octaveNumber+noteNumber), volume, sampleRate);
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

