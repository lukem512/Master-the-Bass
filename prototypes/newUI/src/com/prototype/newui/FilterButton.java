package com.prototype.newui;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class FilterButton {
	private int X,Y;
	private Bitmap image;
	
	public FilterButton(int x,int y,Bitmap img){
		this.X = x;
		this.Y = y;
		this.image = img;
	}
	/*
	 * returns true if given X and Y coordinates
	 * are in the button triangle
	 */
	public boolean isPressed(int x,int y){
		//TODO: function to check if coord in triangle
		return false;
	}
	
	public void draw(Canvas canvas){
		canvas.drawBitmap(image, X, Y, null);
	}
	
}
