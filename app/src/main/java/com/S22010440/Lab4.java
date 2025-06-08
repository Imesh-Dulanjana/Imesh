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
    private static final float TEMPERATURE_THRESHOLD = 40.0f; // max temperature allowed

    private SensorManager sensorManager;
    private Sensor ambientTemperatureSensor;
    private TextView textViewCurrentTemperature;
    private TextView textViewThreshold;
    private TextView textViewAlarmStatus;
    private Button buttonStopAlarm;

    private MediaPlayer mediaPlayer; // to play warning sound
    private boolean isAlarmPlaying = false; // to check if alarm is already playing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // to make app go edge to edge on screen
        setContentView(R.layout.activity_lab4); // load the layout

        // padding for status bar and nav bar so UI fits well
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // connect UI elements with variables
        textViewCurrentTemperature = findViewById(R.id.textViewCurrentTemperature);
        textViewThreshold = findViewById(R.id.textViewThreshold);
        textViewAlarmStatus = findViewById(R.id.textViewAlarmStatus);
        buttonStopAlarm = findViewById(R.id.buttonStopAlarm);

        // show the threshold temp on screen
        textViewThreshold.setText("Threshold: " + TEMPERATURE_THRESHOLD + " °C");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // print all sensors on the device to the log, useful for debugging
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.d(TAG, "--- Listing All Available Sensors ---");
        if (deviceSensors.isEmpty()) {
            Log.d(TAG, "No sensors found on this device.");
        } else {
            for (Sensor sensor : deviceSensors) {
                Log.d(TAG, "Sensor Name: " + sensor.getName() +
                        ", Type: " + sensor.getStringType() +
                        ", Vendor: " + sensor.getVendor() +
                        ", Version: " + sensor.getVersion());
            }
        }
        Log.d(TAG, "--- End of Sensor List ---");

        ambientTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        // check if device has ambient temperature sensor
        if (ambientTemperatureSensor == null) {
            Toast.makeText(this, "Ambient Temperature Sensor not available on this device.", Toast.LENGTH_LONG).show();
            textViewCurrentTemperature.setText("Current Temperature: N/A");
            textViewAlarmStatus.setText("Status: Sensor not found");
            Log.w(TAG, "TYPE_AMBIENT_TEMPERATURE sensor not found on this device.");
        } else {
            Log.d(TAG, "TYPE_AMBIENT_TEMPERATURE sensor found.");
        }

        // prepare MediaPlayer to play alarm sound
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.warning_sound);
            if (mediaPlayer == null) {
                Log.e(TAG, "Failed to create MediaPlayer from R.raw.warning_sound.");
                Toast.makeText(this, "Error initializing alarm sound.", Toast.LENGTH_LONG).show();
            } else {
                mediaPlayer.setLooping(false); // sound will not repeat automatically
                mediaPlayer.setOnCompletionListener(mp -> {
                    Log.d(TAG, "Alarm sound finished playing.");
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during MediaPlayer setup: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up alarm sound.", Toast.LENGTH_LONG).show();
            mediaPlayer = null;
        }

        // when stop alarm button clicked, call stopAlarm function
        buttonStopAlarm.setOnClickListener(v -> stopAlarm());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ambientTemperatureSensor != null) {
            // register listener for ambient temperature sensor
            sensorManager.registerListener(this, ambientTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Sensor listener registered.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ambientTemperatureSensor != null) {
            // unregister sensor to save battery
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Sensor listener unregistered.");
        }
        stopAlarm(); // stop alarm if app is paused
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // free media player resources
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer released.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            float currentTemperature = event.values[0];
            textViewCurrentTemperature.setText(String.format(Locale.getDefault(), "Current Temperature: %.1f °C", currentTemperature));

            if (currentTemperature > TEMPERATURE_THRESHOLD) {
                if (mediaPlayer != null && !isAlarmPlaying) {
                    try {
                        mediaPlayer.start();
                        isAlarmPlaying = true;
                        textViewAlarmStatus.setText("Status: ALARM PLAYING!");
                        textViewAlarmStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        buttonStopAlarm.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Temperature Exceeded Threshold! Alarm Playing!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Temperature exceeded threshold. Alarm started.");
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Error starting MediaPlayer: " + e.getMessage(), e);
                        Toast.makeText(this, "Error playing alarm sound.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                if (isAlarmPlaying) {
                    stopAlarm();
                    textViewAlarmStatus.setText("Status: Temperature Normal");
                    textViewAlarmStatus.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
                    Toast.makeText(this, "Temperature is now normal. Alarm stopped.", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Temperature dropped below threshold. Alarm stopped.");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Sensor accuracy changed for " + sensor.getName() + ": " + accuracy);
    }

    private void stopAlarm() {
        if (mediaPlayer != null && isAlarmPlaying) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            }
            isAlarmPlaying = false;
            textViewAlarmStatus.setText("Status: Idle");
            textViewAlarmStatus.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
            buttonStopAlarm.setVisibility(View.GONE);
            Log.i(TAG, "Alarm stopped.");
        }
    }
}
