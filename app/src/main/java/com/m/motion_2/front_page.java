package com.m.motion_2;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class front_page extends AppCompatActivity {
    AppCompatButton count_fish,tank,archive;
    TextView logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(getApplicationContext(), MyService.class));
        setContentView(R.layout.activity_front_page);
        logout = findViewById(R.id.logout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        count_fish = findViewById(R.id.count_fish);
        archive = findViewById(R.id.archive_tank);
        tank = findViewById(R.id.tank_list);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Intent intent = new Intent();
//            String packageName = getPackageName();
//            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                intent.setData(Uri.parse("package:" + packageName));
//                startService(new Intent(getApplicationContext(), MyService.class));
//                startActivity(intent);
//            }
//        }
        showWarning();

        count_fish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an AlertDialog to confirm counting fish
                AlertDialog.Builder builder = new AlertDialog.Builder(front_page.this);
                builder.setTitle("Confirm Fish Count");
                builder.setMessage("Are you sure you want to count fish?");

                // Add a positive button (Yes)
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Proceed to the CountAct activity
                        Intent i = new Intent(getApplicationContext(), CountAct.class);
                        startActivity(i);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                });

                // Add a negative button (No)
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, simply close the dialog
                        dialog.dismiss();
                    }
                });

                // Show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        archive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),Fish_count_list2.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        tank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),Fish_count_list.class);
                startService(new Intent(getApplicationContext(),MyService.class));
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an AlertDialog to confirm counting fish
                AlertDialog.Builder builder = new AlertDialog.Builder(front_page.this);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to Logout?");

                // Add a positive button (Yes)
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Proceed to the CountAct activity
                        Intent i = new Intent(getApplicationContext(), login.class);
                        FirebaseAuth.getInstance().signOut();
                        startActivity(i);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                });

                // Add a negative button (No)
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, simply close the dialog
                        dialog.dismiss();
                    }
                });

                // Show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }


    private void showWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("This application has been specifically developed for the sole purpose of fish counting");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The user clicked the OK button
                dialog.dismiss();
            }
        });
        builder.setCancelable(false); // Prevent the user from dismissing the dialog by tapping outside of it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
