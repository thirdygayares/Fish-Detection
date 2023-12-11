package com.finquant.Yolov5;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.finquant.Utils.Dialog_utils;
import com.finquant.Yolov5.customview.OverlayView;
import com.finquant.Yolov5.env.ImageUtils;
import com.finquant.Yolov5.env.Logger;
import com.finquant.Yolov5.env.Utils;
import com.finquant.Yolov5.tflite.Classifier;
import com.finquant.Yolov5.tflite.YoloV5Classifier;
import com.finquant.Yolov5.tracking.MultiBoxTracker;
import com.m.motion_2.R;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        cameraButton = findViewById(R.id.cameraButton);
        detectButton = findViewById(R.id.detectButton);
        imageView = findViewById(R.id.imageView);

        cameraButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DetectorActivity.class)));

        Intent intent = getIntent();
        if (intent != null) {
            Log.d("MainActivity", "intent != null");

            String path = intent.getStringExtra("path");
            Log.d("MainActivity", "path: " + path);

            if (path != null) {
                sourceBitmap = BitmapFactory.decodeFile(path);
                if (sourceBitmap != null) {
                    cropBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE);
                    if (cropBitmap != null) {
                        imageView.setImageBitmap(cropBitmap);
                    } else {
                        // Handle the error, show message to the user or log
                        Log.e("MainActivity", "Failed to process the bitmap.");
                    }
                } else {
                    // Handle the error, show message to the user or log
                    Log.e("MainActivity", "Failed to load bitmap from assets.");
                }
            }
        }




        detectButton.setOnClickListener(v -> {
            Handler handler1 = new Handler();

            new Thread(() -> {
                final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
                handler1.post(new Runnable() {
                    @Override
                    public void run() {
                        handleResult(cropBitmap, results);
                    }
                });
            }).start();

        });


        Handler handler = new Handler(Looper.getMainLooper());

        // This will post the Runnable to be executed after 1000 milliseconds (1 second)
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Simulate the button click
                detectButton.performClick();
            }
        }, 1000); // 1000 milliseconds delay



        initBox();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        System.err.println(Double.parseDouble(configurationInfo.getGlEsVersion()));
        System.err.println(configurationInfo.reqGlEsVersion >= 0x30000);
        System.err.println(String.format("%X", configurationInfo.reqGlEsVersion));
    }

    private static final Logger LOGGER = new Logger();

    public static final int TF_OD_API_INPUT_SIZE = 640;

    private static final boolean TF_OD_API_IS_QUANTIZED = false;

    private static final String TF_OD_API_MODEL_FILE = "best-fp16.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/fish.txt";

    // Minimum detection confidence to track a detection.
    private static final boolean MAINTAIN_ASPECT = true;
    private Integer sensorOrientation = 90;

    private Classifier detector;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    private OverlayView trackingOverlay;

    protected int previewWidth = 0;
    protected int previewHeight = 0;

    private Bitmap sourceBitmap;
    private Bitmap cropBitmap;

    private Button cameraButton, detectButton;
    private ImageView imageView;

    private void initBox() {
        previewHeight = TF_OD_API_INPUT_SIZE;
        previewWidth = TF_OD_API_INPUT_SIZE;
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        tracker = new MultiBoxTracker(this);
        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> tracker.draw(canvas));

        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, sensorOrientation);

        try {
            detector =
                    YoloV5Classifier.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED,
                            TF_OD_API_INPUT_SIZE);

//            Handler handler = new Handler();
//
//            new Thread(() -> {
//                final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        handleResult(cropBitmap, results);
//                    }
//                }, 1000);
//            }).start();

        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

        int count = 0; // Initialize the counter outside the loop.

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                canvas.drawRect(location, paint);
                cropToFrameTransform.mapRect(location);

                result.setLocation(location);
                mappedRecognitions.add(result);
                count++; // Increment the count for each detection.

            }
        }

        Log.d("TAG", "Count: " + count);

        Dialog_utils.showFishCountDialog(count, MainActivity.this);

//        tracker.trackResults(f, new Random().nextInt());
//        trackingOverlay.postInvalidate();
        //imageView.setImageBitmap(bitmap);
    }
}
