package com.masterthebass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;

public class Help extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		setupText();
	}
	//load text from file and format textview
	private void setupText(){
		//formatting textview
		TextView t = (TextView)findViewById(R.id.texthelp);
		t.setMovementMethod(new ScrollingMovementMethod());
		t.setTextSize(20);
		WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
	    Display mDisplay = mWindowManager.getDefaultDisplay();
	    t.setMaxLines(mDisplay.getHeight()/20);
		
		//reading text
		InputStream is = getResources().openRawResource(R.raw.help);
		StringBuilder total = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
		try {
			while ((line = br.readLine()) != null) {
			    total.append(line);
			    total.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		t.setText(total);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_help, menu);
		return true;
	}

}
