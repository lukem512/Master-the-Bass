package com.prototype.newui;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	
	//like fulter coffee
	private FilterButton lefttop;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.left_button);
		lefttop = new FilterButton(0,0,myBitmap);
		setContentView(new myView(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private class myView extends View{

		public myView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub
			//Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
			//canvas.drawBitmap(myBitmap, 0, 0, null);
			lefttop.draw(canvas);
			
		}
	}

}
