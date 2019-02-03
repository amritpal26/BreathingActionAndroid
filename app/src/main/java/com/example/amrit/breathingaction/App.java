package com.example.amrit.breathingaction;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class App extends Application {

    public static final String CHANNEL_ID = "Reminders for breathing action";
    private static final String CHANNEL_NAME = "Reminders";
//    private static final int REQUEST_CODE_REMINDER_1 = 100;
//    private final int REQUEST_CODE_REMINDER_2 = 200;
//

//    private final String PREF_USER_NAME = "pref_user_name";
//
//    SharedPreferences preferences;
//    boolean isReminderOneEnabled;
//    boolean isReminderTwoEnabled;
//
//    long reminderOneMillis;
//    long reminderTwoMillis;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription("This channel is for setting reminders for exercise.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }






























//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//
//        isReminderOneEnabled = preferences.getBoolean(PREF_IS_REMINDER_1_KEY, false);
//        isReminderTwoEnabled = preferences.getBoolean(PREF_IS_REMINDER_2_KEY, false);
//
//        reminderOneMillis = preferences.getLong(PREF_REMINDER_1_KEY, 0);
//        reminderTwoMillis = preferences.getLong(PREF_REMINDER_2_KEY, 0);
//
//        Log.i("TIMES", "cur: " + System.currentTimeMillis() + ", one: " + reminderOneMillis + ", two: " + reminderTwoMillis);
//
//        setupReminders();
//    }

//    private void setupReminders(){
//        Log.i("CHANGE", "yes");
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        Intent intent = new Intent(getApplicationContext(), ReminderReceiver.class);
//
//        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(getApplicationContext(),
//                REQUEST_CODE_REMINDER_1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(),
//                REQUEST_CODE_REMINDER_2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        if(isReminderOneEnabled) {
////            alarmManager.cancel(pendingIntent1);
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, AlarmManager.INTERVAL_DAY, reminderOneMillis, pendingIntent1);
//        }
//        else{
////            alarmManager.cancel(pendingIntent1);
//        }
//
//        if(isReminderTwoEnabled) {
////            alarmManager.cancel(pendingIntent2);
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, AlarmManager.INTERVAL_DAY, reminderTwoMillis, pendingIntent2);
//        }
//        else{
////            alarmManager.cancel(pendingIntent2);
//        }
//    }

//    private void createNotificationChannels() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            CharSequence name = getString(R.string.notification_channel_1_name);
//            String description = getString(R.string.notification_channel_1_description);
//
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel1 = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel1.setDescription(description);
//
//            // Register the channels with the system
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel1);
//        }
//    }
}
