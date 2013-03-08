package com.masterthebass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SliderView extends View {
	public final static String TAG = "com.masterthebass.FILTERS";
	public final static int minVal = 0;
	public final static int maxVal = 1000;
	protected KnobValuesChangedListener knobValuesChangedListener;
	private Knob[] knobs = new Knob[2]; // array that holds the knobs
	private int balID = 0; // variable to know what knob is being dragged  
	private Point pointKnobStart, pointKnobEnd;
	private boolean initialisedSlider;
	private int startKnobValue, endKnobValue;//value to know the knob position e.g: 0,40,..,100
	private int startKnobValueTmp, endKnobValueTmp;//the position on the X axis
	private double sliderWidth, sliderHeight;//real size of the view that holds the slider
	private Paint paintText;
	private Bitmap img; //back image
	private Point pos;
	
	
	public SliderView(Context context) {
		super(context);
		setFocusable(true);
		img = BitmapFactory.decodeResource(context.getResources(), R.drawable.bar);
		sliderWidth = img.getWidth();
		sliderHeight = img.getHeight();
	}
	   
	public SliderView(Context context, AttributeSet attrs) {
		super(context, attrs);	   
		setFocusable(true);    
		pointKnobStart = new Point();
		pointKnobEnd = new Point();
		initialisedSlider = false;
		img = BitmapFactory.decodeResource(context.getResources(), R.drawable.bar);
		sliderWidth = img.getWidth();
		sliderHeight = img.getHeight();
	}
	
	@Override 
	protected void onDraw(Canvas canvas) {
		//background for slider
		//canvas.drawColor(Color.BLACK);
		//initialise data for knobs , slider
		if(!initialisedSlider) {
			initialisedSlider = true;
	        Bitmap knobImage = BitmapFactory.decodeResource(getResources(), R.drawable.knob);
			pointKnobStart.x = pos.x + ((int)sliderWidth)*startKnobValue/maxVal;
			pointKnobStart.y = (int)(sliderHeight/2.0) - knobImage.getHeight()/2;
			pointKnobEnd.x = pos.x + ((int)sliderWidth)*endKnobValue/maxVal - knobImage.getWidth();
			pointKnobEnd.y = (int)(sliderHeight/2.0) - knobImage.getHeight()/2;

			knobs[0] = new Knob(getContext(),R.drawable.knob, pointKnobStart);
			knobs[1] = new Knob(getContext(),R.drawable.knob, pointKnobEnd);
			knobs[0].setID(1);
			knobs[1].setID(2);
			//setStartKnobValue(0);
			//setEndKnobValue(100);
			knobValuesChanged(true, true, getStartKnobValue(), getEndKnobValue());

			paintText = new Paint();//the paint for the slider data(the values) 
			paintText.setColor(Color.WHITE);
		}	
		canvas.drawBitmap(img,pos.x,pos.y,null);
		canvas.drawBitmap(knobs[0].getBitmap(), knobs[0].getX(), knobs[0].getY(), null);
		canvas.drawBitmap(knobs[1].getBitmap(), knobs[1].getX(), knobs[1].getY(), null);
	}
	    
	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();     
		int X = (int)event.getX(); 
		int Y = (int)event.getY();
	
		switch (eventaction) { 
			//Touch down to check if the finger is on a knob
			case MotionEvent.ACTION_DOWN:
				balID = 0;
				for (Knob knob : knobs) {				
					// check if inside the bounds of the knob(circle)
					// get the centre of the knob
					int centerX = knob.getX() + knob.getBitmap().getHeight();
					int centerY = knob.getY() + knob.getBitmap().getHeight();
	        		// calculate the radius from the touch to the centre of the knob
					double radCircle  = Math.sqrt( (double) (((centerX-X)*(centerX-X)) + (centerY-Y)*(centerY-Y)));
	        		// if the radius is smaller then 33 (radius of a knob is 22), then it must be on the ball
					if (radCircle < 3*knob.getBitmap().getHeight()/2.0){
						balID = knob.getID();
					}
				}
				break; 
			
			 //Touch drag with the knob
	        case MotionEvent.ACTION_MOVE:	
	        	startKnobValueTmp = 0;
	        	endKnobValueTmp = 0;
	        	
	        	// left and right bound of slider and the difference
	        	int left_bound = pos.x + knobs[0].getWidth()/2 + 10;
	        	int right_bound = pos.x + (int)sliderWidth - knobs[0].getWidth()/2 - 10;
	        	int delta_bound = right_bound - left_bound;
	        	
	        	// start and end value from the slider and the difference
	        	int val_max = maxVal;
	        	int val_min = minVal;
	        	int delta_val = val_max-val_min;
	        	
	        	 /*
	        	  * The relative ratio which is later used
	        	  * to calculate the value of the knob using it's position on the X axis
	        	  */
	        	double ratio = (double)delta_bound/delta_val;
	        	
	        	int radiusKnob = knobs[0].getBitmap().getHeight()/2;

	        	 // knob position from centre
	        	int left_knob = knobs[0].getX() + radiusKnob;
	        	int right_knob = knobs[1].getX() + radiusKnob;
	        	
	        	// The calculated knob value using
	        	// the bounds, ratio, and actual knob position 
	        	startKnobValueTmp = (int)((val_max*ratio - right_bound + left_knob)/ratio);
	        	endKnobValueTmp = (int)((val_max*ratio - right_bound + right_knob)/ratio);

	        	//the first knob should be between the left bound and the second knob
	        	if(balID == 1) {
	        		if(X < left_bound) 
	        			X = left_bound;
	        		if(X >= knobs[1].getX()-radiusKnob)
	        			X = knobs[1].getX()-radiusKnob;
	        		knobs[0].setX(X-radiusKnob);
	        		
	        		//if the start value has changed then we pass it to the listener
	        		if(startKnobValueTmp != getStartKnobValue()) {
	        			setStartKnobValue(startKnobValueTmp);
		        		knobValuesChanged(true, false, getStartKnobValue(), getEndKnobValue());	            	
		        	}	            
	        	}
	        	//the second knob should between the first knob and the right bound
	        	if(balID == 2) {
	        		if(X > right_bound) 
	        			X = right_bound;
	        		if(X <= knobs[0].getX() + 3*radiusKnob)
	        			X = knobs[0].getX() + 3*radiusKnob;
	        		knobs[1].setX(X-radiusKnob);	
	        		
	        		//if the end value has changed then we pass it to the listener
		        	if(endKnobValueTmp != getEndKnobValue()) {
	        			setEndKnobValue(endKnobValueTmp);
		        		knobValuesChanged(false, true, getStartKnobValue(), getEndKnobValue());	            	
		        	}
	        	}
	        	break;
	        	
	        // Touch drop - actions after knob is released are performed   
	        case MotionEvent.ACTION_UP:
	        	break; 
		}	        
		
		 // Redraw the canvas
		invalidate();  
		return true; 
	}
	
	//Slider position
	public void setPosition(int X, int Y){
		pos = new Point(X,Y);
	}
	
	public int getStartKnobValue() {
		return startKnobValue;
	}

	public void setStartKnobValue(int startKnobValue) {
		this.startKnobValue = startKnobValue;
	}

	public int getEndKnobValue() {
		return endKnobValue;
	}

	public void setEndKnobValue(int endKnobValue) {
		this.endKnobValue = endKnobValue;
	}
	
	/**
	 * Interface which defines the knob values changed listener method
	 */
	public interface KnobValuesChangedListener {
		void onValuesChanged(boolean knobStartChanged, boolean knobEndChanged, int knobStart, int knobEnd);
	}
	
	/**
	 * Method applied to the instance of SliderView
	 */
	public void setOnKnobValuesChangedListener (KnobValuesChangedListener l) {
		knobValuesChangedListener = l;
	}
	    
	/**
	 * Method used by knob values changed listener
	 */
	private void knobValuesChanged(boolean knobStartChanged, boolean knobEndChanged, int knobStart, int knobEnd) {
		if(knobValuesChangedListener != null)
			knobValuesChangedListener.onValuesChanged(knobStartChanged, knobEndChanged, knobStart, knobEnd);
	}
}