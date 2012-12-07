package com.example.uitest;

import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;

public class MainActivity extends Activity {
	
//	private boolean hasMenuButton;
	
    @Override
//    @TargetApi(14)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//    	hasMenuButton = ViewConfiguration.get(this).hasPermanentMenuKey();
//        if (!hasMenuButton) 
        	setContentView(R.layout.activity_main);
//        else setContentView(R.layout.activity_main_alt);
        
    }

    
    //when pressing the menu hardware button the settings menu will open
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
