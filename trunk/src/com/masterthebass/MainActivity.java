package com.masterthebass;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	private AudioOutputManager audioman;
	private SoundManager soundman;
	private FileManager fileman;
	private boolean resumeHasRun = false;
	
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
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
    
    /** Called when the user clicks the Settings button */
    public void btnFiltersClick(View view) {
    	// TODO - Guy, Petro
    	// Brings up the filters menu
    }
    
}
