package com.example.cse2200_project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class ForegroundService extends Service {
    String NOTIFICATION_CHANNEL_ID = "file_server";
    CharSequence NOTIFICATION_CHANNEL_NAME = "File Server";
    String NOTIFICATION_CHANNEL_DESCRIPTION = "Notification Channel for the File Server App.";


    Notification.Builder notification_builder;


    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();


        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,0);
        if(Build.VERSION.SDK_INT >= 26) {
            notification_builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);

        }else{
            notification_builder = new Notification.Builder(this);
        }
        notification_builder.setSmallIcon(R.drawable.favicon)
                .setContentTitle("File Server Running")
                .setContentIntent(pendingIntent)
                .setTicker("ticker_text");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification = notification_builder.setContentText(MainActivity.SERVER_URL).build();
        startForeground(6071, notification);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
