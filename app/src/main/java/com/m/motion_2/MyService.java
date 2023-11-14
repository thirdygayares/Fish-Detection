package com.m.motion_2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyService extends Service {
    private static final String CHANNEL_ID = "MyServiceChannel";
    private boolean isTankReset = false;
    private static final int NOTIFICATION_ID = 1;
    private static final int DIARY_NOTIFICATION_ID = 2; // Unique notification ID for diary-related notifications
   //for 7days
//    private static final long INITIAL_DELAY = 7 * 24 * 60 * 60 * 1000;

     //for 1 mins
    private static final long INITIAL_DELAY = 1  * 60 * 1000;
    private long remainingTimeMillis = INITIAL_DELAY;

    private Handler handler = new Handler();
    
    private BroadcastReceiver clearNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            clearNotification();
        }
    };

    private void clearNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.cancel(DIARY_NOTIFICATION_ID);
        manager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        registerReceiver(clearNotificationReceiver, new IntentFilter("CLEAR_NOTIFICATION_ACTION"));
        scheduleTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check if the service is being restarted
        if (intent == null) {
            // Handle restart logic if needed
            Log.d("MyService", "Service restarted. Resetting state or performing cleanup.");
        }

        // Service is started for the first time or explicitly started
        return START_STICKY;  // Make the service sticky
    }
    private void scheduleTask() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("MyService", "Executing task to remove old fish entries.");

                    // Check if the tank has been reset
                    if (!isTankReset) {
                        removeOldDiaryEntriesForCurrentUser();
                    } else {
                        // Stop the task if the tank has been reset
                        Log.d("MyService", "Tank already reset. Stopping the task.");
                        handler.removeCallbacksAndMessages(null);
                    }

                    // Schedule the task to run again after 15 minutes
                    handler.postDelayed(this, INITIAL_DELAY);
                } catch (Exception e) {
                    Log.e("MyService", "Error in scheduled task: " + e.getMessage());
                }
            }
        }, INITIAL_DELAY); // Initial delay of 7 days
    }


    private void removeOldDiaryEntriesForCurrentUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MyService", "Removing old diary entries from the 'tank' database reference.");

                // Get the reference to the 'tank' database location
                DatabaseReference diaryRef = FirebaseDatabase.getInstance().getReference("tank");

                // Get the current time in milliseconds
                long currentTimeMillis = System.currentTimeMillis();

                // Calculate one minute ago
                long oneMinuteAgoMillis = currentTimeMillis - (60 * 1000);

                // Construct a date string for one minute ago
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                String oneMinuteAgoDateString = dateFormat.format(new Date(oneMinuteAgoMillis));

                // Query and remove entries older than one minute
                Query query = diaryRef.orderByChild("timestamp").endAt(oneMinuteAgoDateString);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                childSnapshot.getRef().removeValue();
                            }

                            // Set the tank reset flag to true
                            isTankReset = true;

                            // Show the diary removed notification
                            showDiaryRemovedNotification();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("MyService", "Error: " + databaseError.getMessage());
                    }
                });
            }
        }).start();
    }

    private void showDiaryRemovedNotification() {
        Intent loginIntent = new Intent(this, login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent loginPendingIntent = PendingIntent.getActivity(this, 0, loginIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FindQuant notification")
                .setContentText("Your fish count in Aquarium has been reset.")
                .setSmallIcon(R.drawable.logofin)
                .setContentIntent(loginPendingIntent) // Set the PendingIntent to be triggered on notification click
                .setAutoCancel(true); // Automatically cancel the notification when clicked
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(DIARY_NOTIFICATION_ID, builder.build());
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "My Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FindQuant")
                .setContentText("Running in the background")
                .setSmallIcon(R.drawable.logofin);

        // Make the notification persistent
        builder.setOngoing(true);

        // Create a handler to update the notification at regular intervals
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update the content text with the remaining time
                builder.setContentText("Reset in: " + formatRemainingTime(remainingTimeMillis));

                // Notify the notification manager to update the notification
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.notify(NOTIFICATION_ID, builder.build());

                // Update the remaining time and schedule the task to run again after 1 second
                remainingTimeMillis -= 1000;
                handler.postDelayed(this, 1000);
            }
        }, 1000);

        return builder.build();
    }


    private String formatRemainingTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format(Locale.getDefault(), "%d days, %02d:%02d:%02d", days, hours % 24, minutes % 60, seconds % 60);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(clearNotificationReceiver);
    }
}

