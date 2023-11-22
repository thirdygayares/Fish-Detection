package com.finquant.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.m.motion_2.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_DURATION = 2000;
    private static final int TOTAL_PROGRESS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        TextView textViewProgress = findViewById(R.id.textViewProgress);
        // Hide action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Initialize CircularProgressBar
        CircularProgressBar circularProgressBar = findViewById(R.id.circularProgressBar);

        // Set CircularProgressBar properties
        circularProgressBar.setProgressBarWidth(10f);
        circularProgressBar.setBackgroundProgressBarWidth(5f);
        circularProgressBar.setProgressBarColor(getResources().getColor(R.color.dark_blue));
        circularProgressBar.setBackgroundProgressBarColor(getResources().getColor(R.color.semi));

        // Calculate progress update interval based on total duration and total progress
        long progressUpdateInterval = SPLASH_DISPLAY_DURATION / TOTAL_PROGRESS;

        // Create a handler to update the progress bar
        new Handler().postDelayed(() -> {
            // Start a background thread to simulate progress
            new Thread(() -> {
                for (int progress = 0; progress <= TOTAL_PROGRESS; progress++) {
                    int finalProgress = progress;
                    // Update the progress bar on the UI thread
                    runOnUiThread(() -> {
                        circularProgressBar.setProgressWithAnimation(finalProgress, progressUpdateInterval);
                        textViewProgress.setText(finalProgress + "%");
                    });

                    try {
                        Thread.sleep(progressUpdateInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // After the progress is complete, navigate to the main activity
                runOnUiThread(() -> {
                    Intent mainIntent = new Intent(SplashActivity.this, login.class);
                    startActivity(mainIntent);
                    overridePendingTransition(0, 0);
                    // Close this activity
                    finish();
                });
            }).start();
        }, 1000);
    }
}
