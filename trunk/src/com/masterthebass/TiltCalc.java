package com.masterthebass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class TiltCalc {
    
    private boolean needsRecalc = false;
    private float[] acc_tilt_data = {0, 0, 0}, gyro_tilt_data = {0, 0, 0}, gravity = {0, 0, 0}, magnet = {0, 0, 0}, R={0,0,0,0,0,0,0,0,0};
    
    // Change this to make the sensors respond quicker, or slower:
    private static final int delay = SensorManager.SENSOR_DELAY_GAME;
    
    
    // Special class used to handle sensor events:
    private final SensorEventListener listen = new SensorEventListener() {
        public void onSensorChanged(SensorEvent e) {
            final float[] vals = e.values, target;
            if(e.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                System.arraycopy(vals, 0, gyro_tilt_data, 0, 3);
            }
            //Else, we'll capture the data, and mark the class for a re-calc:
            target = (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? gravity : magnet;
            needsRecalc = true;
            System.arraycopy(vals, 0, target, 0, 3);
        }
        
        public void onAccuracyChanged(Sensor event, int res) {}
    };
    

    
    // The constructor will use a context object to register itself for various inputs:
    public TiltCalc(Context c) {
        SensorManager man = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE); 
        
        Sensor mag_sensor = man.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor acc_sensor = man.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyr_sensor = man.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        
        if(man.registerListener(listen, gyr_sensor, delay)) {
           Log.d("TiltCalc", "Gyroscope detected, and successfully connected.");
            
            // Use an accelerometer+compass approach:
       } 
        if( man.registerListener(listen, mag_sensor, delay) &&
                man.registerListener(listen, acc_sensor, delay) ) {
        // Use an accelerometer+compass approach:
        } else {
            Log.d("TiltCalc", "No acceptable hardware found.");
            
            // We will remove the listener, just in case one of the accelerometer sensors
            // registered, just not the other one:
            man.unregisterListener(listen);
        }
    }
    
    // Will return the most up-to-date tilt data in the vals[] array
    public void getAccTilt(float[] vals) {
        
        // If some of the data has been changed, then we need to recalculate some things...
        if(needsRecalc) {
            // Calculate the rotation matrix, and use that to get the orientation:
            if(SensorManager.getRotationMatrix(R, null, gravity, magnet))
                SensorManager.getOrientation(R, acc_tilt_data);
            
            
            needsRecalc = false;
        }
        
        System.arraycopy(acc_tilt_data, 0, vals, 0, 3);
    }
    
    public void getGyroTilt(float[] vals) {
        
        // If some of the data has been changed, then we need to recalculate some things...
        if(needsRecalc) {
            // Calculate the rotation matrix, and use that to get the orientation:
            if(SensorManager.getRotationMatrix(R, null, gravity, magnet))
                SensorManager.getOrientation(R, gyro_tilt_data);
            
            
            needsRecalc = false;
        }
        
        System.arraycopy(gyro_tilt_data, 0, vals, 0, 3);
    }
}