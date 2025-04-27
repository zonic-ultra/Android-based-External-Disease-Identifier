package com.example.abedi.Controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abedi.Controller.Admin.Disease_Identification.Identify_External_Diseases;
import com.example.abedi.Model.UserDataModel;
import com.example.abedi.R;

public class Menu extends AppCompatActivity {
    UserDataModel myDb;

    Button viewRecordHistoryBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(Build.VERSION.SDK_INT>=19){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        myDb = new UserDataModel(this);
        viewRecordHistoryBtn = (Button)findViewById(R.id.record_history_btn);

        viewAllUsers();

        Button identify_disease_btn = findViewById(R.id.button_identify);
        identify_disease_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Menu.this, Identify_External_Diseases.class);
                startActivity(intent);
            }
        });


    }

    public void viewAllUsers(){
        viewRecordHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = myDb.getAllRecords();

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
                        buffer.append("Student ID:: " + studentId + "\n");
                        buffer.append("Student name:: " + name + "\n\n");
                        lastStudentId = studentId; // Update the last seen student ID
                    }

                    // Append visit details
                    buffer.append("Time of entry:: " + timeOfSeen + "\n");
                    buffer.append("Laboratory purpose:: " + purposeInLab + "\n");
                    buffer.append("Laboratory schedule:: " + scheduleTime + "\n\n");
//                    buffer.append("__________________________________________________________________________\n");
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


        // Create a custom TextView for the title
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20); // Title text size
        titleView.setPadding(16, 16, 16, 16); // Padding for the title
        titleView.setGravity(Gravity.CENTER); // Center the title
        titleView.setBackgroundColor(Color.parseColor("#4B5669")); // Dark gray background for the title
        titleView.setTextColor(Color.WHITE); // White text for the title
        textView.setBackgroundColor(Color.parseColor("#E2E1E7"));



        // Create an AlertDialog with the custom ScrollView
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setCustomTitle(titleView) // Set the styled title
                .setView(scrollView) // Use the ScrollView containing the styled TextView
//                .setPositiveButton("OK", null)
                .show();
    }
}