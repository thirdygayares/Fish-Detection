package com.finquant.Adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.finquant.Class.FishCountModel;
import com.m.motion_2.R;

import java.util.List;

public class FishCountAdapter2 extends RecyclerView.Adapter<FishCountAdapter2.ViewHolder> {

    private Context context;
    private List<FishCountModel> fishCountList;

    public FishCountAdapter2(Context context, List<FishCountModel> fishCountList) {
        this.context = context;
        this.fishCountList = fishCountList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_list_item_layout, parent, false);
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
        builder.setItems(new CharSequence[]{"Rename", "Delete"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showRenameProgressDialog(position);
                        break;
                    case 1:
                        showDelete(position);
                        break;
                }
            }
        });

        builder.create().show();
    }

//    private void showArchiveProgressDialog(final int position) {
//        FishCountModel fishCountModel = fishCountList.get(position);
//        String TankName = fishCountModel.getTankName();
//        final ProgressDialog progressDialog = new ProgressDialog(context);
//        progressDialog.setMessage("Archiving Tank...");
//        progressDialog.show();
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Archive Tank");
//        builder.setMessage("Are you sure you want to Archive Tank name: "+ TankName);
//
//        builder.setPositiveButton("Archive", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                archiveTank(position,progressDialog);
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                progressDialog.dismiss();
//                dialog.dismiss();
//            }
//        });
//
//        builder.create().show();
//    }
//
//
//    private void archiveTank(int position, ProgressDialog progressDialog) {
//        progressDialog.show();
//        FishCountModel fishCountModel = fishCountList.get(position);
//        String tankNameToArchive = fishCountModel.getTankName();
//        String timeStampToArchive = fishCountModel.getTimestamp();
//        int fishCountToArchive = fishCountModel.getFishCount();
//        FirebaseDatabase sourceDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference sourceTankRef = sourceDatabase.getReference("tank");
//        sourceTankRef.orderByChild("tankName").equalTo(tankNameToArchive)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot tankSnapshot : dataSnapshot.getChildren()) {
//                            tankSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    FirebaseDatabase targetDatabase = FirebaseDatabase.getInstance();
//                                    DatabaseReference targetTankRef = targetDatabase.getReference("archived_tank");
//                                    DatabaseReference archivedTankRef = targetTankRef.push();
//                                    archivedTankRef.child("fishCount").setValue(fishCountToArchive);
//                                    archivedTankRef.child("tankName").setValue(tankNameToArchive);
//                                    archivedTankRef.child("timeStamp").setValue(timeStampToArchive);
//                                    notifyDataSetChanged();
//                                    Toast.makeText(context,"Tank Archive Success",Toast.LENGTH_SHORT).show();
//                                    progressDialog.dismiss();
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(context,"Failed to archive Tank",Toast.LENGTH_SHORT).show();
//                                    progressDialog.dismiss();
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        progressDialog.dismiss();
//                    }
//                });
//    }



    private void showDelete(final int position) {
        FishCountModel fishCountModel = fishCountList.get(position);
        String TankName = fishCountModel.getTankName();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Tank?");
        builder.setMessage("Are you sure you want to Archive Tank name: "+ TankName);

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



    private void showRenameProgressDialog(final int position) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Renaming Tank...");
        progressDialog.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rename Tank");

        final EditText newTankNameInput = new EditText(context);
        newTankNameInput.setHint("Enter New Tank Name");
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
                        DatabaseReference tankRef = database.getReference("archived_tank").child(userId);

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
                                            // You may also want to update the UI or the data source to reflect the new tank name.
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
            DatabaseReference tankRef = database.getReference("archived_tank").child(userId);

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

    private void deleteItem(int position) {
        // Get the tank name of the tank you want to delete
        FishCountModel fishCountModel = fishCountList.get(position);
        String tankNameToDelete = fishCountModel.getTankName();

        // Create a Firebase reference to the "tank" node
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tankRef = database.getReference("tank");

        // Query for the tank to delete based on tankName
        tankRef.orderByChild("tankName").equalTo(tankNameToDelete).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tankSnapshot : dataSnapshot.getChildren()) {
                    // Remove the matching tank node
                    tankSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully deleted the tank by tankName
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle the error if the deletion fails
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


    private void renameTankName(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rename Tank");

        final EditText newTankNameInput = new EditText(context);
        newTankNameInput.setHint("Enter New Tank Name");
        builder.setView(newTankNameInput);

        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTankName = newTankNameInput.getText().toString().trim();
                if (!newTankName.isEmpty()) {
                    // Get the old tank name
                    FishCountModel fishCountModel = fishCountList.get(position);
                    String oldTankName = fishCountModel.getTankName();

                    // Create a Firebase reference to the "tank" node
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference tankRef = database.getReference("tank");

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
                                        // You may also want to update the UI or the data source to reflect the new tank name.
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle the error if the renaming fails
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle any errors or onCancelled events
                        }
                    });
                } else {
                    Toast.makeText(context, "New tank name is required", Toast.LENGTH_SHORT).show();
                }
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


    @Override
    public int getItemCount() {
        return fishCountList.size();
    }

    public void filterList(List<FishCountModel> filteredList) {
        fishCountList = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView fishCountTextView;
        private TextView timestampTextView;
        private TextView tankNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fishCountTextView = itemView.findViewById(R.id.fishCountTextView);
            timestampTextView = itemView.findViewById(R.id.dateAndTimeTextView);
            tankNameTextView = itemView.findViewById(R.id.tankNameTextView);
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
    }
}
