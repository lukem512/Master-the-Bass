package com.masterthebass;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Filtersmenu extends Activity {

	public static final String TAG = "com.masterthebass";
	public final static String EXTRA_MESSAGE = "com.masterthebass.MESSAGE";
	private Spinner spinner1, spinner2, spinner3, spinner4;
	//getting the filter list ID's and adding the filter ID's to array
	private int []filterListID = com.masterthebass.FilterManager.getFiltersList();
	private ArrayList filters  = new ArrayList();
	/*  settings:
	 *  0 - 3 are filter on/off buttons 
	 *  4 vibration button
	 */
	boolean[] settings;
	int[] filterdropdown;
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
        filterdropdown = intent.getIntArrayExtra(TAG);
        btn0.setChecked(settings[0]);
        btn1.setChecked(settings[1]);
        btn2.setChecked(settings[2]);
        btn3.setChecked(settings[3]);
        btn4.setChecked(settings[4]);
        Log.e(TAG,"the item in filterdropdown[0] = " + filterdropdown[0]);
        //spinner1.setSelection(1);
        //spinner1.setSelection(filterdropdown[0]);
        //spinner1.setContentDescription(com.masterthebass.FilterManager.getFilterName(filterdropdown[0]));
        
        addtofilters();
        
	}
	//***********************getting filters in to the spinners*********************************************
	
	
	public void addtofilters(){
		//Adding the filter names to the filter list for the list on screen
		for(int i=0;i<filterListID.length;i++){
			filters.add( com.masterthebass.FilterManager.getFilterName(filterListID[i]));
			Log.e(TAG,"the string added to the array " + com.masterthebass.FilterManager.getFilterName(filterListID[i]));
		}
		filters.add("this is a test");
		filters.add("second test");
		
		//adding the array to the spinner1
		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, filters);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter1);			
		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	    	Log.e(TAG,"has been clicked");
	    	com.masterthebass.MainActivity.addTofilterArray(position, 0);
	        // your code here
	    }
	    @Override
	    public void onNothingSelected(AdapterView<?> parentView) {
	        // your code here
	    }
		});
		
		//adding the array to the spinner2
		Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, filters);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter2);			
		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
		@Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			Log.e(TAG,"has been clicked");
			com.masterthebass.MainActivity.addTofilterArray(position, 1);
	        // your code here
		    }
			@Override
		    public void onNothingSelected(AdapterView<?> parentView) {
	        // your code here
			}
			});
		
		//adding the array to the spinner3
				Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
				ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, filters);
				adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner3.setAdapter(adapter3);			
				spinner3.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
			    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					Log.e(TAG,"has been clicked");
					com.masterthebass.MainActivity.addTofilterArray(position, 2);
			        // your code here
				    }
					@Override
				    public void onNothingSelected(AdapterView<?> parentView) {
			        // your code here
					}
					});
				
				
				//adding the array to the spinner4
				Spinner spinner4 = (Spinner) findViewById(R.id.spinner4);
				ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, filters);
				adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner4.setAdapter(adapter4);			
				spinner4.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
			    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					Log.e(TAG,"has been clicked");
					com.masterthebass.MainActivity.addTofilterArray(position, 3);
			        // your code here
				    }
					@Override
				    public void onNothingSelected(AdapterView<?> parentView) {
			        // your code here
					}
					});
		
	}
	
	//******************************************************************************************	
	
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
