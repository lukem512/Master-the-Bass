package com.masterthebass;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class Filtersmenu extends Activity {

	public static final String TAG = "com.masterthebass";
	public final static String EXTRA_MESSAGE = "com.masterthebass.MESSAGE";
	/*  settings:
	 *  0 - 3 are filter on/off buttons 
	 *  4 vibration button
	 */
	boolean[] settings;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = new boolean[5];
		setContentView(R.layout.activity_filters);
		//initiate switches
		ToggleButton btn0 = (ToggleButton) findViewById(R.id.toggleButton1);
		ToggleButton btn1 = (ToggleButton) findViewById(R.id.toggleButton2);
		ToggleButton btn2 = (ToggleButton) findViewById(R.id.toggleButton3);
		ToggleButton btn3 = (ToggleButton) findViewById(R.id.toggleButton4);
		ToggleButton btn4 = (ToggleButton) findViewById(R.id.toggleButton5);
        Intent intent = getIntent();
        settings = intent.getBooleanArrayExtra(EXTRA_MESSAGE);
        btn0.setChecked(settings[0]);
        btn1.setChecked(settings[1]);
        btn2.setChecked(settings[2]);
        btn3.setChecked(settings[3]);
        btn4.setChecked(settings[4]);
	}
	
	@Override
    public void onBackPressed(){
        Intent a = new Intent(getApplicationContext(),MainActivity.class);
        a.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        a.putExtra(EXTRA_MESSAGE, settings);
        setResult(RESULT_OK,a);
        finish();
    }
	
    public void onBtn0Clicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        settings[0] = on;
    }
    
    public void onBtn1Clicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        settings[1] = on;
    }
    
    public void onBtn2Clicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        settings[2] = on;
    }
    
    public void onBtn3Clicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        settings[3] = on;
    }
    
    public void onVibrationClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        settings[4] = on;
    }
	
	public void viewlist(View view) {
		Button button = (Button) view;
		//sending tag so i know where to save in array
	    String tag = button.getTag().toString();
	    //now open new Activity with this tag
	    Intent intent = new Intent(this, Gesturelist.class);
	    Bundle b = new Bundle();
	    b.putString("tag", tag);
	    intent.putExtras(b);
	    startActivity(intent);
	    }
	//hello
	
	public void viewlist2(View view) {
		Button button = (Button) view;
		//sending tag so i know where to save in array
	    String tag = button.getTag().toString();
	    //now open new Activity with this tag
	     Intent intent = new Intent(this, Filterlist.class);
	     Bundle b = new Bundle();
		 b.putString("tag", tag);
		 intent.putExtras(b);
	     startActivity(intent);
	    }

}
