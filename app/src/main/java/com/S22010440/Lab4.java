package com.S22010440;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class Lab4 extends AppCompatActivity implements SensorEventListener {

    private static final float TEMPERATURE_THRESHOLD = 40.0f;

    private SensorManager sensorManager;
    private Sensor ambientTemperatureSensor;
    private TextView tvCurrentTemperature;
    private TextView tvThreshold;
    private TextView tvAlarmStatus;
    private Button btnStopAlarm;

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

        tvCurrentTemperature = findViewById(R.id.tv_current_temperature);
        tvThreshold = findViewById(R.id.tv_threshold);
        tvAlarmStatus = findViewById(R.id.tv_alarm_status);
        btnStopAlarm = findViewById(R.id.btn_stop_alarm);

        tvThreshold.setText("Threshold: " + TEMPERATURE_THRESHOLD + " °C");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ambientTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (ambientTemperatureSensor == null) {
            Toast.makeText(this, "Ambient Temperature Sensor not available.", Toast.LENGTH_LONG).show();
            tvCurrentTemperature.setText("Current Temperature: N/A");
            tvAlarmStatus.setText("Status: Sensor not found");
        }

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.warning_sound);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(false);
                mediaPlayer.setOnCompletionListener(mp -> {
                });
            } else {
            }
        } catch (Exception e) {
        }

        btnStopAlarm.setOnClickListener(v -> stopAlarm());
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
            tvCurrentTemperature.setText(String.format(Locale.getDefault(), "Current Temperature: %.1f °C", currentTemperature));

            if (currentTemperature > TEMPERATURE_THRESHOLD) {
                if (mediaPlayer != null && !isAlarmPlaying && !alarmManuallyStopped) {
                    try {
                        mediaPlayer.start();
                        isAlarmPlaying = true;
                        tvAlarmStatus.setText("Status: ALARM PLAYING!");
                        tvAlarmStatus.setTextColor(Color.YELLOW);
                        btnStopAlarm.setVisibility(View.VISIBLE);
                    } catch (IllegalStateException e) {
                    }
                }
            } else {
                if (isAlarmPlaying || alarmManuallyStopped) {
                    stopAlarm();
                    alarmManuallyStopped = false;
                    tvAlarmStatus.setText("Status: Temperature Normal");
                    tvAlarmStatus.setTextColor(Color.WHITE);
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
            tvAlarmStatus.setText("Status: Idle");
            tvAlarmStatus.setTextColor(Color.WHITE);
            btnStopAlarm.setVisibility(View.GONE);
        }
    }
}
