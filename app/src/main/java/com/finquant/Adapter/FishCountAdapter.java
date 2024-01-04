package com.finquant.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.finquant.Class.FishCountModel;
import com.finquant.Yolov5.CameraActivity;
import com.finquant.Yolov5.CheckFish;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
public class FishCountAdapter extends RecyclerView.Adapter<FishCountAdapter.ViewHolder> {

    private Context context;
    private List<FishCountModel> fishCountList;
    private boolean isRowView;

    public FishCountAdapter(Context context, List<FishCountModel> fishCountList, boolean isRowView) {
        this.context = context;
        this.fishCountList = fishCountList;
        this.isRowView = isRowView; // Initializing the isRowView field
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FishCountModel fishCountModel = fishCountList.get(position);

        // Set the tank name and timestamp in their respective TextViews
        holder.setTankName("Tank name: " + fishCountModel.getTankName());
        holder.setTimestamp("Time Stamp: " + fishCountModel.getTimestamp());

        // Set the integer fish count directly in the TextView
        holder.setFishCount("Fish count: " + fishCountModel.getFishCount());

        if (isRowView) {
            // Hide the extra view in the grid layout
            holder.hideExtraView();
        } else {
            // Show the extra view in the row layout
            holder.showCards();
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Show an alert dialog with "Rename" and "Delete" options
                showOptionsDialog(position);
                return true;
            }
        });
    }

    private void showOptionsDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Options");
        builder.setItems(new CharSequence[]{"Count", "Rename Tank", "Delete", "Generate Manual PDF"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        startCameraActivity(position);
                        break;
                    case 1:
                        showRenameProgressDialog(position);
                        break;
                    case 2:
                        showDelete(position);
                        break;
                    case 3:
                        ShowgeneratedPdf(position);
                        break;
                }
            }
        });

        builder.create().show();
    }

    private void startCameraActivity(int position) {
        FishCountModel fishCountModel = fishCountList.get(position);
        String tankName = fishCountModel.getTankName();
        Intent intent = new Intent(context, CheckFish.class);
        // Pass the tank name as an extra to the intent
        intent.putExtra("tankName", tankName);
        // Start the CameraActivity
        context.startActivity(intent);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.overridePendingTransition(0, 0);
            activity.finish();
        }
    }


    private void ShowgeneratedPdf(int position) {
        if (position >= 0 && position < fishCountList.size()) {
            FishCountModel fishCountModel = fishCountList.get(position);

            // Generate PDF for the specific FishCountModel
            try {
                // Create a Document
                Document document = new Document();

                // Specify the file path for the PDF
                String filePath = context.getFilesDir() + "/output_" + fishCountModel.getTankName() + ".pdf";

                // Create a PdfWriter instance
                PdfWriter.getInstance(document, new FileOutputStream(filePath));

                // Open the document
                document.open();

                // Convert the data to the format you want to upload (e.g., to a JSON string)
                String dataToUpload = convertFishCountModelToJson(fishCountModel);

                // Add content to the document (e.g., your JSON data)
                Paragraph paragraph = new Paragraph(dataToUpload);
                document.add(paragraph);

                // Close the document
                document.close();

                // Read the generated PDF as bytes
                byte[] pdfData = new byte[0];

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    pdfData = Files.readAllBytes(Paths.get(filePath));
                }

                // Specify the storage path in Firebase Storage where you want to upload the PDF
                String storagePath = "Tank List/" + fishCountModel.getTankName() + "_List.pdf";

                // Upload the generated PDF to Firebase Storage
                uploadDataToFirebaseStorage(pdfData, storagePath);

                Toast.makeText(context, "PDF generated and uploaded successfully", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "PDF generation and upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle invalid position
            Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show();
        }
    }

    private String convertFishCountModelToJson(FishCountModel fishCountModel) {
        // Create a JSON object or string representation of the FishCountModel data
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("tankName", fishCountModel.getTankName());
            jsonObject.put("fishCount", fishCountModel.getFishCount());
            jsonObject.put("timestamp", fishCountModel.getTimestamp());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    private void uploadDataToFirebaseStorage(byte[] pdfData, String storagePath) {
        // Get a reference to your Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference
        StorageReference storageRef = storage.getReference();

        // Create a reference to the file in Firebase Storage
        StorageReference dataRef = storageRef.child(storagePath);

        // Upload the PDF data to Firebase Storage
        UploadTask uploadTask = dataRef.putBytes(pdfData);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Data uploaded successfully
                Toast.makeText(context, "Data uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();

                // Get the download URL of the uploaded file
                dataRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Create an intent to open the web browser and let the user download the file
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        context.startActivity(intent);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the upload failure
                Toast.makeText(context, "Data upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showDelete(final int position) {
        FishCountModel fishCountModel = fishCountList.get(position);
        String TankName = fishCountModel.getTankName();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Tank?");
        builder.setMessage("Are you sure you want to Archive Tank name: " + TankName);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDeleteProgressDialog(position);
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

    private void showDeleteProgressDialog(final int position) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting Tank...");
        progressDialog.show();

        // Get the tank name of the tank you want to delete
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get the tank name of the tank you want to delete
            FishCountModel fishCountModel = fishCountList.get(position);
            String tankNameToDelete = fishCountModel.getTankName();
            String userId = currentUser.getUid();

            // Create a Firebase reference to the "archived_tank" node for the current user
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference tankRef = database.getReference("tank").child(userId);

            // Query for the tank to delete based on tankName
            tankRef.orderByChild("tankName").equalTo(tankNameToDelete).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot tankSnapshot : dataSnapshot.getChildren()) {
                        // Remove the matching tank node
                        tankSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Tank delete Success Tank Name: " + tankNameToDelete, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                notifyDataSetChanged();// Dismiss the progress dialog
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Error delete Tank Name: " + tankNameToDelete, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss(); // Dismiss the progress dialog in case of an error
                                // Handle the error if the deletion fails
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressDialog.dismiss(); // Dismiss the progress dialog in case of an error
                    // Handle any errors or onCancelled events
                }
            });
        } else {
            progressDialog.dismiss(); // Dismiss the progress dialog if the user is not authenticated
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }


    private void showArchiveProgressDialog(final int position) {
        FishCountModel fishCountModel = fishCountList.get(position);
        String TankName = fishCountModel.getTankName();
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Archiving Tank...");
        progressDialog.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Archive Tank");
        builder.setMessage("Are you sure you want to Archive Tank name: " + TankName);

        builder.setPositiveButton("Archive", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                archiveTank(position, progressDialog);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    private void archiveTank(int position, ProgressDialog progressDialog) {
        progressDialog.show();
        FishCountModel fishCountModel = fishCountList.get(position);
        String tankNameToArchive = fishCountModel.getTankName();
        String timeStampToArchive = fishCountModel.getTimestamp();
        int fishCountToArchive = fishCountModel.getFishCount();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseDatabase sourceDatabase = FirebaseDatabase.getInstance();
            DatabaseReference sourceTankRef = sourceDatabase.getReference("tank").child(userId);

            sourceTankRef.orderByChild("tankName").equalTo(tankNameToArchive)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot tankSnapshot : dataSnapshot.getChildren()) {
                                tankSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase targetDatabase = FirebaseDatabase.getInstance();
                                        DatabaseReference targetTankRef = targetDatabase.getReference("archived_tank").child(userId);
                                        DatabaseReference archivedTankRef = targetTankRef.push();
                                        archivedTankRef.child("fishCount").setValue(fishCountToArchive);
                                        archivedTankRef.child("tankName").setValue(tankNameToArchive);
                                        archivedTankRef.child("timeStamp").setValue(timeStampToArchive);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Tank Archive Success Tank Name: " + tankNameToArchive, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to archive Tank Name: " + tankNameToArchive, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            progressDialog.dismiss();
                        }
                    });

        } else {
            // User not authenticated or something went wrong
            progressDialog.dismiss();
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }


    private void showRenameProgressDialog(final int position) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Renaming Tank...");
        progressDialog.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rename Tank");

        // Get the current tank name
        FishCountModel fishCountModel = fishCountList.get(position);
        String oldTankName = fishCountModel.getTankName();

        final EditText newTankNameInput = new EditText(context);
        newTankNameInput.setHint("Enter New Tank Name");
        newTankNameInput.setText(oldTankName);
        builder.setView(newTankNameInput);
        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTankName = newTankNameInput.getText().toString().trim();
                if (!newTankName.isEmpty()) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        // Get the old tank name
                        FishCountModel fishCountModel = fishCountList.get(position);
                        String oldTankName = fishCountModel.getTankName();
                        String userId = currentUser.getUid();

                        // Create a Firebase reference to the "tank" node for the current user
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference tankRef = database.getReference("tank").child(userId);

                        // Query for the tank to rename based on oldTankName
                        tankRef.orderByChild("tankName").equalTo(oldTankName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot tankSnapshot : dataSnapshot.getChildren()) {
                                    // Update the "tankName" field with the newTankName
                                    tankSnapshot.getRef().child("tankName").setValue(newTankName).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Successfully renamed the tank by tankName
                                            progressDialog.dismiss();
                                            notifyDataSetChanged();
                                            // Dismiss the progress dialog

                                            //old tank name toast message to new tank name
                                            Toast.makeText(context, "Renamed Tank: " + oldTankName + " to " + newTankName, Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss(); // Dismiss the progress dialog in case of an error
                                            // Handle the error if the renaming fails
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                progressDialog.dismiss(); // Dismiss the progress dialog in case of an error
                                // Handle any errors or onCancelled events
                            }
                        });
                    } else {
                        progressDialog.dismiss(); // Dismiss the progress dialog if the user is not authenticated
                        Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss(); // Dismiss the progress dialog if renaming is not performed
                    Toast.makeText(context, "New tank name is required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss(); // Dismiss the progress dialog if renaming is canceled
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    @Override
    public int getItemCount() {
        return fishCountList.size();
    }

    public void filterList(List<FishCountModel> filteredList) {
        fishCountList = filteredList;
        notifyDataSetChanged();
    }

    public void setLayout(boolean isRowView) {
        this.isRowView = isRowView;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView fishCountTextView;
        private TextView timestampTextView;
        private TextView tankNameTextView;
        private RelativeLayout relativeLayout;
        private View extraView;
        private CardView grid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.rl);
            fishCountTextView = itemView.findViewById(R.id.fishCountTextView);
            timestampTextView = itemView.findViewById(R.id.dateAndTimeTextView);
            tankNameTextView = itemView.findViewById(R.id.tankNameTextView);
            grid = itemView.findViewById(R.id.cards);
            extraView = itemView.findViewById(R.id.extra_view);
        }

        public void setFishCount(String fishCount) {
            fishCountTextView.setText(fishCount);
        }

        public void setTimestamp(String timestamp) {
            timestampTextView.setText(timestamp);
        }

        public void setTankName(String tankName) {
            tankNameTextView.setText(tankName);
        }

        public void hideExtraView() {
            ViewGroup.LayoutParams extraViewParams = grid.getLayoutParams();
            extraViewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            extraView.setVisibility(View.VISIBLE); // To hide extraView
        }

        public void showCards() {
            // Adjust the width of the CardView to 130dp
            ViewGroup.LayoutParams gridParams = grid.getLayoutParams();
            int widthInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, itemView.getResources().getDisplayMetrics());
            gridParams.width = widthInDp;
            grid.setLayoutParams(gridParams);
            // Hide extraView
            extraView.setVisibility(View.GONE);
            // Make the CardView center horizontally within its parent RelativeLayout
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) grid.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            grid.setLayoutParams(layoutParams);
            grid.setVisibility(View.VISIBLE);
        }

    }
}