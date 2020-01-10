package com.google.ar.sceneform.samples.dragonAR;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CreateDragonActivity extends AppCompatActivity {

    EditText dragon_name;

    private DatabaseHandler db = new DatabaseHandler(CreateDragonActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dragon);

        dragon_name = findViewById(R.id.name_dragon);
    }

    public void addDragon(View v) {
        String name = dragon_name.getText().toString();
        if (!name.trim().isEmpty()) {
            db.insertDragon(new Dragon(name, GenderEnum.MASCULIN));
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }

        else{
            Toast.makeText(getApplicationContext(), "Please enter a name", Toast.LENGTH_LONG).show();
        }
    }
}
