package com.m.motion_2;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DISPLAY_DURATION = 4000;

    private ProgressBar progressBar;
    private int progressStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Initialize the progress bar
        progressBar = findViewById(R.id.progressbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();

            // Create a handler to update the progress bar
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Start a background thread to simulate progress
                    new Thread(new Runnable() {
                        public void run() {
                            while (progressStatus < 100) {
                                progressStatus += 1;

                                // Update the progress bar on the UI thread
                                handler.post(new Runnable() {
                                    public void run() {
                                        progressBar.setProgress(progressStatus);
                                    }
                                });

                                try {
                                    Thread.sleep(SPLASH_DISPLAY_DURATION / 100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // After the progress is complete, navigate to the main activity
                            handler.post(new Runnable() {
                                public void run() {
                                    Intent mainIntent = new Intent(SplashActivity.this, login.class);
                                    startActivity(mainIntent);
                                    overridePendingTransition(0,0);
                                    // Close this activity
                                    finish();
                                }
                            });
                        }
                    }).start();
                }
            }, 1000);
        }
    }
}
