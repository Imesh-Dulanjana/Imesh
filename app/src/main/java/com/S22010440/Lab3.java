package com.S22010440;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

    private GoogleMap googleMap;
    private EditText addressEditText;
    private Button showLocationButton;
    private Button goToLab4Button; // Added button variable for Lab4 navigation
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lab3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addressEditText = findViewById(R.id.et_address);
        showLocationButton = findViewById(R.id.btn_show_location);
        goToLab4Button = findViewById(R.id.btn_go_to_lab4); // Find the new button by its ID

        geocoder = new Geocoder(this, Locale.getDefault());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map not found", Toast.LENGTH_SHORT).show();
        }

        showLocationButton.setOnClickListener(v -> {
            showAddressOnMap();
        });

        // Set OnClickListener for the new button to navigate to Lab4
        goToLab4Button.setOnClickListener(v -> {
            Intent intent = new Intent(Lab3.this, Lab4.class);
            startActivity(intent);
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng colombo = new LatLng(6.9271, 79.8612);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombo, 12));
        googleMap.addMarker(new MarkerOptions().position(colombo).title("Colombo"));
    }

    private void showAddressOnMap() {
        String addressString = addressEditText.getText().toString().trim();

        if (addressString.isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (googleMap == null) {
            Toast.makeText(this, "Map not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        googleMap.clear();

        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address foundAddress = addresses.get(0);
                LatLng latLng = new LatLng(foundAddress.getLatitude(), foundAddress.getLongitude());

                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(foundAddress.getAddressLine(0) != null ? foundAddress.getAddressLine(0) : addressString));

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                Toast.makeText(this,
                        "Found: " + (foundAddress.getAddressLine(0) != null ? foundAddress.getAddressLine(0) : addressString) +
                                "\nLat: " + String.format(Locale.getDefault(), "%.4f", foundAddress.getLatitude()) +
                                ", Lon: " + String.format(Locale.getDefault(), "%.4f", foundAddress.getLongitude()),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Address not found. Try again.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error: Check your internet connection.", Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Invalid address format.", Toast.LENGTH_LONG).show();
        }
    }
}
