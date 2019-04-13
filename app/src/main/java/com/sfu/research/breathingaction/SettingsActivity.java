package com.sfu.research.breathingaction;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final String PREF_REMINDER_1_KEY = "timePref_reminder_1";
    public static final String PREF_REMINDER_2_KEY = "timePref_reminder_2";
    private static final String PREF_IS_REMINDER_1_KEY = "reminder_1";
    private static final String PREF_IS_REMINDER_2_KEY = "reminder_2";
    public static final int REMINDER_1_NOTIFICATION_ID = 101;
    public static final int REMINDER_2_NOTIFICATION_ID = 102;
    public static final int REMINDER_1_REQUEST_CODE = 201;
    public static final int REMINDER_2_REQUEST_CODE = 202;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference.getKey().equals("pref_user_name")){
                if(value.equals("default")){
                    preference.setEnabled(true);
                    preference.setSummary(stringValue);
                } else{
                    preference.setEnabled(false);
                    preference.setSummary(stringValue);
                }
            }else{
                preference.setSummary(stringValue);
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
                    if (triggerAtMillis <= System.currentTimeMillis()) {
                        triggerAtMillis = getTriggerTimeFuture(triggerAtMillis);
                    }

                    if (Build.VERSION.SDK_INT >= 23) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
                    }else{
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
                    }
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

                    if (Build.VERSION.SDK_INT >= 23) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
                    }else{
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
                    }
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

                if (Build.VERSION.SDK_INT >= 23) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
                }else{
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
                }
            }else if(preference.getKey().equals(PREF_REMINDER_2_KEY) ){
                intentForAlarm.putExtra(ReminderReceiver.NOTIFICATION_ID_EXTRA, REMINDER_2_NOTIFICATION_ID);
                PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(preference.getContext(), REMINDER_2_REQUEST_CODE, intentForAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntentAlarm);

                Long triggerAtMillis = (Long) value;
                if (triggerAtMillis <= System.currentTimeMillis()){
                    triggerAtMillis = getTriggerTimeFuture(triggerAtMillis);
                }

                if (Build.VERSION.SDK_INT >= 23) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
                }else{
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentAlarm);
                }
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
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }


    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(com.sfu.research.breathingaction.R.xml.pref_headers, target);
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
            addPreferencesFromResource(com.sfu.research.breathingaction.R.xml.pref_general);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("pref_user_name"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(com.sfu.research.breathingaction.R.xml.pref_notification);
            setHasOptionsMenu(true);

            findPreference(PREF_REMINDER_1_KEY).setOnPreferenceChangeListener(preferenceChangeListenerReminder);
            findPreference(PREF_REMINDER_2_KEY).setOnPreferenceChangeListener(preferenceChangeListenerReminder);
            findPreference(PREF_IS_REMINDER_1_KEY).setOnPreferenceChangeListener(preferenceChangeListenerReminder);
            findPreference(PREF_IS_REMINDER_2_KEY).setOnPreferenceChangeListener(preferenceChangeListenerReminder);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return true;
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }
}
