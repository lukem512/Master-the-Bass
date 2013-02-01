package com.masterthebass;
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
import java.util.*; 

public class Filterlist extends ListActivity{

	public static final String TAG = "com.masterthebass";
	//getting the filter list ID's
	//adding the filter ID's to array
	private int []filterListID = com.masterthebass.FilterManager.getFiltersList();
	private ArrayList filters  = new ArrayList();
	public void addtofilters(){
		//Adding the filter names to the filter list for the list on screen
		for(int i=0;i<filterListID.length;i++){
			filters.add( com.masterthebass.FilterManager.getFilterName(filterListID[i]));
			Log.e(TAG,"the string added to the array " + com.masterthebass.FilterManager.getFilterName(filterListID[i]));
		}
		filters.add("this is a test");
		filters.add("second test");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addtofilters();
		setListAdapter( new ArrayAdapter<String>(this, R.layout.list_1st_item,filters));
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
		Log.e(TAG,"the item in the list pressed was " + arg2 + " " + arg3 + " ");
		Log.e(TAG,"the button pressed is " + (Integer.parseInt(tag)-1));
		//adding the place in the list that was clicked because this corresponds to the filters id and adding to filterlist 
		//in main activity
		com.masterthebass.MainActivity.addTofilterArray(arg2, Integer.parseInt(tag) - 1);
		finish();
			}
		}
		);
		}
		}
