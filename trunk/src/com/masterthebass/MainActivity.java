package com.masterthebass;

import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;

public class MainActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);
    }
    
   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
        return true;
    }
   
    /** Called when the user clicks the Start button */
	public void start(View view) {
    	Intent intent = new Intent(this, RunActivity.class);
    	startActivity(intent);
    	}
    
    /** Called when the user clicks the Settings button */
    public void settings(View view) {
    	Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
    }
    
}
