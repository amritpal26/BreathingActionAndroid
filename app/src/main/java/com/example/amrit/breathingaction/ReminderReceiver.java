package com.example.amrit.breathingaction;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {

    public static String NOTIFICATION_ID_EXTRA = "notification-id";
    public static String NOTIFICATION_EXTRA = "notification";


    public void onReceive(Context context, Intent intent){

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = intent.getIntExtra(NOTIFICATION_ID_EXTRA, 0);
        Notification notification = createNotification(context, notificationId);
        notificationManager.notify(notificationId, notification);
    }

    private Notification createNotification(Context context, int notificationId){
        Intent intent = new Intent(context, BreathingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.CHANNEL_ID)
                .setContentTitle("Reminder")
                .setContentText("Remember to perform the breathing exercise.")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        return builder.build();
    }
}
