package com.google.ar.sceneform.samples.dragonAR;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class ActivityUX extends AppCompatActivity {

    public void feed(View view){
        Toast.makeText(getApplicationContext(), "FEEDING...", Toast.LENGTH_LONG).show();
    }
}
