package com.example.abedi.Controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abedi.Model.UserDataModel;
import com.example.abedi.R;
import com.example.abedi.View.IntroStart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SecondFromSlide extends AppCompatActivity {
    UserDataModel usersDb;
    EditText  editTimeOfSeen, editPurposeInLab, editScheduleTime;
    Button submitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second_from_slide);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usersDb = new UserDataModel(this);

        editTimeOfSeen = findViewById(R.id.edit_text_time_of_seen);
        editPurposeInLab = findViewById(R.id.edit_text_purpose_in_lab);
        editScheduleTime = findViewById(R.id.edit_text_time_schedule);
        
        submitBtn = findViewById(R.id.btn_submit);
        addLabVisit();

    }

    public void addLabVisit() {
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

                // Retrieve the studentId from SharedPreferences
                SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
                int studentId = prefs.getInt("studentId", -1);

                if (studentId == -1) {
                    Toast.makeText(SecondFromSlide.this, "Student ID not found.", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    // Parse inputs for Lab Visit
                    String timeOfSeenString = editTimeOfSeen.getText().toString().trim();
                    Date timeOfSeen = dateFormat.parse(timeOfSeenString); // Convert to Date object

                    String purposeInLab = editPurposeInLab.getText().toString().trim();
                    String scheduleTime = editScheduleTime.getText().toString().trim();

                    // Validate inputs
                    if (timeOfSeenString.isEmpty()) {
                        Toast.makeText(SecondFromSlide.this, "Time of Seen is required.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (purposeInLab.isEmpty()) {
                        Toast.makeText(SecondFromSlide.this, "Purpose in Lab is required.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (scheduleTime.isEmpty()) {
                        Toast.makeText(SecondFromSlide.this, "Schedule Time is required.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Insert into Lab Visits
                    boolean isAdded = usersDb.insertToLabVisits(timeOfSeen, purposeInLab, scheduleTime, studentId);

                    // Show result messages
                    if (isAdded) {
                        Toast.makeText(SecondFromSlide.this, "Laboratory Visit successfully added.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SecondFromSlide.this, "Insertion failed", Toast.LENGTH_LONG).show();
                    }
                    startActivity(new Intent(SecondFromSlide.this, Menu.class));
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(SecondFromSlide.this, "Invalid Date Format", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

}