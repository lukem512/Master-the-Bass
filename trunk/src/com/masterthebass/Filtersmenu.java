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

	public final static String TAG = "com.masterthebass.FILTERS";
	public final static String EXTRA_MESSAGE = "com.masterthebass.MESSAGE";
	public final static String FILTERMAN_CLASS = "com.masterthebass.FILTERMAN_CLASS";
	
	public final static String LogTag = "FiltersMenu";
	
	private Spinner spinner1, spinner2, spinner3, spinner4;
	//getting the filter list ID's and adding the filter ID's to array
	private int[] filterListID;	
	private ArrayList filters  = new ArrayList();
	/*  settings:
	 *  0 - 3 are filter on/off buttons 
	 *  4 vibration button
	 */
	boolean[] settings;
	int[] filterdropdown;
	
	private FilterManager filterman;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		settings = new boolean[5];
		setContentView(R.layout.activity_filters);
		
		// Initiate switches
		ToggleButton btn0 = (ToggleButton) findViewById(R.id.toggleButton1);
		ToggleButton btn1 = (ToggleButton) findViewById(R.id.toggleButton2);
		ToggleButton btn2 = (ToggleButton) findViewById(R.id.toggleButton3);
		ToggleButton btn3 = (ToggleButton) findViewById(R.id.toggleButton4);
		ToggleButton btn4 = (ToggleButton) findViewById(R.id.toggleButton5);
		
		// Get data from intent
		Intent intent = getIntent();
		
		filterman = (FilterManager) intent.getSerializableExtra(FILTERMAN_CLASS);
		Log.d(LogTag, "Got FilterManager instance");
		
		// get list of filters
		filterListID = filterman.getFiltersList();
		Log.d(LogTag, "Got list of filter IDs from FilterManager instance");
		
		for (int i = 0; i < filterListID.length; i++) {
			Log.d (LogTag, "Received filter: "+ filterListID[i]);
		}
		
        settings = intent.getBooleanArrayExtra(EXTRA_MESSAGE);
        Log.d(LogTag, "Got settings from from FilterManager instance");
        
        for (int i = 0; i < settings.length; i++) {
			Log.d (LogTag, "Received setting: "+settings[i]);
		}
        
        filterdropdown = intent.getIntArrayExtra(TAG);
        Log.d(LogTag, "Got filter drop down from from FilterManager instance");
        
        for (int i = 0; i < filterdropdown.length; i++) {
			Log.d (LogTag, "Received filterdropdown: "+filterdropdown[i]);
		}
        
        if (btn0 == null || btn1 == null || btn2 == null || btn3 == null || btn4 == null) {
        	Log.e(LogTag,"One or more of the toggle buttons are null!");
        } else {
        	Log.d(LogTag, "Toggle buttons aren't null");
        }
        
        btn0.setChecked(settings[0]);
        btn1.setChecked(settings[1]);
        btn2.setChecked(settings[2]);
        btn3.setChecked(settings[3]);
        btn4.setChecked(settings[4]);
        Log.d(LogTag,"the item in filterdropdown[0] = " + filterdropdown[0]);
        
        addtofilters();
        Log.d(LogTag,"Added filters to list.");
	}
	//***********************getting filters in to the spinners*********************************************
	
	
	public void addtofilters(){
		//Adding the filter names to the filter list for the list on screen
		for(int i=0;i<filterListID.length;i++){
			filters.add(filterman.getFilterName(filterListID[i]));
			Log.i(TAG,"the string added to the array " + filterman.getFilterName(filterListID[i]));
		}
		
		//adding the array to the spinner1
		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, filters);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter1);			
		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	    	Log.i(TAG,"has been clicked");
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
			Log.i(TAG,"has been clicked");
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
					Log.i(TAG,"has been clicked");
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
					Log.i(TAG,"has been clicked");
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
}