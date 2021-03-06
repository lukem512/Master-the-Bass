package com.masterthebass.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.Activity;
import android.content.Context;
import android.text.format.Time;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	private Display mDisplay;
	private WindowManager mWindowManager;
	private WakeLock mWakeLock;
    private PowerManager mPowerManager;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private SensorManager oSensorManager;
	private Sensor oSensor;

	SensorEvent calibrate;
	
	private float mSensorX, mSensorY, mSensorZ, oSensorX; 
	private float mLastX, mLastY, mLastZ, oLastX;
	boolean isNegative, lIsNegative;
	
	private float calx = 0;
	private float caly = 0;
	private float calz = 0;
	
	float totalAccel, prevTotalAccel;
	Time timeA, timeB;
	boolean useTimeA = true;
	
	private int moveCount = 0;
	
	private static final String TAG = "accelData";
	private static final String TAG2 = "accelDataMax";
	boolean writing = false;
	private int i = 0;
	float totalDif = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);										//Prevents screen from locking
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);									//
		mDisplay = mWindowManager.getDefaultDisplay();														//
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());	//
		mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);						//Manages Linear Acceleration sensor
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);							//
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);						//
		oSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);						//Manages Orientation sensor 
		oSensor = oSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);									//
		oSensorManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_FASTEST);						//
		
		prevTotalAccel = 0;
		
		timeA = new Time();
		timeA.setToNow();
		
		timeB = new Time();
		timeB.setToNow();
	
	((Button)findViewById(R.id.startData)).setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) //When button is clicked
		{
			if (!writing) //If not outputting data
			{
				((Button)findViewById(R.id.startData)).setText(R.string.stopData); //Change the button text
				writing = true; //set writing to true
			}
			else //If outputting data
			{
				((Button)findViewById(R.id.startData)).setText(R.string.startData); //Change the button text
				writing = false; //Set writing to false
			}
		}
	});
	}

	public void onResume() //When the app is resumed
	{
		super.onResume();
		mWakeLock.acquire(); //Restart the wake lock
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST); //Stop listening to sensors
		oSensorManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_FASTEST); //Frees up sensors and saves battery power
	}
	
	public void onPause() //When the app is paused
	{
		super.onPause();
		mWakeLock.release(); //Release the wake lock
		mSensorManager.unregisterListener(this); //Start listening to the sensors again
		oSensorManager.unregisterListener(this); //
	}
		
	public boolean onCreateOptionsMenu(Menu menu) //When menu button is pressed open menu
	{
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.cali):
				calx = calibrate.values[0];
				caly = calibrate.values[1];
				calz = calibrate.values[2];
				break;
			case (R.id.reset_cal):
				calx = 0;
				caly = 0;
				calz = 0;
				break;
			case (R.id.reset_count):
				moveCount = 0;
				break;
	        default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		TextView xAccel = ((TextView)findViewById(R.id.x_accel));
		TextView yAccel = ((TextView)findViewById(R.id.y_accel));
		TextView zAccel = ((TextView)findViewById(R.id.z_accel));
		TextView actualAccel = ((TextView)findViewById(R.id.actual_acceleration));
		TextView xOrientation = ((TextView)findViewById(R.id.x_orientation));
		
		TextView noMoves = ((TextView)findViewById(R.id.no_shakes));
		noMoves.setText("No. of Moves  : " + moveCount);
		
		if( oSensor == null ) {
			xOrientation.setText("No orientation sensor.");
			return;
    	}
		if( mSensor == null ) {
			xAccel.setText("No accelerometer found.");
			yAccel.setText("No accelerometer found.");
			zAccel.setText("No accelerometer found.");
			return;
		} 
		
	    if (event.sensor.equals(mSensor))
	    {
			//if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION)
	        //    return;
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
			
				xAccel.setText("X Acceleration: " + (deltaX - calx));
				yAccel.setText("Y Acceleration: " + (deltaY - caly));
				zAccel.setText("Z Acceleration: " + (deltaZ - calz));
				
				totalAccel = FloatMath.sqrt((deltaX - calx) * (deltaX - calx) +
						  (deltaY - caly) * (deltaY - caly) +
						  (deltaZ - calz) * (deltaZ - calz));
				actualAccel.setText("Acceleration   : " + totalAccel);
			
	    }
	    else if (event.sensor.equals(oSensor))
	    {
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
		    	xOrientation.setText("X Orientation	 : " + (oSensorX));
	    	}
		
	    if(oSensorX < 0)
	    	isNegative = true;
	    else
	    	isNegative = false;
	    
	    if(oLastX < 0)
	    	lIsNegative = true;
	    else
	    	lIsNegative = false;
	    
		/*if( totalDif < totalAccel )
		{
			totalDif = totalAccel;
		}
		if( totalDif > totalAccel && (( !isNegative && lIsNegative ) || ( isNegative && !lIsNegative )) )
		{
			moveCount ++;
			if(writing )
			{
				Log.i(TAG2, "Speed Max: " + totalDif);
				i++;
			}
			totalDif = 0;
		}*/
	    
	    if (useTimeA) {
	    	Log.d(TAG2, "setting timeA");
			timeA.setToNow();
		} else {
			Log.d(TAG2, "setting timeB");
			timeB.setToNow();
		}
	    
	    if(writing && (prevTotalAccel != totalAccel || totalAccel == 0))
		{
			double dTime;
			
			if (useTimeA) {
				dTime = (timeA.toMillis(false) - timeB.toMillis(false));
			} else {
				dTime = (timeB.toMillis(false) - timeA.toMillis(false));
			}
			
			double grad = (totalAccel - prevTotalAccel)/(dTime);
			i++;
			
			Log.i(TAG2, "Speed Max: " + totalAccel);
			Log.i(TAG2, "Gradient: " + grad);
			Log.i(TAG2, "timeA: " + timeA.toMillis(false));
			Log.i(TAG2, "timeB: " + timeB.toMillis(false));
		}
	    
	    prevTotalAccel = totalAccel;
	    useTimeA = !useTimeA;
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
