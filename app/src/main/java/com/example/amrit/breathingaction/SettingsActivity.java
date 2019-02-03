package com.example.amrit.breathingaction;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Random;


public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String PREF_REMINDER_1_KEY = "timePref_reminder_1";
    private static final String PREF_REMINDER_2_KEY = "timePref_reminder_2";
    private static final String PREF_IS_REMINDER_1_KEY = "reminder_1";
    private static final String PREF_IS_REMINDER_2_KEY = "reminder_2";
    public static final int REMINDER_1_NOTIFICATION_ID = 101;
    public static final int REMINDER_2_NOTIFICATION_ID = 102;
    private static final int REMINDER_1_REQUEST_CODE = 201;
    private static final int REMINDER_2_REQUEST_CODE = 202;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference.getKey().equals("pref_user_name")){
                if(value.equals("user name")){
                    preference.setEnabled(true);
                } else{
                    preference.setEnabled(false);
                    preference.setSummary(stringValue);
                }
            }else{
                preference.setSummary(stringValue);
                Log.i("PREFF", "else");
            }
            return true;
        }
    };

    private static Preference.OnPreferenceChangeListener preferenceChangeListenerReminder = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            Intent intentForAlarm = new Intent(preference.getContext(), ReminderReceiver.class);
            intentForAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            AlarmManager alarmManager = (AlarmManager) preference.getContext().getSystemService(Context.ALARM_SERVICE);

            // time on reminder can only be changed when the reminder is active. So its safe.
            if(preference.getKey().equals(PREF_IS_REMINDER_1_KEY)){
                intentForAlarm.putExtra(ReminderReceiver.NOTIFICATION_ID_EXTRA, REMINDER_1_NOTIFICATION_ID);
                PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(preference.getContext(), REMINDER_1_REQUEST_CODE, intentForAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                if(value.equals(true)){
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());

                    Long triggerAtMillis = prefs.getLong(PREF_REMINDER_1_KEY, 0);
                    if (triggerAtMillis <= System.currentTimeMillis()){
                        triggerAtMillis = getTriggerTimeFuture(triggerAtMillis);
                    }

                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntentAlarm);
                }else{
                    alarmManager.cancel(pendingIntentAlarm);
                }
            }else if(preference.getKey().equals(PREF_IS_REMINDER_2_KEY)){
                intentForAlarm.putExtra(ReminderReceiver.NOTIFICATION_ID_EXTRA, REMINDER_2_NOTIFICATION_ID);
                PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(preference.getContext(), REMINDER_2_REQUEST_CODE, intentForAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                if(value.equals(true)){
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());

                    Long triggerAtMillis = prefs.getLong(PREF_REMINDER_2_KEY, 0);
                    if (triggerAtMillis <= System.currentTimeMillis()){
                        triggerAtMillis = getTriggerTimeFuture(triggerAtMillis);
                    }

                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntentAlarm);
                }else{
                    alarmManager.cancel(pendingIntentAlarm);
                }
            }else if (preference.getKey().equals(PREF_REMINDER_1_KEY)){
                intentForAlarm.putExtra(ReminderReceiver.NOTIFICATION_ID_EXTRA, REMINDER_1_NOTIFICATION_ID);
                PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(preference.getContext(), REMINDER_1_REQUEST_CODE, intentForAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntentAlarm);

                Long triggerAtMillis = (Long) value;
                if (triggerAtMillis <= System.currentTimeMillis()){
                    triggerAtMillis = getTriggerTimeFuture(triggerAtMillis);
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntentAlarm);
            }else if(preference.getKey().equals(PREF_REMINDER_2_KEY) ){
                intentForAlarm.putExtra(ReminderReceiver.NOTIFICATION_ID_EXTRA, REMINDER_2_NOTIFICATION_ID);
                PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(preference.getContext(), REMINDER_2_REQUEST_CODE, intentForAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntentAlarm);

                Long triggerAtMillis = (Long) value;
                if (triggerAtMillis <= System.currentTimeMillis()){
                    triggerAtMillis = getTriggerTimeFuture(triggerAtMillis);
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntentAlarm);
            }
            return true;
        }

        private Long getTriggerTimeFuture(Long time){
            long dayInMillis = AlarmManager.INTERVAL_DAY;

            long current = System.currentTimeMillis();
            long daysBehind = ((current - time) / dayInMillis) + 1;

            return time + (daysBehind * dayInMillis);
        }
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }


    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("pref_user_name"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);
            findPreference(PREF_REMINDER_1_KEY).setOnPreferenceChangeListener(preferenceChangeListenerReminder);
            findPreference(PREF_REMINDER_2_KEY).setOnPreferenceChangeListener(preferenceChangeListenerReminder);
            findPreference(PREF_IS_REMINDER_1_KEY).setOnPreferenceChangeListener(preferenceChangeListenerReminder);
            findPreference(PREF_IS_REMINDER_2_KEY).setOnPreferenceChangeListener(preferenceChangeListenerReminder);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    private Notification createNotification(){
        Intent intent = new Intent(this, BreathingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Reminder")
                .setContentText("Remember to perform the breathing exercise.")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        return builder.build();
    }

    private PendingIntent getPendingIntent(int notificationId, int requestCode){

        Notification notification = createNotification();

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.NOTIFICATION_EXTRA, notification);
        intent.putExtra(ReminderReceiver.NOTIFICATION_ID_EXTRA, notificationId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    private void scheduleNotification(PendingIntent pendingIntent, long triggerAtMillis){

        // TODO: find the triggerAtMillis value.
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void cancelNotification(PendingIntent pendingIntent){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
