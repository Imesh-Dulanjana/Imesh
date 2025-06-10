package com.S22010440;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Locale;

public class Lab4 extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "Lab4_TemperatureMonitor";
    private static final float TEMPERATURE_THRESHOLD = 40.0f;

    private SensorManager sensorManager;
    private Sensor ambientTemperatureSensor;
    private TextView textViewCurrentTemperature;
    private TextView textViewThreshold;
    private TextView textViewAlarmStatus;
    private Button buttonStopAlarm;

    private MediaPlayer mediaPlayer;
    private boolean isAlarmPlaying = false;
    private boolean alarmManuallyStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lab4);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewCurrentTemperature = findViewById(R.id.textViewCurrentTemperature);
        textViewThreshold = findViewById(R.id.textViewThreshold);
        textViewAlarmStatus = findViewById(R.id.textViewAlarmStatus);
        buttonStopAlarm = findViewById(R.id.buttonStopAlarm);

        textViewThreshold.setText("Threshold: " + TEMPERATURE_THRESHOLD + " °C");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ambientTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (ambientTemperatureSensor == null) {
            Toast.makeText(this, "Ambient Temperature Sensor not available.", Toast.LENGTH_LONG).show();
            textViewCurrentTemperature.setText("Current Temperature: N/A");
            textViewAlarmStatus.setText("Status: Sensor not found");
        }

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.warning_sound);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(false);
                mediaPlayer.setOnCompletionListener(mp -> {
                    Log.d(TAG, "Alarm sound finished playing.");
                });
            } else {
                Log.e(TAG, "MediaPlayer failed to initialize.");
            }
        } catch (Exception e) {
            Log.e(TAG, "MediaPlayer setup failed: " + e.getMessage(), e);
        }

        buttonStopAlarm.setOnClickListener(v -> stopAlarm());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ambientTemperatureSensor != null) {
            sensorManager.registerListener(this, ambientTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ambientTemperatureSensor != null) {
            sensorManager.unregisterListener(this);
        }
        stopAlarm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            float currentTemperature = event.values[0];
            textViewCurrentTemperature.setText(String.format(Locale.getDefault(), "Current Temperature: %.1f °C", currentTemperature));

            if (currentTemperature > TEMPERATURE_THRESHOLD) {
                if (mediaPlayer != null && !isAlarmPlaying && !alarmManuallyStopped) {
                    try {
                        mediaPlayer.start();
                        isAlarmPlaying = true;
                        textViewAlarmStatus.setText("Status: ALARM PLAYING!");
                        textViewAlarmStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        buttonStopAlarm.setVisibility(View.VISIBLE);
                        Log.i(TAG, "Temperature exceeded threshold. Alarm started.");
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Error playing alarm: " + e.getMessage(), e);
                    }
                }
            } else {
                if (isAlarmPlaying || alarmManuallyStopped) {
                    stopAlarm();
                    alarmManuallyStopped = false;
                    textViewAlarmStatus.setText("Status: Temperature Normal");
                    textViewAlarmStatus.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
                    Log.i(TAG, "Temperature back to normal. Alarm stopped.");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void stopAlarm() {
        if (mediaPlayer != null && isAlarmPlaying) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            }
            isAlarmPlaying = false;
            alarmManuallyStopped = true;
            textViewAlarmStatus.setText("Status: Idle");
            textViewAlarmStatus.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
            buttonStopAlarm.setVisibility(View.GONE);
            Log.i(TAG, "Alarm stopped manually.");
        }
    }
}
