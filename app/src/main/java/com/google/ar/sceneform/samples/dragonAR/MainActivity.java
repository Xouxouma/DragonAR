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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.text.BreakIterator;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    private ModelRenderable fishRenderable;
    private ModelRenderable ballRenderable;

    private boolean dragonHere = false;

    private DatabaseHandler dbh;
    private Dragon dragon;

    private ProgressBar progressBarSatiety;
    private ProgressBar progressBarHappiness;
    private ProgressBar progressBarEnergy;
    private double startSleepTime = -1;
    private TransformableNode andy;
    private TextView textViewName;
    private Button buttonFeed;
    private Button buttonPlay;
    private Button buttonSleep;

    @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }
    setContentView(R.layout.activity_ux);

    dbh = new DatabaseHandler(this);
    progressBarSatiety = findViewById(R.id.progressBarSatiety);
    progressBarHappiness = findViewById(R.id.progressBarHappiness);
    progressBarEnergy = findViewById(R.id.progressBarEnergy);

    progressBarSatiety.getProgressDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
    progressBarHappiness.getProgressDrawable().setColorFilter(Color.CYAN, android.graphics.PorterDuff.Mode.SRC_IN);
    progressBarEnergy.getProgressDrawable().setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);

    getDragonFromDB();

    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    buttonFeed = findViewById(R.id.buttonFeed);
    buttonPlay = findViewById(R.id.buttonPlay);
    buttonSleep = findViewById(R.id.buttonSleep);
    buttonFeed.setVisibility(View.GONE);
    buttonPlay.setVisibility(View.GONE);
    buttonSleep.setVisibility(View.GONE);

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

    ModelRenderable.builder()
            // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
            .setSource(this, Uri.parse("13007_Blue-Green_Reef_Chromis_v2_l3.sfb"))
            .build()
            .thenAccept(renderable -> fishRenderable = renderable)
            .exceptionally(
                    throwable -> {
                        Log.e(TAG, "Unable to load Fish Renderable.", throwable);
                        return null;
                    });

    ModelRenderable.builder()
            // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
            .setSource(this, Uri.parse("ball.sfb"))
            .build()
            .thenAccept(renderable -> ballRenderable = renderable)
            .exceptionally(
                    throwable -> {
                        Log.e(TAG, "Unable to load Fish Renderable.", throwable);
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
          initButtons();

          // Create the Anchor.
          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          // Create the transformable andy and add it to the anchor.
          andy = new TransformableNode(arFragment.getTransformationSystem());

          andy.getScaleController().setMaxScale(0.45f);
          andy.getScaleController().setMinScale(0.449f);
          andy.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1, 0), 50f));

          andy.setParent(anchorNode);
          andy.setRenderable(andyRenderable);
          andy.select();
          andy.getWorldPosition();

        });

  }

    private void getDragonFromDB() {
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c  = db.rawQuery("SELECT * FROM Dragons ORDER BY id DESC LIMIT 1;", null);
        if (c.moveToFirst()) {
            Toast.makeText(getApplicationContext(), "Dragon found in db", Toast.LENGTH_LONG).show();
            dragon = new Dragon(
                    c.getInt(c.getColumnIndex("id")),
                    c.getString(c.getColumnIndex("name")),
                    c.getInt(c.getColumnIndex("gender")),
                    c.getInt(c.getColumnIndex("satiety")),
                    c.getInt(c.getColumnIndex("happiness")),
                    c.getInt(c.getColumnIndex("energy"))
            );
            c.close();
        }
        else {
            Toast.makeText(getApplicationContext(), "No dragon found in db", Toast.LENGTH_LONG).show();
            dragon = new Dragon("TMPDragon", GenderEnum.MASCULIN);
        }
        progressBarSatiety.setProgress(dragon.getSatiety()*10);
        progressBarHappiness.setProgress(dragon.getHappiness()*10);
        progressBarEnergy.setProgress(dragon.getEnergy()*10);
        textViewName = findViewById(R.id.textViewName);
        textViewName.setText(dragon.getName());
    }

    void initButtons(){
        buttonFeed.setVisibility(View.VISIBLE);
        buttonPlay.setVisibility(View.VISIBLE);
        buttonSleep.setVisibility(View.VISIBLE);
        Toast.makeText(getBaseContext(),"Dragon pop",Toast.LENGTH_LONG).show();

        buttonFeed.setOnClickListener(view -> {
          feed();
        });
        buttonPlay.setOnClickListener(view -> {
            play();
        });
        buttonSleep.setOnClickListener(view -> {
            sleep();
        });
  }

    private void sleep() {
        int start_energy = dragon.getEnergy();
        Toast.makeText(getApplicationContext(), "SLEEPING...", Toast.LENGTH_LONG).show();
        if(!dragon.isSleeping) {
            textViewName.setText(dragon.getName() + "(Zzz)");

            findViewById(R.id.buttonSleep).setBackgroundResource(R.drawable.wakeup);
            startSleepTime = dragon.startSleep();
            buttonFeed.setVisibility(View.INVISIBLE);
            buttonPlay.setVisibility(View.INVISIBLE);
        }
        else {
            startSleepTime = -1;
            buttonFeed.setVisibility(View.VISIBLE);
            buttonPlay.setVisibility(View.VISIBLE);
            textViewName.setText(dragon.getName());
            findViewById(R.id.buttonSleep).setBackgroundResource(R.drawable.sleep2);
            dragon.wakeUp(startSleepTime);
            ProgressBarAnimation anim = new ProgressBarAnimation(progressBarEnergy, start_energy * 10, (dragon.getEnergy()) * 10);
            anim.setDuration(1000);
            progressBarEnergy.startAnimation(anim);
        }

        dbh.updateDragon(dragon);
    }

    private void play() {
        if (dragon.isSleeping){
            Toast.makeText(getApplicationContext(), "STILL SLEEPING...", Toast.LENGTH_LONG).show();
            return;
        }
        if (dragon.getEnergy() > 0) {
            Toast.makeText(getApplicationContext(), "PLAYING...", Toast.LENGTH_LONG).show();
            loadBall();
            int start_happiness = dragon.getHappiness();
            int start_energy = dragon.getEnergy();

            dragon.play();

            ProgressBarAnimation anim = new ProgressBarAnimation(progressBarHappiness, start_happiness * 10, dragon.getHappiness() * 10);
            anim.setDuration(1000);
            progressBarHappiness.startAnimation(anim);

            ProgressBarAnimation anim2 = new ProgressBarAnimation(progressBarEnergy, start_energy * 10, (dragon.getEnergy()) * 10);
            anim2.setDuration(1000);
            progressBarEnergy.startAnimation(anim2);

            dbh.updateDragon(dragon);
        }

        else {
            Toast.makeText(getApplicationContext(), "INSUFFICIENT ENERGY", Toast.LENGTH_LONG).show();
        }
    }

    void feed(){
        if (dragon.isSleeping){
            Toast.makeText(getApplicationContext(), "STILL SLEEPING...", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(getApplicationContext(), "FEEDING...", Toast.LENGTH_LONG).show();
        int start_satiety = dragon.getSatiety();
        int start_happiness = dragon.getHappiness();

        loadFish();
        dragon.feed();

        ProgressBarAnimation anim = new ProgressBarAnimation(progressBarSatiety, start_satiety * 10, (dragon.getSatiety() + 1) * 10);
        anim.setDuration(1000);
        progressBarSatiety.startAnimation(anim);

        ProgressBarAnimation anim2 = new ProgressBarAnimation(progressBarHappiness, start_happiness * 10, dragon.getHappiness() * 10);
        anim2.setDuration(1000);
        progressBarHappiness.startAnimation(anim2);

        dbh.updateDragon(dragon);
    }

    private void startWalking(Node node) {
        ObjectAnimator objectAnimation = new ObjectAnimator();
        objectAnimation.setAutoCancel(true);
        objectAnimation.setTarget(node);

        // All the positions should be world positions
        // The first position is the start, and the second is the end.
        objectAnimation.setObjectValues(node.getWorldPosition(), andy.getWorldPosition());

        // Use setWorldPosition to position andy.
        objectAnimation.setPropertyName("worldPosition");

        // The Vector3Evaluator is used to evaluator 2 vector3 and return the next
        // vector3.  The default is to use lerp.
        objectAnimation.setEvaluator(new Vector3Evaluator());
        // This makes the animation linear (smooth and uniform).
        objectAnimation.setInterpolator(new LinearInterpolator());
        // Duration in ms of the animation.
        objectAnimation.setDuration(2000);
        objectAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                node.getParent().setParent(null);
                node.setParent(null);
            }
        });
        objectAnimation.start();
    }

    void loadFish(){
        AnchorNode anchorNode = new AnchorNode();
        anchorNode.setLocalPosition(new Vector3(0.03f,0,-0.1f));
        //Node node = new Node();
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        anchorNode.setRenderable(null);
        TransformableNode fishNode = new TransformableNode(arFragment.getTransformationSystem());
        fishNode.setRenderable(fishRenderable);
        fishNode.setParent(anchorNode);

        //fishNode.getScaleController().setMaxScale(0.099f);
        //fishNode.getScaleController().setMinScale(0.15f);
        fishNode.setLocalRotation(Quaternion.axisAngle(new Vector3(3, 3, 3), 90f));

        startWalking(fishNode);
    }

    void loadBall(){
        AnchorNode anchorNode = new AnchorNode();
        anchorNode.setLocalPosition(new Vector3(0.03f,0,-0.1f));
        //Node node = new Node();
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        anchorNode.setRenderable(null);
        TransformableNode ballNode = new TransformableNode(arFragment.getTransformationSystem());
        ballNode.setRenderable(ballRenderable);
        ballNode.setParent(anchorNode);

        //fishNode.getScaleController().setMaxScale(0.099f);
        //fishNode.getScaleController().setMinScale(0.15f);
        ballNode.setLocalRotation(Quaternion.axisAngle(new Vector3(3, 3, 3), 90f));
        startWalking(ballNode);
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
