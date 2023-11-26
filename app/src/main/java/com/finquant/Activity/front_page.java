package com.finquant.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.finquant.Utils.Dialog_utils;
import com.google.firebase.database.DatabaseReference;
import com.m.motion_2.R;

public class front_page extends AppCompatActivity {
    AppCompatButton count_fish,tank,archive;
    private DatabaseReference databaseReference;
    TextView logout;
    private static final String CONFIRMED ="CONFIRMED";
    Dialog_utils dialogUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        logout = findViewById(R.id.logout);
        changeStatusBarColor(getResources().getColor(R.color.superBlue));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        dialogUtils = new Dialog_utils();
        showWarning();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
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

        count_fish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Dialog_utils dialog_utils = new Dialog_utils();
            dialog_utils.countFish(front_page.this);
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
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog_utils dialog_utils = new Dialog_utils();
                dialog_utils.logout(front_page.this);
            }

        });

    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }
    private void showWarning() {
        dialogUtils.warning(this);
    }
}