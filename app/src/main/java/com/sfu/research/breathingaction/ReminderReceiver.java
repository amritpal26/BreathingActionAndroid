package com.sfu.research.breathingaction;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Date;

public class ReminderReceiver extends BroadcastReceiver {

    public static String NOTIFICATION_ID_EXTRA = "notification-id";

    public void onReceive(Context context, Intent intent){

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = intent.getIntExtra(NOTIFICATION_ID_EXTRA, 0);
        Notification notification = createNotification(context, notificationId);
        notificationManager.notify(notificationId, notification);

        // Repeat the notification for the next day.
       PendingIntent pendingIntentAlarm = createPendingIntentForAlarm(context, notificationId);
       long triggerAtMillis = getNotificationTriggerTime(context, notificationId);

       if (Build.VERSION.SDK_INT >= 23) {
           alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
       } else {
           alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
       }
    }

    private Notification createNotification(Context context, int notificationId){
        Intent intent = new Intent(context, BreathingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.CHANNEL_ID)
                .setContentTitle("Reminder")
                .setContentText("Time to do the relaxing exercise")
                .setSmallIcon(com.sfu.research.breathingaction.R.drawable.ic_notifications_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        return builder.build();
    }

    private PendingIntent createPendingIntentForAlarm(Context context, int notification_id){

        Intent intentForAlarm = new Intent(context, ReminderReceiver.class);
        intentForAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intentForAlarm.putExtra(ReminderReceiver.NOTIFICATION_ID_EXTRA, notification_id);

        int reminderRequestCode = SettingsActivity.REMINDER_1_REQUEST_CODE;
        if (notification_id == SettingsActivity.REMINDER_2_NOTIFICATION_ID){
            reminderRequestCode = SettingsActivity.REMINDER_2_REQUEST_CODE;
        }

        PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(context, reminderRequestCode, intentForAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntentAlarm;
    }

    private long getNotificationTriggerTime(Context context, int notificationId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Long triggerAtMillis = 0L;
        if (notificationId == SettingsActivity.REMINDER_1_NOTIFICATION_ID){
            triggerAtMillis = prefs.getLong(SettingsActivity.PREF_REMINDER_1_KEY, 0);
        }else{
            triggerAtMillis = prefs.getLong(SettingsActivity.PREF_REMINDER_2_KEY, 0);
        }

        if (triggerAtMillis <= System.currentTimeMillis()) {
            triggerAtMillis = getTriggerTimeFuture(context, triggerAtMillis);
        }

        return triggerAtMillis;
    }

    private Long getTriggerTimeFuture(Context context, Long time){
        long dayInMillis = AlarmManager.INTERVAL_DAY;

        long current = System.currentTimeMillis();
        long daysBehind = ((current - time) / dayInMillis) + 1;

        Log.i("TIME ALARM", DateFormat.getDateFormat(context).format(new Date(time)));
        Log.i("TIME CURR", DateFormat.getDateFormat(context).format(new Date(current)));
        Log.i("TIME NEXT", DateFormat.getDateFormat(context).format(new Date(time + (daysBehind * dayInMillis))));

        return time + (daysBehind * dayInMillis);

    }
}
