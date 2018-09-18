package com.example.amrit.breathingaction;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {

    private final int REQUEST_CODE_REMINDER_1 = 100;
    private final int REQUEST_CODE_REMINDER_2 = 200;
    private final String CHANNEL_ID = "Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast", "recieved");

        Intent notification_intent = new Intent(context, BreathingActivity.class);
        notification_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE_REMINDER_1, notification_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Reminder")
                .setContentText("Remember to do the breathing exercise")
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.alert_light_frame)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(REQUEST_CODE_REMINDER_1, builder.build());
    }
}
