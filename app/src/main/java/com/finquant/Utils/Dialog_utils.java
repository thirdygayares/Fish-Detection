package com.finquant.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.finquant.Activity.CountAct;
import com.finquant.Activity.login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.m.motion_2.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Dialog_utils {
    private static String NOTIFICATION_PREF_KEY = "notification_permission";
    private static final String CONFIRMED ="CONFIRMED";

    //fishCountDialog
    public static void showFishCountDialog(final int fishCount, Activity activity) {
        View contentView = activity.getLayoutInflater().inflate(R.layout.custom_fish_count_dialog, null);
        TextView fishCountTextView = contentView.findViewById(R.id.fishCountTextView);
        EditText tankNameInput = contentView.findViewById(R.id.tankNameInput);
        fishCountTextView.setText(String.valueOf(fishCount));
        CountAct countAct = new CountAct();
        DialogPlus dialog = DialogPlus.newDialog(activity)
                .setContentHolder(new ViewHolder(contentView))
                .setHeader(R.layout.dialog_header)
                .setFooter(R.layout.dialog_footer)
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .setExpanded(false)
                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                        if (view.getId() == R.id.saveButton && currentUser != null) {
                            String tankName = tankNameInput.getText().toString().trim();
                            if (!tankName.isEmpty()) {
                                // Get the current date and time
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                                String currentDateTime = dateFormat.format(new Date());

                                // Create a Firebase reference to the "user_tanks" node under the current user's ID
                                String userId = currentUser.getUid();
                                DatabaseReference userTanksRef = FirebaseDatabase.getInstance().getReference().child("tank").child(userId);

                                // Set the fish count, tank name, and timestamp as child values in a single node
                                DatabaseReference newFishCountRef = userTanksRef.push();
                                newFishCountRef.child("tankName").setValue(tankName);
                                newFishCountRef.child("fishCount").setValue(fishCount);
                                newFishCountRef.child("timeStamp").setValue(currentDateTime);

                                // Show a success toast message
                                Toast.makeText(activity.getApplicationContext(), "Fish count saved successfully in tank name: " + tankName, Toast.LENGTH_SHORT).show();

                                dialog.dismiss(); // Close the dialog after saving
                            } else {
                                Toast.makeText(activity.getApplicationContext(), "Tank name is required", Toast.LENGTH_SHORT).show();
                            }
                        } else if (view.getId() == R.id.tryAgainButton) {
                            Intent i = new Intent(activity.getApplicationContext(), CountAct.class);
                            activity.startActivity(i);
                            activity.overridePendingTransition(0, 0);
                            countAct.finishActivity();
                            countAct.reinitializeCamera();
                            countAct.handleCameraViewStopped();
                        } else if (view.getId() == R.id.cancelButton) {
                            countAct.reinitializeCamera();
                            dialog.dismiss(); // Close the dialog if the "Cancel" button is clicked
                        }
                    }
                })
                .create();

        dialog.show();
    }

    //logout_dialog
    public void logout(Activity activity){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.logout_dialog, null);
        DialogPlus dialogPlus = DialogPlus.newDialog(activity)
                .setContentHolder(new ViewHolder(contentView))
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .setExpanded(false)
                .create();
        Button Confirm_button = contentView.findViewById(R.id.open);
        Button Cancel_button = contentView.findViewById(R.id.cancel);
        Confirm_button.setOnClickListener(v -> {
            Intent i = new Intent(activity.getApplicationContext(), login.class);
            FirebaseAuth.getInstance().signOut();
            activity.startActivity(i);
            activity.overridePendingTransition(0,0);
            activity.finish();
        });

        Cancel_button.setOnClickListener(v -> {
            dialogPlus.dismiss();
        });
        dialogPlus.show();

    }

    //warning
    public void countFish(Activity activity){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_confirm, null);
        DialogPlus dialogPlus = DialogPlus.newDialog(activity)
                .setContentHolder(new ViewHolder(contentView))
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .setExpanded(false)
                .create();
        Button Confirm_button = contentView.findViewById(R.id.open);
        Button Cancel_button = contentView.findViewById(R.id.cancel);
        Confirm_button.setOnClickListener(v -> {
            Intent i = new Intent(activity.getApplicationContext(), CountAct.class);
            activity.startActivity(i);
            activity.overridePendingTransition(0,0);
            activity.finish();
        });

        Cancel_button.setOnClickListener(v -> {
            dialogPlus.dismiss();
        });
        dialogPlus.show();

    }
    public void warning(Activity activity){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.warning_dialog, null);
        DialogPlus dialogPlus = DialogPlus.newDialog(activity)
                .setContentHolder(new ViewHolder(contentView))
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .setExpanded(false)
                .create();
        Button Confirm_button = contentView.findViewById(R.id.ok);
        Confirm_button.setOnClickListener(v -> {
            dialogPlus.dismiss();
        });
        dialogPlus.show();
    }

    //forgeground
    public void foreground(Activity activity){
        boolean notificationConfirmed = PreferenceUtils.getBoolean(activity, CONFIRMED, false);
        if (!notificationConfirmed) {
            View contentView = LayoutInflater.from(activity).inflate(R.layout.your_dialog_layout, null);
            DialogPlus dialogPlus = DialogPlus.newDialog(activity)
                    .setContentHolder(new ViewHolder(contentView))
                    .setGravity(Gravity.CENTER)
                    .setCancelable(false)
                    .setExpanded(false)
                    .create();

            Button Confirm_button = contentView.findViewById(R.id.open);
            Button cancelBtn = contentView.findViewById(R.id.cancel);

            Confirm_button.setOnClickListener(v -> {
                openAppInfoSettings(activity);
                PreferenceUtils.putBoolean(activity, CONFIRMED, true);
                dialogPlus.dismiss();
            });

            cancelBtn.setOnClickListener(v -> {
                dialogPlus.dismiss();
            });

            dialogPlus.show();
        } else {
        }
    }
    private void openAppInfoSettings(Activity activity) {
        Uri packageUri = Uri.parse("package:" + activity.getApplicationContext().getPackageName());
        Intent appInfoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
        appInfoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.getApplicationContext().startActivity(appInfoIntent);
    }


    //notification
    public void notification(Activity activity, Context context) {
        boolean notificationConfirmed = PreferenceUtils.getBoolean(context, NOTIFICATION_PREF_KEY, false);
        if (!notificationConfirmed) {
            View contentView = LayoutInflater.from(activity).inflate(R.layout.notification, null);
            DialogPlus dialogPlus = DialogPlus.newDialog(activity)
                    .setContentHolder(new ViewHolder(contentView))
                    .setGravity(Gravity.CENTER)
                    .setCancelable(false)
                    .setExpanded(false)
                    .create();

            Button confirmButton = contentView.findViewById(R.id.open);
            Button cancelBtn = contentView.findViewById(R.id.cancel);

            confirmButton.setOnClickListener(v -> {
                checkNotificationPermission(activity);
                PreferenceUtils.putBoolean(context, NOTIFICATION_PREF_KEY, true);
                dialogPlus.dismiss();
            });

            cancelBtn.setOnClickListener(v -> {
                dialogPlus.dismiss();
            });

            dialogPlus.show();
        } else {
            Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show();
        }
    }
    public void checkNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName()); // Use activity.getPackageName() instead of getPackageName()
            activity.startActivity(intent); // Use activity.startActivity(intent) instead of just (intent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", activity.getPackageName(), null)); // Use activity.getPackageName() instead of getPackageName()
            activity.startActivity(intent); // Use activity.startActivity(intent) instead of just (intent)
        }
    }
}