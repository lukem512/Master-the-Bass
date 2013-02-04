package com.masterthebass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class Knob  {
	private Bitmap img; // the image of the knob
	private int coordX = 0; // the x coordinate at the canvas
	private int coordY = 0; // the y coordinate at the canvas
	private int id; // gives every knob his own id
 
	public Knob(Context context, int drawable) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		img = BitmapFactory.decodeResource(context.getResources(), drawable); 
	}
		
	public Knob(Context context, int drawable, Point point) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		img = BitmapFactory.decodeResource(context.getResources(), drawable);
		coordX= point.x;
		coordY = point.y;
	}
				
	void setX(int newValue) {
		coordX = newValue;
	}
		
	public int getX() {
		return coordX;
	}

	void setY(int newValue) {
		coordY = newValue;
	}
		
	public int getY() {
		return coordY;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
		
	public Bitmap getBitmap() {
		return img;
	}
	public int getHeight(){
		return img.getHeight();
	}
	
	public int getWidth(){
		return img.getWidth();
	}
}