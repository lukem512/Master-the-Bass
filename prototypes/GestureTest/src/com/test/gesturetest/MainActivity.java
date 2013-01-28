package com.test.gesturetest;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity implements OnGestureListener{
	public static final int gestureDelay = 500;
	public static final String TAG = "com.test.gesturetest";
	private GestureDetector gestureScanner;
	private static final String[] gesturearray = new String[]{"NUll","NUll","NUll","NUll"};	
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        gestureScanner = new GestureDetector(this,this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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
	     Intent intent = new Intent(this, Filters.class);
	     startActivity(intent);
	    }

		
}
