package com.S22010440;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Lab2 extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;
    Button loginButton;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab2);

        usernameEditText = findViewById(R.id.editTextText);
        passwordEditText = findViewById(R.id.editTextNumberPassword);
        loginButton = findViewById(R.id.button2);
        dbHelper = new DBHelper(this);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(Lab2.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = dbHelper.insertUser(username, password);
            if (inserted) {
                Toast.makeText(Lab2.this, "User saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Lab2.this, "User already exists!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
