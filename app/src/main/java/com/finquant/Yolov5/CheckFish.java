package com.finquant.Yolov5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;
import android.widget.Toast;

import com.finquant.Activity.front_page;
import com.google.common.util.concurrent.ListenableFuture;
import com.m.motion_2.R;
import com.m.motion_2.ml.Classification;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutionException;

public class CheckFish extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextView textView; // For displaying classification result
    private PreviewView viewFinder;
    private int imageSize = 224;
    String result = "";

    ProcessCameraProvider cameraProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_fish);

        textView = findViewById(R.id.textView); // Assuming you have a TextView with id 'textView'
        viewFinder = findViewById(R.id.viewFinder); // For camera preview

        startCamera();
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

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK) // Use back camera
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
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

            // Update the UI directly as this method is called from the UI thread
            runOnUiThread(() -> {
                if ("Fish".equals(classificationResult) && confidencePercentage >= 98) {
                    //textView.setText("Fish Detected");
                    result = "Fish Detected";

                    Intent intent = new Intent(CheckFish.this, DetectorActivity.class);
// Right after startActivity(intent); and before finish();
                    // Optionally add flags or data to the intent
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Use your own animations
                    stopCamera(); // Stop the camera here

                    finish(); // Call finish to destroy this activity

                } else {
                    //textView.setText("No Fish Detected");
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
        stopCamera(); // Ensure the camera is stopped when the activity is paused
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera(); // Ensure the camera is stopped when the activity is destroyed
    }
  public void onBackPressed(){
        Intent i = new Intent(getApplicationContext(), front_page.class);
        startActivity(i);
        finish();
        overridePendingTransition(0,0);
        super.onBackPressed();
  }

}
