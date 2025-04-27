package com.example.abedi.Controller.Admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abedi.Model.AdminPasswordHasherModel;
import com.example.abedi.R;

public class ChangePassword extends AppCompatActivity {

    private static final String PREFS_NAME = "AdminPrefs";
    private static final String STORED_HASHED_PASSWORD_KEY = "stored_hashed_password";

    private EditText editTextCurrentPassword, editTextNewPassword, editTextConfirmPassword;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);


        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = editTextCurrentPassword.getText().toString();
            String newPassword = editTextNewPassword.getText().toString();
            String confirmPassword = editTextConfirmPassword.getText().toString();

            // Retrieve the current stored password hash from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String storedHashedPassword = sharedPreferences.getString(STORED_HASHED_PASSWORD_KEY, "");

            // Hash the current entered password
            String hashedCurrentPassword = AdminPasswordHasherModel.hashPassword(currentPassword);

            // Check if the current password matches the stored password hash
            if (hashedCurrentPassword == null || !hashedCurrentPassword.equals(storedHashedPassword)) {
                Toast.makeText(ChangePassword.this, "Current password is incorrect", Toast.LENGTH_LONG).show();
                return;
            }

            // Ensure the new password and confirm password match
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(ChangePassword.this, "New passwords do not match", Toast.LENGTH_LONG).show();
                return;
            }

            // Hash the new password and store it in SharedPreferences
            String hashedNewPassword = AdminPasswordHasherModel.hashPassword(newPassword);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(STORED_HASHED_PASSWORD_KEY, hashedNewPassword);
            editor.apply();

            Toast.makeText(ChangePassword.this, "Password changed successfully", Toast.LENGTH_LONG).show();
            
            finish();  // Close the ChangePassword activity and go back to the previous activity
        });
    }
}
