package com.masterthebass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.Activity;
import android.content.Context;
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

public class Accelerometer extends Activity implements SensorEventListener {

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
	
	float totalAccel;
	
	private int moveCount = 0;
	
	private static final String TAG = "accelData";
	private static final String TAG2 = "accelDataMax";
	boolean writing = false;
	private int i = 0;
	float totalDif = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accelerometer);
		
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);										//Prevents screen from locking
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);									//
		mDisplay = mWindowManager.getDefaultDisplay();														//
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());	//
		mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);						//Manages Linear Acceleration sensor
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);							//
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);						//
		oSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);						//Manages Orientation sensor 
		oSensor = oSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);									//
		oSensorManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_UI);						//
	
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
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI); //Stop listening to sensors
		oSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI); //Frees up sensors and saves battery power
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
		getMenuInflater().inflate(R.menu.activity_accelerometer, menu);
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
		
	    if (event.sensor.equals(mSensor))			//if reading from the accelerometer
	    {
			//if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION)
	        //    return;
			Sensor source = event.sensor;
			calibrate = event;
			final float NOISE = (float) 1.0;
			
			switch (mDisplay.getRotation())			//Changes accelerometer sensor read based on orientation of the device
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
				
				if (deltaX < NOISE)				//Removes noise
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
			
				xAccel.setText("X Accel: " + (deltaX - calx));
				yAccel.setText("Y Accel: " + (deltaY - caly));
				zAccel.setText("Z Accel: " + (deltaZ - calz));
				
				totalAccel = FloatMath.sqrt((deltaX - calx) * (deltaX - calx) +
						  (deltaY - caly) * (deltaY - caly) +
						  (deltaZ - calz) * (deltaZ - calz));
				actualAccel.setText("Accel   : " + totalAccel);
			
	    }
	    else if (event.sensor.equals(oSensor))			//if reading from the orientation sensor
	    {
	    	oLastX = oSensorX;
		    	switch (mDisplay.getRotation())			//Changes orientation sensor read based on orientation of the device
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
		    	xOrientation.setText("X Orien: " + (oSensorX));
	    	}
		
	    if(oSensorX < 0)
	    	isNegative = true;
	    else
	    	isNegative = false;
	    
	    if(oLastX < 0)
	    	lIsNegative = true;
	    else
	    	lIsNegative = false;
	    
		if( totalDif < totalAccel )
		{
			totalDif = totalAccel;
		}
		if( totalDif > totalAccel && (( !isNegative && lIsNegative ) || ( isNegative && !lIsNegative )) ) 
		{ //if one sound generation movement has been completed
			moveCount ++;
			if(writing )
			{ //output the max acceleration
				Log.i(TAG2, "Accel. Max: " + totalDif);
				i++;
			}
			totalDif = 0;
		}
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
