package com.masterthebass;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class listV extends ListActivity{

	public static final String TAG = "com.masterthebass";
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
					com.masterthebass.MainActivity.addTogestureArray(((TextView) arg1).getText(), Integer.parseInt(tag) - 1);
					finish();
				}
			}
		);				
	}	
	static final String[] gesture = new String[]{
		"Tap","Swipe Down","Swipe Up","Swipe Left","Swipe Right","Hold","Tap","Swipe Down","Swipe Up","Swipe Left","Swipe Right","Hold"};		
}
