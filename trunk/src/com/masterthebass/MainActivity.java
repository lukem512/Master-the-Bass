package com.masterthebass;

import android.os.Bundle;
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
	private FilterManager filterman;
	private boolean resumeHasRun = false;
	
	/** Private helper methods */
	   
   	private void instantiate() {
   		audioman 	= new AudioOutputManager();
   		soundman	= new SoundManager();
   		fileman 	= new FileManager();
   		filterman 	= new FilterManager();
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
    		resumeHasRun = true;
    		
    		Log.d("main.onResume", "The filter IDs list is...");
    		int[] IDs = filterman.getFiltersList();
    		
    		for (int i = 0; i < IDs.length; i++)
    			Log.d("main.onResume", i + ": " + filterman.getFilterName(IDs[i]) + " has ID #" + IDs[i]);
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
    
    //*********************gesture code****************************
    
    public static final int gestureDelay = 500;
	public static final String TAG = "com.masterthebass";
	private GestureDetector gestureScanner;
	private static final String[] gesturearray = new String[]{"NULL","NULL","NULL","NULL"};	
	private static final String[] actionarray = new String[]{"NULL","NULL","NULL","NULL"};
	
	long lastGesture = System.currentTimeMillis();
	
	public static void addTogestureArray(CharSequence gesture,int gesturenum){
		gesturearray[gesturenum] = (String) gesture;
		Log.e(TAG,"the gesture is " + gesturearray[0]);
		Log.e(TAG,"the gesture is " + gesturearray[1]);
		Log.e(TAG,"the gesture is " + gesturearray[2]);
		Log.e(TAG,"the gesture is " + gesturearray[3]);
	}
	
	public static void addToactionArray(CharSequence action, int actionnum){
		actionarray[actionnum] = (String) action;
		Log.e(TAG,"the action is " + actionarray[0]);
		Log.e(TAG,"the action is " + actionarray[1]);
		Log.e(TAG,"the action is " + actionarray[2]);
		Log.e(TAG,"the action is " + actionarray[3]);
		
	}   
	
    @Override
	public boolean onTouchEvent(MotionEvent me)	{
		//return false;
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
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
		for(int i = 0; i<4;i++){
			if(gesturearray[i] == "Hold"){
				Toast toast = Toast.makeText(getApplicationContext(), "Hold", Toast.LENGTH_SHORT);
				toast.show();
			}
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
				for(int i = 0; i<4;i++){
					if(gesturearray[i] == "Swipe Right"){
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Right", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
				Log.e(TAG,"Swipe Right");
			} else if (distanceX > 10)
			{
				for(int i = 0; i<4;i++){
					if(gesturearray[i] == "Swipe Left"){
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Left", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
				Log.e(TAG,"Swipe Left");
			}
			if (distanceY < -10)
			{
				for(int i = 0; i<4;i++){
					if(gesturearray[i] == "Swipe Down"){
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Down", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
				Log.e(TAG,"Swipe Down");
			} else if (distanceY > 10)
			{
				for(int i = 0; i<4;i++){
					if(gesturearray[i] == "Swipe Up"){
						Toast toast = Toast.makeText(getApplicationContext(), "Swipe Up", Toast.LENGTH_SHORT);
						toast.show();
					}
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
		for(int i = 0; i<4;i++){
			if(gesturearray[i] == "Tap"){
				Toast toast = Toast.makeText(getApplicationContext(), "Tap", Toast.LENGTH_SHORT);
				toast.show();
			}
		}
		Log.e(TAG, "Single tap up");
		return false;
	}
	 //start the filter activity
	 public void filters(View view) {
	     Intent intent = new Intent(this, Filtersmenu.class);
	     startActivity(intent);
	    }

		
}

