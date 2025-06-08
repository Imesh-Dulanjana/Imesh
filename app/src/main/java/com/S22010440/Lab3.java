package com.S22010440; // package name for the app

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log; // for logging stuff in Logcat
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast; // to show quick messages

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Lab3 extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // google map object
    private EditText editTextAddress; // where user types address
    private Button buttonShowLocation; // button to show location on map
    private Geocoder geocoder; // to get coordinates from address

    private static final String TAG = "Lab3_Map"; // tag for logs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // enable full screen edge to edge
        setContentView(R.layout.activity_lab3);

        // add padding for system bars (status bar, nav bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextAddress = findViewById(R.id.editTextAddress); // find address input
        buttonShowLocation = findViewById(R.id.buttonShowLocation); // find button

        geocoder = new Geocoder(this, Locale.getDefault()); // init geocoder

        // get the map fragment and wait till map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map not found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Map fragment missing in layout");
        }

        // when button clicked, show location
        buttonShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationOnMap();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // show Colombo as starting point with marker
        LatLng colombo = new LatLng(6.9271, 79.8612);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombo, 12));
        mMap.addMarker(new MarkerOptions().position(colombo).title("Colombo"));
    }

    private void showLocationOnMap() {
        String addressString = editTextAddress.getText().toString().trim();

        if (addressString.isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mMap == null) {
            Toast.makeText(this, "Map not ready yet", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Map is null");
            return;
        }

        mMap.clear(); // clear old markers

        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address foundAddress = addresses.get(0);
                LatLng latLng = new LatLng(foundAddress.getLatitude(), foundAddress.getLongitude());

                // put marker on found location
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(foundAddress.getAddressLine(0) != null ? foundAddress.getAddressLine(0) : addressString));

                // zoom in to location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                Toast.makeText(this,
                        "Found: " + (foundAddress.getAddressLine(0) != null ? foundAddress.getAddressLine(0) : addressString) +
                                "\nLat: " + String.format(Locale.getDefault(), "%.4f", foundAddress.getLatitude()) +
                                ", Lon: " + String.format(Locale.getDefault(), "%.4f", foundAddress.getLongitude()),
                        Toast.LENGTH_LONG).show();

                Log.i(TAG, "Found location: " + foundAddress.toString());
            } else {
                Toast.makeText(this, "Address not found. Try again.", Toast.LENGTH_LONG).show();
                Log.w(TAG, "No result for: " + addressString);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error: Check your internet connection.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "IOException: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Invalid address format.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "IllegalArgumentException: " + e.getMessage(), e);
        }
    }
}
