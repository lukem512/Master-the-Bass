package com.masterbass;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class Help extends Activity implements OnTouchListener{

	private Button scr;
	private int currentImage = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		scr = (Button)findViewById(R.id.scr);
		scr.setBackgroundDrawable(getResources().getDrawable(R.drawable.help1));
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_help, menu);
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		Log.i("HI: ","potato!");
		if (me.getAction() == MotionEvent.ACTION_DOWN){
		switch (currentImage){
		case 0:
			scr.setBackgroundDrawable(getResources().getDrawable(R.drawable.help2));
			currentImage = 1;
			break;
		case 1:
			scr.setBackgroundDrawable(getResources().getDrawable(R.drawable.help3));
			currentImage = 2;
			break;
		case 2:
			scr.setBackgroundDrawable(getResources().getDrawable(R.drawable.help4));
			currentImage = 3;
			break;
		case 3:
			scr.setBackgroundDrawable(getResources().getDrawable(R.drawable.help5));
			currentImage = 4;
			break;
		case 4:
			scr.setBackgroundDrawable(getResources().getDrawable(R.drawable.help1));
			currentImage = 0;
			break; 	
		}
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
