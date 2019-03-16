package com.sfu.research.breathingaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.SimpleDateFormat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nex3z.expandablecircleview.ExpandableCircleView;

import java.util.ArrayList;
import java.util.Date;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class BreathingActivity extends AppCompatActivity {

    private final int PROGRESS_INHALE_COLOR = Color.rgb(0, 191, 225);
    private final int PROGRESS_EXHALE_COLOR = Color.rgb(225, 191, 0);
    private final int PROGRESS_HOLD_COLOR = Color.rgb(191, 255, 0);

    public static final String PREFERENCES_NAME = "my_prefs";
    public static final String TIMER_SELECTED_ITEM = "timer_Selected_item";
    public static final String INHALE_SELECTED_ITEM = "inhale_Selected_item";
    public static final String EXHALE_SELECTED_ITEM = "exhale_Selected_item";
    public static final String HOLD1_SELECTED_ITEM = "hold_1_Selected_item";
    public static final String HOLD2_SELECTED_ITEM = "hold_2_Selected_item";
    private final String PREFERENCE_KEY_SOUND_SWITCH = "pref_sound";
    private final String PREFERENCE_KEY_VIBRATION_SWITCH = "pref_vibration";
    private final String PREFERENCE_KEY_USER_NAME = "pref_user_name";

    FirebaseDatabase database;

    private static final int EXPAND_DURATION = 100;

    private enum BreathingState {
        NEW_TIMER, PAUSED, INHALE, EXHALE, HOLD
    }

    CountDownTimer timer;
    private long inhaleTimeMillis;
    private long exhaleTimeMillis;
    private long hold_1_TimeMillis;
    private long hold_2_TimeMillis;
    private long currentRunMillisElapsed;
    private long previousTimerTimeMillis = 0;
    private long timerTimeOnSpinnerMillis;
    private long timerTimeMillis;
    long currentCycleNumber;
    private BreathingState breathingState = BreathingState.NEW_TIMER;

    MediaPlayer beepSound;
    Vibrator vibrator;

    MaterialProgressBar timerProgressBar;
    ExpandableCircleView currentActionProgress;

    SharedPreferences defaultPrefs;
    SharedPreferences prefs;
    boolean soundEnabled;
    boolean vibrationEnabled;

    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.sfu.research.breathingaction.R.layout.activity_breathing);

        timerProgressBar = (MaterialProgressBar) findViewById(com.sfu.research.breathingaction.R.id.timerActivityProgressBar);
        currentActionProgress = (ExpandableCircleView) findViewById(com.sfu.research.breathingaction.R.id.currentActionProgressBar);
        currentActionProgress.setExpandAnimationDuration(EXPAND_DURATION);

        final TextView commandTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionTime);
        commandTextView.bringToFront();
        actionTimerTextView.bringToFront();

        prefs = this.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        beepSound = MediaPlayer.create(this, R.raw.beep);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        soundEnabled = defaultPrefs.getBoolean(PREFERENCE_KEY_SOUND_SWITCH, false);
        vibrationEnabled = defaultPrefs.getBoolean(PREFERENCE_KEY_VIBRATION_SWITCH, false);
        userName = defaultPrefs.getString(PREFERENCE_KEY_USER_NAME, "default");
        Long time = defaultPrefs.getLong("timePref_reminder_1", 0);
        Log.i("timePref_reminder_1", DateFormat.getTimeFormat(this).format(new Date(time)));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initiateDatabase();

        setupSpinner(com.sfu.research.breathingaction.R.id.inhaleSpinner);
        setupSpinner(com.sfu.research.breathingaction.R.id.exhaleSpinner);
        setupSpinner(com.sfu.research.breathingaction.R.id.holdSpinner);
        setupSpinner(com.sfu.research.breathingaction.R.id.holdSpinner2);
        setupSpinner(com.sfu.research.breathingaction.R.id.timerSpinner);
        setupStartClick();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        prefs = this.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private void setupStartClick() {
        final TextView commandTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionTime);

        commandTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (breathingState == BreathingState.NEW_TIMER)
                    startTimer(0);
                else if (breathingState != BreathingState.PAUSED && breathingState != BreathingState.NEW_TIMER) {
                    pauseTimer();
                } else if (breathingState == BreathingState.PAUSED) {
                    startTimer(previousTimerTimeMillis);
                }
            }
        });

        actionTimerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (breathingState == BreathingState.PAUSED && breathingState != BreathingState.NEW_TIMER) {
                    previousTimerTimeMillis = 0;
                    startTimer(0);
                } else if (breathingState != BreathingState.PAUSED || breathingState != BreathingState.NEW_TIMER) {
                    pauseTimer();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.sfu.research.breathingaction.R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.sfu.research.breathingaction.R.id.action_settings){
            Intent intent = SettingsActivity.makeIntent(BreathingActivity.this);
            startActivity(intent);
        }
        return true;
    }

    private void pauseTimer() {
        final TextView commandTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionTime);
        breathingState = BreathingState.PAUSED;
        actionTimerTextView.setText("Restart");
        commandTextView.setText("Continue");
        previousTimerTimeMillis += currentRunMillisElapsed;
        unlockSpinners();
        timer.cancel();

        actionTimerTextView.setBackground(getDrawable(R.drawable.timer_buttons_continue));
        commandTextView.setBackground(getDrawable(R.drawable.timer_buttons_restart));

        currentActionProgress.setAlpha(0.4f);
        timerProgressBar.setAlpha(0.4f);
    }

    private void startTimer(final long currentTimerMillis) {
        lockSpinners();
        currentActionProgress.setAlpha(1f);
        timerProgressBar.setAlpha(1f);
        final TextView actionCommandTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionTime);
        final TextView clockTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.clockTextView);
        actionTimerTextView.setVisibility(View.VISIBLE);
        actionTimerTextView.setBackground(getDrawable(R.drawable.timer_buttons_continue));
        actionCommandTextView.setBackgroundResource(0);
        actionTimerTextView.setBackgroundResource(0);


        inhaleTimeMillis = getTimeFromSpinnerMillis(com.sfu.research.breathingaction.R.id.inhaleSpinner);
        Log.i("MSG", inhaleTimeMillis + "");
        hold_1_TimeMillis = getTimeFromSpinnerMillis(com.sfu.research.breathingaction.R.id.holdSpinner);
        exhaleTimeMillis = getTimeFromSpinnerMillis(com.sfu.research.breathingaction.R.id.exhaleSpinner);
        hold_2_TimeMillis = getTimeFromSpinnerMillis(com.sfu.research.breathingaction.R.id.holdSpinner2);
        timerTimeOnSpinnerMillis = getTimeFromSpinnerMillis(com.sfu.research.breathingaction.R.id.timerSpinner);

        final long inhaleTimeRangeMillis = inhaleTimeMillis;
        final long hold_1_TimeRangeMillis = inhaleTimeMillis + hold_1_TimeMillis;
        final long exhaleTimeRangeMillis = hold_1_TimeRangeMillis + exhaleTimeMillis;
        final long hold_2_TimeRangeSec = exhaleTimeRangeMillis + hold_2_TimeMillis;

        final long cycleTimeMillis = inhaleTimeMillis + hold_1_TimeMillis + exhaleTimeMillis + hold_2_TimeMillis;
        final long numberOfCycles = (timerTimeOnSpinnerMillis/cycleTimeMillis) + 1;
        timerTimeMillis = numberOfCycles * cycleTimeMillis;

        timerProgressBar.setMax((int) timerTimeMillis);

        timer = new CountDownTimer((timerTimeMillis) - previousTimerTimeMillis, 10) {
            @Override
            public void onTick(long millisUntilFinished) {

                currentRunMillisElapsed = (timerTimeMillis) - millisUntilFinished - previousTimerTimeMillis;
                long millisElapsedTotal = currentRunMillisElapsed + previousTimerTimeMillis;

                currentCycleNumber = millisElapsedTotal / cycleTimeMillis;

                if ((millisElapsedTotal - (currentCycleNumber * cycleTimeMillis)) < inhaleTimeRangeMillis) {
                    if (breathingState != BreathingState.INHALE) {
                        playSoundAndVibrate(false);
                        breathingState = BreathingState.INHALE;
                        currentActionProgress.setInnerColor(PROGRESS_INHALE_COLOR);
                        Log.i("COLOR", "inhale");
                    }
                    long inhaleTimerRemainingMillis = (inhaleTimeMillis) - (millisElapsedTotal - (currentCycleNumber * cycleTimeMillis));

                    long inhaleCoveredTimerMillis = millisElapsedTotal - (currentCycleNumber * cycleTimeMillis);
                    float progress = ((inhaleCoveredTimerMillis * 100) / (2 * inhaleTimeMillis)) + 50;
                    currentActionProgress.setProgress((int) progress);

                    actionCommandTextView.setText("Inhale");
                    String inhaleTimerRemainingSec = millisToSecString(inhaleTimerRemainingMillis);
                    actionTimerTextView.setText(inhaleTimerRemainingSec);
                } else if ((millisElapsedTotal - (currentCycleNumber * cycleTimeMillis)) < hold_1_TimeRangeMillis) {
                    if (breathingState != BreathingState.HOLD) {
                        playSoundAndVibrate(false);
                        breathingState = BreathingState.HOLD;
                        currentActionProgress.setInnerColor(PROGRESS_HOLD_COLOR);
                        int progress = currentActionProgress.getProgress();
                        currentActionProgress.setProgress(progress);
                        actionCommandTextView.setText("Hold");
                        Log.i("COLOR", "Hold1");
                    }

                    long holdTimerRemainingMillis = hold_1_TimeMillis - (millisElapsedTotal - (currentCycleNumber * cycleTimeMillis) - inhaleTimeRangeMillis);
                    String holdTimerRemainingSec = millisToSecString(holdTimerRemainingMillis);
                    actionTimerTextView.setText(holdTimerRemainingSec);
                } else if ((millisElapsedTotal - (currentCycleNumber * cycleTimeMillis)) < exhaleTimeRangeMillis) {
                    if (breathingState != BreathingState.EXHALE) {
                        playSoundAndVibrate(false);
                        breathingState = BreathingState.EXHALE;
                        actionCommandTextView.setText("Exhale");
                        currentActionProgress.setInnerColor(PROGRESS_EXHALE_COLOR);
                        Log.i("COLOR", "exhale");
                    }

                    long exhaleTimerRemainingMillis = exhaleTimeMillis - (millisElapsedTotal - (currentCycleNumber * cycleTimeMillis) - hold_1_TimeRangeMillis);
                    float exhaleReverseTimerMillis = (currentCycleNumber * cycleTimeMillis) + (hold_1_TimeRangeMillis) - millisElapsedTotal;

                    float progress = ((exhaleReverseTimerMillis * 100) / (2 * exhaleTimeMillis)) + 100;
                    String exhaleRemainingSec = millisToSecString(exhaleTimerRemainingMillis);
                    actionTimerTextView.setText(exhaleRemainingSec);
                    currentActionProgress.setProgress((int) progress);
                } else if (((millisElapsedTotal) - (currentCycleNumber * cycleTimeMillis)) < hold_2_TimeRangeSec) {
                    if (breathingState != BreathingState.HOLD) {
                        playSoundAndVibrate(false);
                        breathingState = BreathingState.HOLD;
                        actionCommandTextView.setText("Hold");
                        currentActionProgress.setInnerColor(PROGRESS_HOLD_COLOR);
                        int progress = currentActionProgress.getProgress();
                        currentActionProgress.setProgress(progress);
                        Log.i("COLOR", "Hold");
                    }

                    long holdTimerRemainingMillis = hold_2_TimeMillis - (millisElapsedTotal - (currentCycleNumber * cycleTimeMillis) - exhaleTimeRangeMillis);
                    String holdTimerRemainingSec = millisToSecString(holdTimerRemainingMillis);
                    actionTimerTextView.setText(holdTimerRemainingSec);
                }

                timerProgressBar.setProgress((int) (millisElapsedTotal));
                clockTextView.setText(getTimeMinutesString((int) millisElapsedTotal / 1000));
            }

            @Override
            public void onFinish() {
                actionCommandTextView.setText("Start Again");
                actionTimerTextView.setVisibility(View.INVISIBLE);
                breathingState = BreathingState.NEW_TIMER;
                clockTextView.setText(getTimeMinutesString((int) timerTimeMillis / 1000));
                timerProgressBar.setProgress((int) timerTimeMillis);
                playSoundAndVibrate(true);
                unlockSpinners();
                sendTimeStampToDataBase();
            }
        }.start();
    }

    private String millisToSecString(long millis) {
        int sec = (int) millis / 1000;
        String str = sec + "";
        return str;
    }


    private void setupSpinner(int spinnerId) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        final TextView actionCommandTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) findViewById(com.sfu.research.breathingaction.R.id.breathingActionTime);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (breathingState != BreathingState.NEW_TIMER) {
                    actionCommandTextView.setText("Start");
                    actionTimerTextView.setVisibility(View.INVISIBLE);
                    previousTimerTimeMillis = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<String> stringSecondsList = new ArrayList<String>();
        if (spinnerId != com.sfu.research.breathingaction.R.id.timerSpinner) {
            if (spinnerId == com.sfu.research.breathingaction.R.id.inhaleSpinner) {
                int[] intSecondsList = getResources().getIntArray(R.array.secondsListInhale);
                for (int i = 0; i < intSecondsList.length; i++) {
                    stringSecondsList.add((float) intSecondsList[i] / 2 + " sec");
                }
                ArrayAdapter adapter = new ArrayAdapter(this, com.sfu.research.breathingaction.R.layout.drop_down_layout, stringSecondsList);
                spinner.setAdapter(adapter);
                if (spinnerId == com.sfu.research.breathingaction.R.id.inhaleSpinner) {
                    spinner.setSelection(prefs.getInt(INHALE_SELECTED_ITEM, 0));
                } else {
                    spinner.setSelection(prefs.getInt(EXHALE_SELECTED_ITEM, 0));
                }
            }else if(spinnerId == R.id.exhaleSpinner){
                int[] intSecondsList = getResources().getIntArray(R.array.secondsListExhale);
                for (int i = 0; i < intSecondsList.length; i++) {
                    stringSecondsList.add((float) intSecondsList[i] / 2 + " sec");
                }
                ArrayAdapter adapter = new ArrayAdapter(this, com.sfu.research.breathingaction.R.layout.drop_down_layout, stringSecondsList);
                spinner.setAdapter(adapter);
                if (spinnerId == com.sfu.research.breathingaction.R.id.inhaleSpinner) {
                    spinner.setSelection(prefs.getInt(INHALE_SELECTED_ITEM, 0));
                } else {
                    spinner.setSelection(prefs.getInt(EXHALE_SELECTED_ITEM, 0));
                }
            }else {
                int[] intSecondsList = getResources().getIntArray(com.sfu.research.breathingaction.R.array.secondsListHold);
                for (int i = 0; i < intSecondsList.length; i++) {
                    stringSecondsList.add((float) intSecondsList[i] / 2 + " sec");
                }
                ArrayAdapter adapter = new ArrayAdapter(this, com.sfu.research.breathingaction.R.layout.drop_down_layout, stringSecondsList);
                spinner.setAdapter(adapter);
                if (spinnerId == com.sfu.research.breathingaction.R.id.holdSpinner) {
                    spinner.setSelection(prefs.getInt(HOLD1_SELECTED_ITEM, 0));
                } else {
                    spinner.setSelection(prefs.getInt(HOLD2_SELECTED_ITEM, 0));
                }
            }
        } else {
            int[] intSecondsList = getResources().getIntArray(com.sfu.research.breathingaction.R.array.secondsListTimer);
            for (int i = 0; i < intSecondsList.length; i++) {
                String timerString = getTimeMinutesString(intSecondsList[i]);
                stringSecondsList.add(timerString);
            }
            ArrayAdapter adapter = new ArrayAdapter(this, com.sfu.research.breathingaction.R.layout.drop_down_layout, stringSecondsList);
            spinner.setAdapter(adapter);
            spinner.setSelection(prefs.getInt(TIMER_SELECTED_ITEM, 0));
        }
    }

    private long getTimeFromSpinnerMillis(int spinnerId) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        long timeOnSpinnerMillis;
        int positionOfItemSelected = spinner.getSelectedItemPosition();

        if (spinnerId == com.sfu.research.breathingaction.R.id.timerSpinner) {
            timeOnSpinnerMillis = getResources().getIntArray(com.sfu.research.breathingaction.R.array.secondsListTimer)[positionOfItemSelected];
            timeOnSpinnerMillis = timeOnSpinnerMillis * 1000;
        } else if (spinnerId == com.sfu.research.breathingaction.R.id.inhaleSpinner) {
            timeOnSpinnerMillis = getResources().getIntArray(R.array.secondsListInhale)[positionOfItemSelected];
            timeOnSpinnerMillis = timeOnSpinnerMillis * 1000;
            timeOnSpinnerMillis = timeOnSpinnerMillis / 2;
        } else if(spinnerId == com.sfu.research.breathingaction.R.id.exhaleSpinner){
            timeOnSpinnerMillis = getResources().getIntArray(R.array.secondsListExhale)[positionOfItemSelected];
            timeOnSpinnerMillis = timeOnSpinnerMillis * 1000;
            timeOnSpinnerMillis = timeOnSpinnerMillis / 2;
        }else {
            timeOnSpinnerMillis = getResources().getIntArray(com.sfu.research.breathingaction.R.array.secondsListHold)[positionOfItemSelected];
            timeOnSpinnerMillis = timeOnSpinnerMillis * 1000;
            timeOnSpinnerMillis = timeOnSpinnerMillis / 2;
        }

        return timeOnSpinnerMillis;
    }

    private String getTimeMinutesString(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String secondsString = seconds + "";
        if (seconds < 9) {
            secondsString = "0" + secondsString;
        }

        return minutes + ":" + secondsString;
    }

    private void lockSpinners() {
        Spinner timerSpinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.timerSpinner);
        Spinner inhaleSpinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.inhaleSpinner);
        Spinner exhaleSpinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.exhaleSpinner);
        Spinner hold1_Spinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.holdSpinner);
        Spinner hold2_Spinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.holdSpinner2);

        timerSpinner.setEnabled(false);
        inhaleSpinner.setEnabled(false);
        exhaleSpinner.setEnabled(false);
        hold1_Spinner.setEnabled(false);
        hold2_Spinner.setEnabled(false);
    }

    private void unlockSpinners() {
        Spinner timerSpinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.timerSpinner);
        Spinner inhaleSpinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.inhaleSpinner);
        Spinner exhaleSpinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.exhaleSpinner);
        Spinner hold1_Spinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.holdSpinner);
        Spinner hold2_Spinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.holdSpinner2);

        timerSpinner.setEnabled(true);
        inhaleSpinner.setEnabled(true);
        exhaleSpinner.setEnabled(true);
        hold1_Spinner.setEnabled(true);
        hold2_Spinner.setEnabled(true);
    }

    private void saveSpinnerData() {
        Spinner timerSpinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.timerSpinner);
        Spinner inhaleSpinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.inhaleSpinner);
        Spinner exhaleSpinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.exhaleSpinner);
        Spinner hold1_Spinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.holdSpinner);
        Spinner hold2_Spinner = (Spinner) findViewById(com.sfu.research.breathingaction.R.id.holdSpinner2);

        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putInt(TIMER_SELECTED_ITEM, timerSpinner.getSelectedItemPosition());
        editor.putInt(INHALE_SELECTED_ITEM, inhaleSpinner.getSelectedItemPosition());
        editor.putInt(EXHALE_SELECTED_ITEM, exhaleSpinner.getSelectedItemPosition());
        editor.putInt(HOLD1_SELECTED_ITEM, hold1_Spinner.getSelectedItemPosition());
        editor.putInt(HOLD2_SELECTED_ITEM, hold2_Spinner.getSelectedItemPosition());
        editor.commit();
    }


    private void initiateDatabase(){
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
    }

    private void sendTimeStampToDataBase(){
        DatabaseReference myRef = database.getReference("users");
        Date date = new Date();
        SimpleDateFormat dateF = new SimpleDateFormat("EE MMM dd, yyyy");
        SimpleDateFormat timeF = new SimpleDateFormat("hh:mm:ss a");
        String inhale = ((double) inhaleTimeMillis / 1000) + "";
        String hold1 = ((double) hold_1_TimeMillis / 1000) + "";
        String exhale = ((double) exhaleTimeMillis / 1000) + "";
        String hold2 = ((double) hold_2_TimeMillis / 1000) + "";

        String dateString = dateF.format(date);
        String timeString = timeF.format(date);

        myRef.child(userName).child(dateString).child(timeString).child("inhale").setValue(inhale);
        myRef.child(userName).child(dateString).child(timeString).child("hold 1").setValue(hold1);
        myRef.child(userName).child(dateString).child(timeString).child("exhale").setValue(exhale);
        myRef.child(userName).child(dateString).child(timeString).child("hold 2").setValue(hold2);
    }


    private void playSoundAndVibrate(boolean isEnd) {
        if (isEnd) {
            if (soundEnabled) {
                beepSound.stop();
                beepSound = MediaPlayer.create(this, R.raw.final_sound);
                beepSound.start();
            }
            if (vibrationEnabled)
                vibrator.vibrate(700);
        }else {
            if (soundEnabled) {
                beepSound.stop();
                beepSound = MediaPlayer.create(this, R.raw.beep);
                beepSound.start();
            }
            if (vibrationEnabled){
                vibrator.vibrate(400);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (breathingState != BreathingState.PAUSED && breathingState != BreathingState.NEW_TIMER) {
            pauseTimer();
        }
        saveSpinnerData();
    }

    @Override
    public void onResume() {
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        soundEnabled = defaultPrefs.getBoolean(PREFERENCE_KEY_SOUND_SWITCH, false);
        vibrationEnabled = defaultPrefs.getBoolean(PREFERENCE_KEY_VIBRATION_SWITCH, false);
        userName = defaultPrefs.getString(PREFERENCE_KEY_USER_NAME, "default");
        Log.i("Call", "onresume: " + soundEnabled + " " + vibrationEnabled);
        super.onResume();
    }

}
