package com.example.abedi.Controller.Admin;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abedi.Model.UserDataModel;
import com.example.abedi.R;

public class RecordHistoryAdmin extends AppCompatActivity {

    private StringBuilder buffer; // In-memory buffer

    EditText editStudentId, editVisitId, editDeleteStudentId;

    Button viewRecordHistoryBtn, searchBtn, deleteLabVisitBtn, deleteStudentBtn;
    UserDataModel usersDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record_history_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usersDb = new UserDataModel(this);

        viewRecordHistoryBtn = findViewById(R.id.btn_record_history);

        // Initialize UI elements
        editStudentId = findViewById(R.id.editText_student_id);
        editVisitId = findViewById(R.id.editVisitId);
        editDeleteStudentId = findViewById(R.id.editStudentId);
        deleteLabVisitBtn = findViewById(R.id.deleteVisitButton);
        deleteStudentBtn = findViewById(R.id.deleteStudentRecordButton);

        searchBtn = findViewById(R.id.searchByStudentId);
        viewRecordsByStudentId();
        viewAllUsers();
        deleteUserByStudentId();
        deleteUserByVisitId();

    }

    public void viewRecordsByStudentId() {
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentIdStr = editStudentId.getText().toString().trim();

                // Check for empty input
                if (studentIdStr.isEmpty()) {
                    Toast.makeText(RecordHistoryAdmin.this, "Please enter a valid student ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate input is numeric
                int studentId;
                try {
                    studentId = Integer.parseInt(studentIdStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(RecordHistoryAdmin.this, "Invalid Student ID format", Toast.LENGTH_SHORT).show();
                    return;
                }

                Cursor res = null;
                try {
                    res = usersDb.fetchAllRecordsByStudentId(studentId);

                    // Check if records are available
                    if (res == null || res.getCount() == 0) {
                        showMessage("Error", "No records found for Student ID: " + studentId);
                        return;
                    }

                    // Building StringBuffer to display history records
                    StringBuffer buffer = new StringBuffer();
                    while (res.moveToNext()) {
                        buffer.append("Visit ID:: " + res.getInt(0) + "\n");
                        buffer.append("Name:: " + res.getString(1) + "\n");
                        buffer.append("Time of Seen:: " + res.getString(2) + "\n");
                        buffer.append("Laboratory purpose:: " + res.getString(3) + "\n");
                        buffer.append("Laboratory schedule:: " + res.getString(4) + "\n\n");
                    }

                    showMessage("Record of Student ID: " + studentId, buffer.toString());
                } finally {
                    if (res != null) {
                        res.close();
                    }
                }
            }

        });
    }

    public void viewAllUsers() {
        viewRecordHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = usersDb.getAllRecords();
                if (res.getCount() == 0) {
                    showMessage("Error", "No Data Available");
                    return;
                }

                StringBuffer buffer = new StringBuffer();
                String lastStudentId = ""; // Track last student ID to avoid duplicate headers

                while (res.moveToNext()) {
                    String studentId = res.getString(0);
                    String name = res.getString(1);
                    String timeOfSeen = res.getString(2);
                    String purposeInLab = res.getString(3);
                    String scheduleTime = res.getString(4);

                    // Only append student header if it's a new student ID
                    if (!lastStudentId.equals(studentId)) {
                        buffer.append("__________________________________________________________________________\n\n");
                        buffer.append("Student Id:: " + studentId + "\n");
                        buffer.append("Name:: " + name + "\n\n");
                        lastStudentId = studentId; // Update the last seen student ID
                    }

                    // Append visit and schedule details
                    buffer.append("Time of entry:: " + timeOfSeen + "\n");
                    buffer.append("Laboratory purpose:: " + purposeInLab + "\n");
                    buffer.append("Laboratory schedule:: " + scheduleTime + "\n\n");
                }

                showMessage("Record History", buffer.toString());
            }
        });
    }

    public void showMessage(String title, String message) {
        // Create a ScrollView to enable scrolling
        ScrollView scrollView = new ScrollView(this);

        // Create a TextView to display the message
        TextView textView = new TextView(this);

        // Set the text from the message
        textView.setText(message);

        // Customize the TextView's appearance
        textView.setTextSize(14); // Set text size
        textView.setPadding(16, 16, 16, 16); // Add padding for better appearance
        textView.setBackgroundColor(Color.parseColor("#EFEFEF")); // Set background color (light gray)
        textView.setTextColor(Color.BLACK); // Set text color

        // Add the TextView to the ScrollView
        scrollView.addView(textView);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setPadding(16, 16, 16, 16);
        titleView.setGravity(Gravity.CENTER);
        titleView.setBackgroundColor(Color.parseColor("#4B5669"));
        titleView.setTextColor(Color.WHITE);
        textView.setBackgroundColor(Color.parseColor("#E2E1E7"));

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setCustomTitle(titleView)
                .setView(scrollView)
                .show();
    }

    public void deleteUserByStudentId(){
        // Set listener to delete all lab visits for a student by student_id
        deleteStudentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentIdString = editDeleteStudentId.getText().toString().trim();

                if (studentIdString.isEmpty()) {
                    Toast.makeText(RecordHistoryAdmin.this, "Student ID is required.", Toast.LENGTH_LONG).show();
                } else {
                    int studentId = Integer.parseInt(studentIdString);
                    boolean isDeleted = usersDb.deleteByStudentId(studentId);
                    if (isDeleted) {
                        Toast.makeText(RecordHistoryAdmin.this, "User deleted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RecordHistoryAdmin.this, "Failed to delete user", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void deleteUserByVisitId() {
        deleteLabVisitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String visitIdString = editVisitId.getText().toString().trim();

                if (visitIdString.isEmpty()) {
                    Toast.makeText(RecordHistoryAdmin.this, "Visit ID is required.", Toast.LENGTH_LONG).show();
                } else {
                    int visitId = Integer.parseInt(visitIdString);
                    boolean isDeleted = usersDb.deleteLabVisitByVisitId(visitId);

                    if (isDeleted) {
                        Toast.makeText(RecordHistoryAdmin.this, "Lab Visit deleted successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RecordHistoryAdmin.this, "Failed to delete Lab Visit", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}