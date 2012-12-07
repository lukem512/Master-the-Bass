package com.masterthebass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class RunActivity extends Activity {
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
    }   

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
		return true;
	}
	
	public void btnSoundCtrlClick(View view) {
		// TODO
	}
	
	public void btnSoundStopClick(View view) {
		// TODO
	}
}
