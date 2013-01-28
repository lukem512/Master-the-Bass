package com.masterthebass;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class Filters extends Activity {

	public static final String TAG = "com.masterthebass";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filters);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_filters,menu);
		return true;
	}
	
	public void viewlist(View view) {
		Button button = (Button) view;
		//sending tag so i know where to save in array
	    String tag = button.getTag().toString();
	    //now open new Activity with this tag
	    Intent intent = new Intent(this, listV.class);
	    Bundle b = new Bundle();
	    b.putString("tag", tag);
	    intent.putExtras(b);
	    startActivity(intent);
	    }
	
	public void viewlist2(View view) {
		Button button = (Button) view;
		//sending tag so i know where to save in array
	    String tag = button.getTag().toString();
	    //now open new Activity with this tag
	     Intent intent = new Intent(this, listV2.class);
	     Bundle b = new Bundle();
		 b.putString("tag", tag);
		 intent.putExtras(b);
	     startActivity(intent);
	    }

}
