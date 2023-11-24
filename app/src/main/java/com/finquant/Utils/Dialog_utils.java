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
import android.widget.Button;
import android.widget.Toast;

import com.finquant.Activity.CountAct;
import com.finquant.Activity.login;
import com.google.firebase.auth.FirebaseAuth;
import com.m.motion_2.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

public class Dialog_utils {
    private static String NOTIFICATION_PREF_KEY = "notification_permission";
    private static final String CONFIRMED ="CONFIRMED";



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