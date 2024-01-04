package com.finquant.Yolov5;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.finquant.Activity.front_page;
import com.finquant.Utils.Dialog_utils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.m.motion_2.R;
import com.m.motion_2.ml.Classification;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CheckFish2 extends AppCompatActivity {
    private boolean isFishDetected = false;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextView textView,timeText; // For displaying classification result
    private PreviewView viewFinder;
    private int imageSize = 224;
    private int fishCount = 0;
    private static final long START_TIME = 10000; // Initial time in milliseconds
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = START_TIME;

    String result = "";
    AppCompatButton countBtn,restartBtn,cancelBtn;
    ProcessCameraProvider cameraProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_check_fish2);
        timeText = findViewById(R.id.Timer2);
        countBtn = findViewById(R.id.count_btn2);
        restartBtn = findViewById(R.id.restart_button2);
        cancelBtn = findViewById(R.id.cancels2);

        textView = findViewById(R.id.textView2);
        viewFinder = findViewById(R.id.viewFinder2);

        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartActivity();
            }
        });


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cancel = new Intent(getApplicationContext(),front_page.class);
                startActivity(cancel);
                finish();
                overridePendingTransition(0,0);

            }
        });

        countBtn.setOnClickListener(v -> {
                startCamera();

        });
    }

    public void restartActivity() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Restarting...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                progressDialog.dismiss();
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                startActivity(intent);
                overridePendingTransition(0, 0);
                stopCamera();
            }
        }.start();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Cancel the previous timer if it exists
        }

        countDownTimer = new CountDownTimer(START_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                timeText.setText("0");
                //savedData from the tankName
                saveData();
            }
        }.start();

        timerRunning = true;
    }


    private void saveData() {
        Dialog_utils.showFishCountDialog3(CheckFish2.this, fishCount);
    }

    private void updateTimer() {
        int seconds = (int) (timeLeftInMillis / 1000);
        timeText.setText(String.valueOf(seconds));
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors (including InterruptedException)
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CameraStart", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void bindCameraAnalysis(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), image -> {
            if (image.getFormat() == ImageFormat.YUV_420_888) {
                Bitmap bitmap = toBitmap(image); // Convert image to Bitmap
                String classificationResult = classifyImage(bitmap); // Classify the image
                runOnUiThread(() -> textView.setText(classificationResult)); // Update UI
                image.close(); // Close the image
            }
        });

        // Bind each use case separately to avoid conflicts
        try {
            // Unbind any previous use cases before rebinding
            cameraProvider.unbindAll();

            // Bind the preview use case
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview);

            // Bind the image analysis use case
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalysis);
        } catch (Exception e) {
            // Handle any errors during camera setup
            Toast.makeText(this, "Error setting up camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("CameraSetup", "Error setting up camera", e);
        }
    }

    private Bitmap toBitmap(ImageProxy image) {
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public String classifyImage(Bitmap image) {
        try {
            Classification classification = Classification.newInstance(getApplicationContext());

            // Ensure the Bitmap is the correct size
            Bitmap scaledImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            scaledImage.getPixels(intValues, 0, scaledImage.getWidth(), 0, 0, scaledImage.getWidth(), scaledImage.getHeight());

            // Load the pixel data into the ByteBuffer for the model
            for (int val : intValues) {
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Classification.Outputs outputs = classification.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"Fish", "Others"};
            final String classificationResult = classes[maxPos];
            final float confidencePercentage = confidences[maxPos] * 100;
            runOnUiThread(() -> {
                if ("Fish".equals(classificationResult) && confidencePercentage >= 98 && !isFishDetected) {
                    textView.setText("Fish Detected");
                    result = "Fish Detected";

                    fishCount++;

                    // Set the flag to true once a fish is detected
                    isFishDetected = true;
                    // Save data only when fishCount is greater than zero
                    if (fishCount > 0) {
                        saveData();
                    }
                }

                // If fish is detected and the flag is set, keep showing "Fish Detected"
                if (isFishDetected) {
                    textView.setText("Fish Detected");
                    result = "Fish Detected";
                } else {
                    textView.setText("No Fish Detected");
                    result = "No Fish Detected";
                }
            });

            // Log the results
            String s = "";
            for (int i = 0; i < classes.length; i++) {
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }
            Log.d("RESULT", "Confidence: " + s);

            // Releases model resources if no longer used.
            classification.close();

        } catch (IOException e) {
            Log.e("CheckFish", "Failed to load image", e);
            Toast.makeText(getApplicationContext(), "Failed to load image! Try again later", Toast.LENGTH_SHORT).show();
        }

        return result;
    }

    protected void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll(); // This unbinds all use cases from the lifecycle
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (timerRunning) {
            countDownTimer.cancel();
            stopCamera(); // Ensure the camera is stopped when the activity is paused
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerRunning) {
            countDownTimer.cancel();
            stopCamera(); // Ensure the camera is stopped when the activity is destroyed

        }
    }
    public void onBackPressed(){
        Intent i = new Intent(getApplicationContext(), front_page.class);
        startActivity(i);
        finish();
        overridePendingTransition(0,0);
        super.onBackPressed();
    }

}
