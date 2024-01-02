package com.finquant.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finquant.Adapter.FishCountAdapter;
import com.finquant.Class.FishCountModel;
import com.finquant.Utils.Dialog_utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.m.motion_2.R;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class front_page extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;



    private DatabaseReference databaseReference;
    private static final String CONFIRMED ="CONFIRMED";
    private RecyclerView recyclerView;
    private AppCompatButton addTankbtn;
    private FishCountAdapter adapter;
    private List<FishCountModel> fishCountList;
    Dialog_utils dialogUtils;
    FloatingActionButton upload;
    private boolean isRowView = true;


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        changeStatusBarColor(getResources().getColor(R.color.superBlue));
        upload = findViewById(R.id.download);
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        dialogUtils = new Dialog_utils();
        Spinner spinner = findViewById(R.id.Spinner);
        EditText searchEditText = findViewById(R.id.search);
        recyclerView = findViewById(R.id.recyclerTank);
        addTankbtn = findViewById(R.id.addtank);
        ImageButton rowViewButton = findViewById(R.id.rowView);
        ImageButton gridViewButton = findViewById(R.id.gridView);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseReference = FirebaseDatabase.getInstance().getReference("tank");
        fishCountList = new ArrayList<>();
        boolean initialLayoutIsRowView = true;
        adapter = new FishCountAdapter(this, fishCountList, initialLayoutIsRowView);
        recyclerView.setAdapter(adapter);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userTanksRef = FirebaseDatabase.getInstance().getReference().child("tank").child(userId);
            databaseReference = userTanksRef;
        } else {
            Toast.makeText(getApplicationContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(front_page.this, login.class);
            startActivity(loginIntent);
            finish(); 
        }
        
        showWarning();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
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

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the data from the Realtime Database
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            // Create a Document
                            Document document = new Document();

                            // Specify the file path for the PDF
                            String filePath = getFilesDir() + "/output.pdf";

                            // Create a PdfWriter instance
                            PdfWriter.getInstance(document, new FileOutputStream(filePath));

                            // Open the document
                            document.open();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                FishCountModel fishCountModel = snapshot.getValue(FishCountModel.class);

                                // Convert the data to the format you want to upload (e.g., to a JSON string)
                                String dataToUpload = convertFishCountModelToJson(fishCountModel);

                                // Add content to the document (e.g., your JSON data)
                                Paragraph paragraph = new Paragraph(dataToUpload);
                                document.add(paragraph);
                            }

                            // Close the document
                            document.close();

                            // Read the generated PDF as bytes
                            byte[] pdfData = new byte[0];

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                pdfData = Files.readAllBytes(Paths.get(filePath));
                            }

                            // Specify the storage path in Firebase Storage where you want to upload the PDF
                            String storagePath = "Tank List/Tank_List.pdf";

                            // Upload the generated PDF to Firebase Storage
                            uploadDataToFirebaseStorage(pdfData, storagePath);

                            Toast.makeText(getApplicationContext(), "PDF generated and uploaded successfully", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "PDF generation and upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle any errors
                    }
                });
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home: {
                        Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), front_page.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0); // Disable animation
                        finish();
                        break;
                    }

                    case R.id.logout: {
                        Toast.makeText(getApplicationContext(), "Logout", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), login.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0); // Disable animation
                        finish();
                        break;
                    }

                }
                return false;

            }
        });

        rowViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRowView = true; // Set flag for row view
                setLayout(isRowView);
                // Set background drawable for row view button
                rowViewButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gray_btn));
                // Reset background drawable for grid view button
                gridViewButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_bg));
            }
        });

        gridViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRowView = false; // Set flag for grid view
                setLayout(isRowView);
                // Set background drawable for grid view button
                gridViewButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gray_btn));
                // Reset background drawable for row view button
                rowViewButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_bg));
            }
        });


        addTankbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog_utils.showFishCountDialog2(0,front_page.this);
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed for this implementation
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchText = charSequence.toString().trim().toLowerCase();
                filterData(searchText);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed for this implementation
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fishCountList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FishCountModel fishCountModel = snapshot.getValue(FishCountModel.class);
                    fishCountList.add(fishCountModel);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });

    ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDayOfWeek = spinner.getSelectedItem().toString();

                if (!selectedDayOfWeek.equals("None")) {
                    filterDataByDay(selectedDayOfWeek);
                } else {
                    // Handle the case when "None" is selected (show all data)
                    // Simply notify the adapter with the original unfiltered data
                    adapter.filterList(fishCountList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case when nothing is selected
            }
        });
        
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fishCountList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FishCountModel fishCountModel = snapshot.getValue(FishCountModel.class);
                    fishCountList.add(fishCountModel);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
        

    }

    private void uploadDataToFirebaseStorage(byte[] data, String storagePath) {
        // Get a reference to your Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to the file in Firebase Storage
        StorageReference dataRef = storageRef.child(storagePath);

        // Upload the data to Firebase Storage
        UploadTask uploadTask = dataRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Data uploaded successfully
                Toast.makeText(getApplicationContext(), "Data uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();

                // Get the download URL of the uploaded file
                dataRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Create an intent to open the web browser and let the user download the file
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the upload failure
                Toast.makeText(getApplicationContext(), "Data upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String convertFishCountModelToJson(FishCountModel fishCountModel) {
        // Format the tank data as a string
        String tankData = "Tank Name: " + fishCountModel.getTankName() +
                " ; Fish Count: " + fishCountModel.getFishCount() +
                " ; TimeStamp: " + fishCountModel.getTimestamp();
        return tankData;
    }


    private void setLayout(boolean isRowView) {
        RecyclerView.LayoutManager layoutManager;
        if (isRowView) {
            layoutManager = new LinearLayoutManager(this);
        } else {
            layoutManager = new GridLayoutManager(this, 2); // Set the span count as needed
        }
        recyclerView.setLayoutManager(layoutManager);
        adapter.setLayout(isRowView); // Pass the layout type to the adapter
    }

    private void filterData(String searchText) {
        List<FishCountModel> filteredList = new ArrayList<>();
        for (FishCountModel fishCountModel : fishCountList) {
            String tankName = fishCountModel.getTankName().toLowerCase();
            if (tankName.contains(searchText.toLowerCase())) {
                filteredList.add(fishCountModel);
            }
        }
        adapter.notifyDataSetChanged();
        adapter.filterList(filteredList);
    }


    private void filterDataByDay(String selectedDayOfWeek) {
        List<FishCountModel> filteredList = new ArrayList<>();
        for (FishCountModel fishCountModel : fishCountList) {
            String dayOfWeek = getDayOfWeekFromTimestamp(fishCountModel.getTimestamp());
            if (dayOfWeek.equalsIgnoreCase(selectedDayOfWeek)) {
                filteredList.add(fishCountModel);
            }
        }
        adapter.filterList(filteredList);
    }

    private String getDayOfWeekFromTimestamp(String timestamp) {
        try {
            // Define the date format using the default locale and the custom format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

            // Parse the timestamp string into a Date object
            Date date = dateFormat.parse(timestamp);

            // Define a format to get the day of the week using the default locale
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

            // Format the date to get the day of the week
            String dayOfWeek = dayFormat.format(date);

            return dayOfWeek;
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }
    private void showWarning() {
        dialogUtils.warning(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}