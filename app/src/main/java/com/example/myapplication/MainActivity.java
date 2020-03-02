package com.example.myapplication;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import static android.R.*;
import static android.R.color.black;
import static com.example.myapplication.R.color.colorAccent;
import static com.example.myapplication.R.color.colorPrimaryDark;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NOTIFY = "key_notify";
    private static final String KEY_INTERVAL = "key_interval";
    private static final String KEY_HOUR = "key_hour";
    private static final String KEY_MINUTE = "key_minute";
    private TimePicker timePicker;
    private EditText editMinutes;
    private Button bntNotify;
    private int interval;
    private int hours;
    private int minutes;
    private boolean activated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.time_picker);
        editMinutes = findViewById(R.id.editText);
        bntNotify = findViewById(R.id.button);

        final SharedPreferences storage = getSharedPreferences("storage", Context.MODE_PRIVATE);
        activated=storage.getBoolean(KEY_NOTIFY, false);


        if(activated){
            bntNotify.setText(R.string.pause);
            bntNotify.setBackgroundResource(black);

            editMinutes.setText(String.valueOf(storage.getInt(KEY_INTERVAL,0)));
            timePicker.setHour(storage.getInt(KEY_HOUR,timePicker.getHour()));
            timePicker.setMinute(storage.getInt(KEY_MINUTE,timePicker.getMinute()));


        }else{
            bntNotify.setText(R.string.notify);
            bntNotify.setBackgroundResource(colorAccent);

        }


        timePicker.setIs24HourView(true);

        bntNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!activated) {

                    String sInterval = editMinutes.getText().toString();

                    if (sInterval.isEmpty()) {
                        Toast.makeText(MainActivity.this, getString(R.string.validation), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    interval = Integer.parseInt(sInterval);
                    hours = timePicker.getHour();
                    minutes = timePicker.getMinute();

                    bntNotify.setText(R.string.pause);
                    bntNotify.setBackgroundResource(black);

                    SharedPreferences.Editor edit = storage.edit();
                    edit.putBoolean(KEY_NOTIFY, true);
                    edit.putInt(KEY_INTERVAL,interval);
                    edit.putInt(KEY_HOUR,hours);
                    edit.putInt(KEY_MINUTE,minutes);
                    edit.apply();

                    Intent notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
                    notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1);
                    notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION,"Hora de beber água");

                    PendingIntent broadcast= PendingIntent.getBroadcast(MainActivity.this,0,
                            notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                    long futureInMillis = SystemClock.elapsedRealtime() + (interval * 1000);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, broadcast);

                    activated = true;

                }else{
                    bntNotify.setText(R.string.notify);
                    bntNotify.setBackgroundResource(colorAccent);

                    SharedPreferences.Editor edit = storage.edit();
                    edit.putBoolean(KEY_NOTIFY, false);
                    edit.remove(KEY_INTERVAL);
                    edit.remove(KEY_HOUR);
                    edit.remove(KEY_MINUTE);
                    edit.apply();

                    activated = false;
                }
            }
        });

    }
}
