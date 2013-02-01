package com.masterthebass;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.content.Context;
import android.widget.Toast;


public class MainActivity extends Activity implements OnGestureListener{
	private AudioOutputManager audioman;
	private SoundManager soundman;
	private FileManager fileman;
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

	public final static String EXTRA_MESSAGE = "com.masterthebass.MESSAGE";
	
	/** Private helper methods */
	   
   	private void instantiate() {
   		audioman = new AudioOutputManager();
   		soundman = new SoundManager();
   		fileman = new FileManager();
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
    	startActivityForResult(intent,1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 1) {

    		if(resultCode == RESULT_OK){
    			settings = data.getBooleanArrayExtra(Filtersmenu.EXTRA_MESSAGE);
    		}

    		if (resultCode == RESULT_CANCELED) {
    			//Write code on no result return 
    		}
    	}
    }
    
    //*********************gesture code****************************
    
    public static final int gestureDelay = 500;
	public static final String TAG = "com.masterthebass";
	private GestureDetector gestureScanner;
	private static final String[] gesturearray = new String[]{"Swipe Up","Swipe Left","Tap","Hold"};	
	// amount of 0's for the amount of filter names, NEED TO CHANGE
	private static final int[] filterarray = new int[]{0,0,0,0};
	
	
	long lastGesture = System.currentTimeMillis();
	
	public static void addTogestureArray(CharSequence gesture,int gesturenum){
		gesturearray[gesturenum] = (String) gesture;
		Log.e(TAG,"the gesture is " + gesturearray[0]);
		Log.e(TAG,"the gesture is " + gesturearray[1]);
		Log.e(TAG,"the gesture is " + gesturearray[2]);
		Log.e(TAG,"the gesture is " + gesturearray[3]);
	}
	
	public static void addTofilterArray(int filter, int filternum){
		filterarray[filternum] = filter;
		Log.e(TAG,"the action is " + filterarray[0]);
		Log.e(TAG,"the action is " + filterarray[1]);
		Log.e(TAG,"the action is " + filterarray[2]);
		Log.e(TAG,"the action is " + filterarray[3]);
		
	}   
	
    @Override
	public boolean onTouchEvent(MotionEvent me)	{
		//return false;
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		if (settings[4]) v.vibrate(300);
		Log.e(TAG, "Down");		
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		Log.e(TAG, "Fling");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	
			//change this depending on whether the toggles are on for this gesture
				Toast toast = Toast.makeText(getApplicationContext(), "Hold", Toast.LENGTH_SHORT);
				toast.show();
							
				if(gesture4 == false){
					com.masterthebass.FilterManager.enableFilter(filterarray[3]);
					
					gesture4 = true;
				}else{
					com.masterthebass.FilterManager.disableFilter(filterarray[3]);		
					gesture4 = false;
				}			
		
		Log.e(TAG, "Long Press");
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
						com.masterthebass.FilterManager.disableFilter(filterarray[1]);							
						gesture2 = false;
				
				Log.e(TAG,"Swipe Right");
			} else if (distanceX > 10)
			{					
						if (settings[4]) v.vibrate(300);
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Left", Toast.LENGTH_SHORT);
						toast.show();
						if(gesture2 == false){
							com.masterthebass.FilterManager.enableFilter(filterarray[1]);							
							gesture2 = true;
						}					
				
				Log.e(TAG,"Swipe Left");
			}
			if (distanceY < -10)
			{
						if (settings[4]) v.vibrate(300);
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Down", Toast.LENGTH_SHORT);
						toast.show();
						
							com.masterthebass.FilterManager.disableFilter(filterarray[0]);							
							gesture1 = false;
							
					
				Log.e(TAG,"Swipe Down");
			} else if (distanceY > 10)
			{
				
						if (settings[4]) v.vibrate(300);
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Up", Toast.LENGTH_SHORT);
						toast.show();
						if(gesture1 == false){
							com.masterthebass.FilterManager.enableFilter(filterarray[0]);							
							gesture1 = true;
						}	
				Log.e(TAG,"Swipe Up");
			}
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		//checking whether it is a real tap or accident
		Log.e(TAG, "Show press");	
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		
				if (settings[4]) v.vibrate(300);
				Toast toast = Toast.makeText(getApplicationContext(), "Tap", Toast.LENGTH_SHORT);
				toast.show();
			
				if(gesture3 == false){
					com.masterthebass.FilterManager.enableFilter(filterarray[2]);
					
					gesture3 = true;
				}else{
					com.masterthebass.FilterManager.disableFilter(filterarray[2]);		
					gesture3 = false;
				}
				
		Log.e(TAG, "Single tap up");
		return false;
	}

		
}

