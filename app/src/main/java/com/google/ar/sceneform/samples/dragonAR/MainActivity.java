/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.dragonAR;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  private ArFragment arFragment;
  private ModelRenderable andyRenderable;

  private boolean dragonHere = false;

  private DatabaseHandler dbh;
  private Dragon dragon;

    @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }
    dbh = new DatabaseHandler(this);
    getDragonFromDB();

    setContentView(R.layout.activity_ux);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
    ModelRenderable.builder()
        .setSource(this, Uri.parse("Night_Fury.sfb"))
        .build()
        .thenAccept(renderable -> andyRenderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });

    arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (andyRenderable == null) {
            return;
          }

          if (dragonHere){
              return;
          }
          dragonHere = true;
          // Create the Anchor.
          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          // Create the transformable andy and add it to the anchor.
          TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());

          andy.getScaleController().setMaxScale(0.45f);
          andy.getScaleController().setMinScale(0.449f);
          andy.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1, 0), 50f));

          andy.setParent(anchorNode);
          andy.setRenderable(andyRenderable);
          andy.select();
        });

    initButtons();
  }

    private void getDragonFromDB() {
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c  = db.rawQuery("SELECT * FROM Dragons ORDER BY id DESC LIMIT 1;", null);
        if (c.moveToFirst()) {
            Toast.makeText(getApplicationContext(), "Dragon found in db", Toast.LENGTH_LONG).show();
            dragon = new Dragon(
                    c.getString(c.getColumnIndex("name")),
                    c.getInt(c.getColumnIndex("gender")),
                    c.getInt(c.getColumnIndex("satiety")),
                    c.getInt(c.getColumnIndex("happiness")),
                    c.getInt(c.getColumnIndex("energy"))
            );
        }
        else {
            Toast.makeText(getApplicationContext(), "No dragon found in db", Toast.LENGTH_LONG).show();
            dragon = new Dragon("TMPDragon", GenderEnum.MASCULIN);
        }
  }

    void initButtons(){
        // Feed
        findViewById(R.id.buttonFeed).setOnClickListener(view -> {
          feed();
        });
        findViewById(R.id.buttonPlay).setOnClickListener(view -> {
            play();
        });
        findViewById(R.id.buttonSleep).setOnClickListener(view -> {
            sleep();
        });
  }

    private void sleep() {
        Toast.makeText(getApplicationContext(), "SLEEPING...", Toast.LENGTH_LONG).show();
    }

    private void play() {
        Toast.makeText(getApplicationContext(), "PLAYING...", Toast.LENGTH_LONG).show();
    }

    void feed(){
      Toast.makeText(getApplicationContext(), "FEEDING...", Toast.LENGTH_LONG).show();
      dragon.feed();
    }


  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }

}
