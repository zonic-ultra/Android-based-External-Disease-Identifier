package com.example.abedi.Controller.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.abedi.Model.AdminPasswordHasherModel;
import com.example.abedi.R;

public class AdminLogin extends AppCompatActivity {
    // Key for SharedPreferences
    private static final String PREFS_NAME = "AdminPrefs";
    private static final String STORED_HASHED_PASSWORD_KEY = "stored_hashed_password";

    private EditText editTextPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Check if the password hash is already stored in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String storedHashedPassword = sharedPreferences.getString(STORED_HASHED_PASSWORD_KEY, "");

        // If the password is not set (first-time launch), store the default hashed password
        if (storedHashedPassword.isEmpty()) {
            String defaultPassword = "000Admin";
            String defaultHashedPassword = AdminPasswordHasherModel.hashPassword(defaultPassword);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(STORED_HASHED_PASSWORD_KEY, defaultHashedPassword);
            editor.apply();
        }

        btnLogin.setOnClickListener(v -> {
            String enteredPassword = editTextPassword.getText().toString();

            // Ig hahash niya an gin entered na password gamit yung AdminPasswordHasherModel
            String hashedEnteredPassword = AdminPasswordHasherModel.hashPassword(enteredPassword);

            // Compare the entered hashed password with the stored hash
            if (hashedEnteredPassword != null && hashedEnteredPassword.equals(storedHashedPassword)) {
                Toast.makeText(AdminLogin.this, "Access Granted", Toast.LENGTH_LONG).show();

                startActivity(new Intent(AdminLogin.this, AdminPage.class));
            } else {
                Toast.makeText(AdminLogin.this, "Invalid password", Toast.LENGTH_LONG).show();
            }

            editTextPassword.setText("");
        });
    }
}
