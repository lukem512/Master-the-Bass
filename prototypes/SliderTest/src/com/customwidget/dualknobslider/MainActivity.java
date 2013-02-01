package com.customwidget.dualknobslider;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.customwidget.dualknobslider.helpers.SliderView;
import com.customwidget.dualknobslider.helpers.SliderView.KnobValuesChangedListener;

public class MainActivity extends Activity {
	private SliderView slider;
	private TextView text_interval;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        SetUpSlider();
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
		
		slider.setOnKnobValuesChangedListener(new KnobValuesChangedListener() {
			
			@Override
			public void onValuesChanged(boolean knobStartChanged,
					boolean knobEndChanged, int knobStart, int knobEnd) {
				if(knobStartChanged || knobEndChanged)
					text_interval.setText(knobStart + " - " + knobEnd);
			}
		});
	}
}