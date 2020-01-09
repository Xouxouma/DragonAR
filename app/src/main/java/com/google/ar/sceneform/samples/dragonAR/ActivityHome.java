package com.google.ar.sceneform.samples.dragonAR;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.content.Intent;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class ActivityHome extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;
    private DatabaseHandler db = new DatabaseHandler(ActivityHome.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (db.numberOfDragons() == 0) {
                    Intent homeIntent = new Intent(ActivityHome.this, CreateDragonActivity.class);
                    startActivity(homeIntent);
                    finish();
                }
                else {
                    Intent homeIntent = new Intent(ActivityHome.this, MainActivity.class);
                    startActivity(homeIntent);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);

    }
}

