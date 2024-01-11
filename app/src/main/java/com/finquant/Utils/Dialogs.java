package com.finquant.Utils;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.m.motion_2.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.ViewHolder;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Dialogs {

    public void showDialog(Activity activity, String tankName, int fishCount) {
        Holder holder = new ViewHolder(R.layout.dialog_layouts23);
        DialogPlus dialog = DialogPlus.newDialog(activity)
                .setContentHolder(holder)
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setExpanded(false)
                .create();

        View dialogView = dialog.getHolderView();// Get the dialog view to access its components
        AppCompatButton yesButton = dialogView.findViewById(R.id.open);
        AppCompatButton noButton = dialogView.findViewById(R.id.cancel);
        TextView countText = dialogView.findViewById(R.id.fishCountTextView);
        TextView tankNAME = dialogView.findViewById(R.id.TankNAME);
        tankNAME.setText("tank name: "+tankName);
        countText.setText(String.valueOf("fish count is: "+fishCount));


        yesButton.setOnClickListener(v -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            String currentDateTime = dateFormat.format(new Date());
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userTanksRef = FirebaseDatabase.getInstance().getReference().child("tank").child(userId);

                userTanksRef.orderByChild("tankName").equalTo(tankName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DatabaseReference existingFishCountRef = snapshot.getRef();
                                existingFishCountRef.child("fishCount").setValue(fishCount);
                                existingFishCountRef.child("timeStamp").setValue(currentDateTime);
                                Toast.makeText(activity, "Data updated successfully!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            DatabaseReference newFishCountRef = userTanksRef.push();
                            newFishCountRef.child("tankName").setValue(tankName);
                            newFishCountRef.child("fishCount").setValue(fishCount);
                            newFishCountRef.child("timeStamp").setValue(currentDateTime);
                            Toast.makeText(activity, "New data saved successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(activity, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss(); // Dismiss the dialog after the action is performed
        });

        noButton.setOnClickListener(v -> {
            // Do nothing or provide feedback to the user about not saving the data
            Toast.makeText(activity, "Data not saved", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Dismiss the dialog after the action is performed
        });

        dialog.show();
    }

}
