package com.test.gesturetest;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class listV2 extends ListActivity{

	public static final String TAG = "com.test.gesturetest";

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setListAdapter( new ArrayAdapter<String>(this, R.layout.list_1st_item,gesture));
			ListView list = getListView();
			list.setTextFilterEnabled(true);
			list.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), ((TextView) arg1).getText(), Toast.LENGTH_SHORT).show();
						Intent intent = getIntent();
						String tag = intent.getStringExtra("tag");
						Log.e(TAG,"the button pressed is " + (Integer.parseInt(tag)-1));					
						com.test.gesturetest.MainActivity.addToactionArray(((TextView) arg1).getText(), Integer.parseInt(tag) - 1);
						finish();
					}
				}
			);				
		}	
		static final String[] gesture = new String[]{
			"Noise", "Distortion", "Band-pass", "Low-pass", "High-pass", "Square- wave"};
			}