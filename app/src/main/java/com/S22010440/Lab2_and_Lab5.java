package com.S22010440;

import android.content.Intent;
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

public class Lab2_and_Lab5 extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;
    Button loginButton;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lab2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.et_username);
        passwordEditText = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);
        dbHelper = new DBHelper(this);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(Lab2_and_Lab5.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = dbHelper.insertUser(username, password);
            if (inserted) {
                Toast.makeText(Lab2_and_Lab5.this, "User saved successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Lab2_and_Lab5.this, Lab3.class);
                startActivity(intent);
            } else {
                Toast.makeText(Lab2_and_Lab5.this, "User already exists or login failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
