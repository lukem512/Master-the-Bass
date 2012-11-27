package com.example.uitest;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class RunActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_run, menu);
        return true;
    }

}
