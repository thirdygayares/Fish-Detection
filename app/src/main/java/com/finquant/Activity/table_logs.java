package com.finquant.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.finquant.Adapter.CustomHeaderAdapter;
import com.finquant.Adapter.SimpleTableHeaderAdapter2;
import com.finquant.Adapter.TankLogTableDataAdapter;
import com.finquant.Class.FishCountModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.m.motion_2.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

public class table_logs extends AppCompatActivity {
    private TableView<FishCountModel> tableView;
    private DatabaseReference databaseReference;
    private List<FishCountModel> fishCountModelList;
    ImageView back,rotation,delete_table;
    TextView tankNameLog;
    String tankName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_logs);
        back = findViewById(R.id.back);
        tankNameLog = findViewById(R.id.tanKname);
        rotation = findViewById(R.id.rotate);
        delete_table = findViewById(R.id.delete);
        changeStatusBarColor(getResources().getColor(R.color.superBlue));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Retrieve tankName from the intent
        Intent intent = getIntent();
        tankName = intent.getStringExtra("tankName");

        // Use tankName as needed, for example, in a log statement
        if (tankName != null) {
            // Log the tankName
            System.out.println("Tank Name: " + tankName);
        }
        tankNameLog.setText("Tank Name : " +tankName);

        tableView = findViewById(R.id.tableView);
        fishCountModelList = new ArrayList<>();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        rotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

        delete_table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });


        // Set up Firebase references
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userTanksRef = FirebaseDatabase.getInstance().getReference().child("tankLogs").child(userId);
            databaseReference = userTanksRef;
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fishCountModelList.clear(); // Clear the list before updating
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FishCountModel fishCountModel = snapshot.getValue(FishCountModel.class);
                    if (fishCountModel != null) {
                        // Check if the tankName matches the current tank's name
                        if (tankName.equals(fishCountModel.getTankName())) {
                            fishCountModelList.add(fishCountModel);
                        }
                    }
                }

                displayDataInTableView(fishCountModelList);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(table_logs.this);
        builder.setTitle("Delete Tank?");
        builder.setMessage("Are you sure you want to delete all logs for this tank?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteLogsForTank();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void deleteLogsForTank() {
        // Get the current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Create a Firebase reference to the "tankLogs" node for the current user
            DatabaseReference logsRef = FirebaseDatabase.getInstance().getReference().child("tankLogs").child(userId);

            // Query for logs to delete based on tankName
            logsRef.orderByChild("tankName").equalTo(tankName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot logSnapshot : dataSnapshot.getChildren()) {
                        // Remove the matching log node
                        logSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Handle the deletion success
                                Toast.makeText(table_logs.this, "Logs deleted successfully for Tank: " + tankName, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle the deletion failure
                                Toast.makeText(table_logs.this, "Error deleting logs for Tank: " + tankName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors or onCancelled events
                }
            });
        }
    }
    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    private void displayDataInTableView(List<FishCountModel> fishCountModelList) {
        // Sort the list based on timestamp in descending order
        Collections.sort(fishCountModelList, new Comparator<FishCountModel>() {
            @Override
            public int compare(FishCountModel model1, FishCountModel model2) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    String timestamp1 = model1.getTimestamp();
                    String timestamp2 = model2.getTimestamp();

                    // Null checks
                    if (timestamp1 == null || timestamp2 == null) {
                        // Handle the case where timestamp is null
                        return 0; // or another appropriate value
                    }

                    Date date1 = sdf.parse(timestamp1);
                    Date date2 = sdf.parse(timestamp2);

                    // Sort in descending order
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter2(this, "Tank Name", "Fish Count", "Time Stamp"));
        tableView.setDataAdapter(new TankLogTableDataAdapter(this, fishCountModelList));
        // Provide your own colors for alternating rows
        int colorEven = getResources().getColor(R.color.white);
        int colorOdd = getResources().getColor(R.color.white);
        tableView.setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(colorEven, colorOdd));
    }

    public void onBackPressed(){
        Intent i = new Intent(getApplicationContext(),front_page.class);
        startActivity(i);
        overridePendingTransition(0,0);
        finish();
        super.onBackPressed();
    }
}
