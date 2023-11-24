package com.finquant.Activity;
import androidx.annotation.NonNull;
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
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.finquant.Utils.Dialog_utils;
import com.finquant.Utils.PreferenceUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.finquant.Class.FishCountModel;
import com.finquant.Service.MyService;
import com.m.motion_2.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class front_page extends AppCompatActivity {
    AppCompatButton count_fish,tank,archive;
    private DatabaseReference databaseReference;
    TextView logout;
    private List<FishCountModel> fishCountList;
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
        openSettings();
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
        fishCountList = new ArrayList<>();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userTanksRef = FirebaseDatabase.getInstance().getReference().child("tank").child(userId);
            databaseReference = userTanksRef;
        } else {
            Toast.makeText(getApplicationContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(getApplicationContext(), login.class);
            startActivity(loginIntent);
            finish(); // Optional: Close the current activity so users can't go back without login
        }
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fishCountList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FishCountModel fishCountModel = snapshot.getValue(FishCountModel.class);
                    fishCountList.add(fishCountModel);
                }
                if (!fishCountList.isEmpty()) {
                    startService(new Intent(getApplicationContext(), MyService.class));
                } else {
                    Intent clearNotificationIntent = new Intent("CLEAR_NOTIFICATION_ACTION");
                    sendBroadcast(clearNotificationIntent);
                    stopService(new Intent(getApplicationContext(), MyService.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });

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

    private void openSettings() {
        dialogUtils.foreground(this);
    }
    private void showWarning() {
        dialogUtils.warning(this);
    }
}