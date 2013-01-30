package com.masterthebass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity{
	public final static String EXTRA_MESSAGE = "com.masterthebass.MESSAGE";
	ToggleButton vibrationToggle;
	boolean vibrationON;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        vibrationToggle = (ToggleButton) findViewById(R.id.vibrationButton);
        Intent intent = getIntent();
        vibrationON = intent.getBooleanExtra(MainActivity.EXTRA_MESSAGE,false);
        vibrationToggle.setChecked(vibrationON);
    }

    @Override
    public void onBackPressed(){
        Intent a = new Intent(getApplicationContext(),MainActivity.class);
        a.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        a.putExtra(EXTRA_MESSAGE, vibrationON);
        setResult(RESULT_OK,a);
        finish();
    }
   /*
    @Override
    public void onResume(){
    	vibrationToggle.setChecked(vibrationON);
    	super.onResume();
    }
    */
    public void onVibrationClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        /*
        if (on) {
            // Enable vibrate
        } else {
            // Disable vibrate
        } */
       vibrationON = on;
    }
}