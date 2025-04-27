package com.example.abedi.Controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abedi.Controller.Admin.AdminLogin;
import com.example.abedi.Controller.Admin.AdminPage;
import com.example.abedi.Model.UserDataModel;
import com.example.abedi.R;

public class FirstFormSlide extends AppCompatActivity {
    UserDataModel usersDb;

    EditText editStudentId, editName;
    private Button BtnAdmin, nextBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_form_slide);

        usersDb = new UserDataModel(this);


        editStudentId = findViewById(R.id.edit_text_student_id);
        editName = findViewById(R.id.edit_text_name);
        nextBtn = findViewById(R.id.btn_next);

        BtnAdmin = findViewById(R.id.btn_admin);

        BtnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstFormSlide.this, AdminLogin.class);
                startActivity(intent);
            }
        });
        addUserInfo();
    }

    public void addUserInfo() {
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Get and validate input fields
                    String studentIdToString = editStudentId.getText().toString().trim();
                    String name = editName.getText().toString().trim();

                    if (studentIdToString.isEmpty()) {
                        Toast.makeText(FirstFormSlide.this, "Student ID is required.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (name.isEmpty()) {
                        Toast.makeText(FirstFormSlide.this, "Student Name is required.", Toast.LENGTH_LONG).show();
                        return;
                    }


                    // Parse studentId and check for numeric value
                    int studentId;
                    try {
                        studentId = Integer.parseInt(studentIdToString);
                    } catch (NumberFormatException e) {
                        Toast.makeText(FirstFormSlide.this, "Invalid Student ID. Please enter a numeric value.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Check if the user exists in the database
                    boolean userExists = usersDb.checkIfUserExists(studentId);

                    if (userExists) {
                        // If user exists, proceed to next slide
                        Toast.makeText(FirstFormSlide.this, "User already exists. Proceeding...", Toast.LENGTH_LONG).show();
                        navigateToSecondFormSlide(studentId, name);

                        // Save the studentId in SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("studentId", studentId);
                        editor.apply();
                    } else {

                        // add new user
                        boolean isAdded = usersDb.insertToUserInfo(studentId, name);
                        if (isAdded) {
                            Toast.makeText(FirstFormSlide.this, "User added successfully. Proceeding...", Toast.LENGTH_LONG).show();
                            navigateToSecondFormSlide(studentId, name);

                            // Save the studentId in SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("studentId", studentId);
                            editor.apply();
                        } else {
                            Toast.makeText(FirstFormSlide.this, "Failed to add user. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(FirstFormSlide.this, "Unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Navigation helper method
    private void navigateToSecondFormSlide(int studentId, String name) {
        Intent intent = new Intent(FirstFormSlide.this, SecondFromSlide.class);
        intent.putExtra("studentId", studentId);
        intent.putExtra("studentName", name);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

}