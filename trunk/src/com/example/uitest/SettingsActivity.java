package com.example.uitest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class SettingsActivity extends Activity{
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
    
    //called when the user clicks the back button
    public void goBack(View view) {
    	Intent intent = new Intent(this, MainActivity.class);
    	startActivity(intent);
    	}
}