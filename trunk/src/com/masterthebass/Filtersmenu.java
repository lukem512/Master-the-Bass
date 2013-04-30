package com.masterthebass;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.masterthebass.WaveButton.onWaveChangeListener;



import com.masterthebass.SliderView;
import com.masterthebass.SliderView.KnobValuesChangedListener;


public class FiltersMenu extends Activity {

	public final static String WAVE = "com.masterthebass.WAVE";
	public final static String TAG = "com.masterthebass.FILTERS";
	public final static String EXTRA_MESSAGE = "com.masterthebass.MESSAGE";
	public final static String FILTERMAN_FILTER_NAMES = "com.masterthebass.FILTERMAN_FILTER_NAMES";
	public final static int NUM_SETTINGS = 6;
	
	public final static String LogTag = "FiltersMenu";

	
	private SliderView slider;
	private TextView text_interval;
	
	
	private String[] filterListNames;	
	private ArrayList<String> filters  = new ArrayList<String>();
	/*  settings:
	 *  0 - 3 are filter on/off buttons 
	 *  4 vibration button
	 */
	boolean[] settings;
	/* filterdropdown:
	 * 0-3 correspond to each spinner
	 */
	int[] filterdropdown;
	int[] sliderValues;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settings = new boolean[NUM_SETTINGS];
		setContentView(R.layout.activity_filters);
		
		// Initiate switches
		
		ToggleButton btn4 = (ToggleButton) findViewById(R.id.toggleButton5);
		
		
		// Get data from intent
		Bundle bundle = getIntent().getExtras();
		settings = bundle.getBooleanArray("settings");
		
		sliderValues = bundle.getIntArray("sliderValues");
		filterdropdown = bundle.getIntArray("filters");
		filterListNames = bundle.getStringArray("FILTERMAN_FILTER_NAMES");
		
      
        btn4.setChecked(settings[4]);

        SetUpSlider();

        addtofilters();
        
        //using own font
        Typeface tf = Typeface.createFromAsset(getAssets(),"NeoSans_Bold_Italic.ttf");
        TextView settings = (TextView) findViewById(R.id.CustomFontText);
        settings.setTypeface(tf);
        TextView minmaxfreq = (TextView) findViewById(R.id.minmaxfreq);
        minmaxfreq.setTypeface(tf);
        TextView vibration = (TextView) findViewById(R.id.vibration);
        vibration.setTypeface(tf);
        TextView text_interval = (TextView) findViewById(R.id.text_interval);
        text_interval.setTypeface(tf);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        //setting centre for filter buttons
        RelativeLayout.LayoutParams fparms = (RelativeLayout.LayoutParams)findViewById(R.id.filterbuttons).getLayoutParams();
        Button bcentre = (Button)findViewById(R.id.midbutton);
        LayoutParams sp = (LayoutParams)findViewById(R.id.spinner1).getLayoutParams();
        RelativeLayout.LayoutParams centreparms = (RelativeLayout.LayoutParams)bcentre.getLayoutParams();
        Log.i(TAG, "TOP: " + findViewById(R.id.spinner1).getLayoutParams().width);
        centreparms.width = sp.width/2 + 10;
        centreparms.height = centreparms.width;
        centreparms.topMargin = fparms.topMargin + sp.height - centreparms.height/2;
        bcentre.setLayoutParams(centreparms);
        
        Button calib = (Button)findViewById(R.id.buttoncalib);
        ViewGroup.LayoutParams parms = calib.getLayoutParams();
        parms.height = (int)(centreparms.height/1.5);
        calib.setLayoutParams(parms);
	}
	
	private void SetUpSlider() {
		text_interval = (TextView)findViewById(R.id.text_interval);		
		slider = (SliderView)findViewById(R.id.slider);

		//size of screen and knob
        Bitmap backImage = BitmapFactory.decodeResource(getResources(), R.drawable.bar);
		
        //we use the sizes for the slider
        LayoutParams params = slider.getLayoutParams();
        params.width = backImage.getWidth();
        params.height = backImage.getHeight();
		slider.setLayoutParams(params);
		slider.setPosition(10,0);
		slider.setStartKnobValue(sliderValues[0]);
		slider.setEndKnobValue(sliderValues[1]);
		slider.setOnKnobValuesChangedListener(new KnobValuesChangedListener() {
			
			@Override
			public void onValuesChanged(boolean knobStartChanged,
					boolean knobEndChanged, int knobStart, int knobEnd) {
				if(knobStartChanged || knobEndChanged)
					text_interval.setText(knobStart + " - " + knobEnd);
					sliderValues[0] = knobStart;
					sliderValues[1] = knobEnd;
			}
		});
	}
	
	//***********************getting filters in to the spinners*********************************************
	
	
	public void addtofilters(){
		//Adding the filter names to the filter list for the list on screen
		for(int i=0;i<filterListNames.length;i++){
			filters.add(filterListNames[i]);
		}
		
		//adding the array to the spinner1
		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,R.layout.custom_spinner, filters);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter1);			
		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
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
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,R.layout.custom_spinner, filters);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter2);			
		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
		@Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
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
		ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this,R.layout.custom_spinner, filters);
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner3.setAdapter(adapter3);			
		spinner3.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
			    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
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
		ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this,R.layout.custom_spinner, filters);
		adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner4.setAdapter(adapter4);			
		spinner4.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
			    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					com.masterthebass.MainActivity.addTofilterArray(position, 3);
			        // your code here
				    }
					@Override
				    public void onNothingSelected(AdapterView<?> parentView) {
			        // your code here
					}
					});
		spinner1.setSelection(filterdropdown[0]);
		spinner2.setSelection(filterdropdown[1]);
		spinner3.setSelection(filterdropdown[2]);
		spinner4.setSelection(filterdropdown[3]);
	}
	
	//******************************************************************************************	
	

	

	
	@Override
    public void onBackPressed(){
		WaveButton b = (WaveButton) findViewById(R.id.btnWave);		
        Intent a = new Intent(getApplicationContext(),MainActivity.class);
        a.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        a.putExtra(EXTRA_MESSAGE, settings);
        a.putExtra(TAG, sliderValues);
        a.putExtra(WAVE, b.getText());
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

	public void calibrate(View view){
		settings[5] = true;
		
		// Display instructions
		Toast toast = Toast.makeText(getApplicationContext(), "Hold your phone in the desired orientation and tap speaker graphic", Toast.LENGTH_LONG);
		toast.show();
		
		// Navigate back to main screen
		onBackPressed();
	}
    
}