package com.example.abedi.Controller.Admin;

import static java.lang.Character.toUpperCase;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
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

import com.example.abedi.Model.TreatmentSuggestionModel;
import com.example.abedi.R;

import java.util.Locale;

public class TreatmentSuggestionAdmin extends AppCompatActivity {
    TreatmentSuggestionModel prescriptionDb;
    EditText conditionInput, editCondition, editTreatment, editID;
    Button addBtn, suggestButton, viewAllDataBtn, update_data_btn, delete_data_btn, viewExternalDiseaseAvailableBtn;
    private TextView suggestionOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_treatment_suggestion_admin);
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

        prescriptionDb = new TreatmentSuggestionModel(this);

        conditionInput = (EditText) findViewById(R.id.conditionInput);
        suggestionOutput=(TextView) findViewById(R.id.suggestionOutput);
        editID = (EditText)findViewById(R.id.editText_id);

        editCondition = (EditText)findViewById(R.id.condition);
        editTreatment = (EditText)findViewById(R.id.treatment);

        viewExternalDiseaseAvailableBtn = (Button)findViewById(R.id.view_EDA_btn);
        suggestButton = (Button) findViewById(R.id.suggestButton);
        update_data_btn = (Button)findViewById(R.id.update_data_btn);
        delete_data_btn = (Button)findViewById(R.id.delete_data_btn);

        suggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestTreatment();
            }
        });
        addBtn = (Button) findViewById(R.id.addBtn);

        viewAllDataBtn = (Button)findViewById(R.id.view_all_data_btn);

        addTreatmentSuggestion();

        viewAllTreatmentSuggestionData();

        updateTreatmentSuggestion();

        deleteTreatmentSuggestion();

        viewExternalDiseaseAvailable();

        TextView textView = findViewById(R.id.suggestionOutput);
        textView.setMovementMethod(new ScrollingMovementMethod());

    }

    public void viewExternalDiseaseAvailable(){
        viewExternalDiseaseAvailableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = prescriptionDb.getAllData();

                if (res.getCount() == 0){
                    showMessage("ERROR", "Not Data Available");
                    return;
                }
                StringBuffer buffer = new StringBuffer();

                while (res.moveToNext()){
                    buffer.append(":: " + res.getString(1)+"\n");
                }
                showMessage("DISEASES", buffer.toString());

            }
        });
    }
    public void addTreatmentSuggestion(){
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String condition = editCondition.getText().toString().trim();
                String treatment = editTreatment.getText().toString().trim();
                if (condition.isEmpty()||treatment.isEmpty()){
                    Toast.makeText(TreatmentSuggestionAdmin.this, "Required all fields.", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean isAdded = prescriptionDb.insertToPrescription(condition,treatment);
                if (isAdded){
                    Toast.makeText(TreatmentSuggestionAdmin.this, "added successfully.", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(TreatmentSuggestionAdmin.this, "added unsuccessfully.", Toast.LENGTH_LONG).show();
                }
                editCondition.setText("");
                editTreatment.setText("");
            }
        });
    }
    public void suggestTreatment(){

        String condition = conditionInput.getText().toString().trim();
        SQLiteDatabase db = prescriptionDb.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT "+ TreatmentSuggestionModel.COLUMN_TREATMENT +
                " FROM "+ TreatmentSuggestionModel.TABLE_NAME +
                " WHERE "+ TreatmentSuggestionModel.COLUMN_CONDITION + "=? COLLATE NOCASE ", new String[]{condition});

            if (cursor.moveToFirst()) {
                String treatment = cursor.getString(0);
                suggestionOutput.setText(" " + treatment);
            } else {
                suggestionOutput.setText("No suggestion found for this condition.");
            }
        cursor.close();

    }
    public void viewAllTreatmentSuggestionData(){
        viewAllDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = prescriptionDb.getAllData();

                if (res.getCount() == 0){
                    showMessage("ERROR", "Not Data Available");
                    return;
                }
                StringBuffer buffer = new StringBuffer();

                while (res.moveToNext()){
                    buffer.append("DISEASE ID:: " + res.getString(0) + "\n");
                    buffer.append("CONDITION:: " + res.getString(1)+"\n");
                    buffer.append("TREATMENT:: " + res.getString(2)+"\n\n");
                }
                showMessage("Treatment Suggestions", buffer.toString());

            }
        });
    }

    public void updateTreatmentSuggestion(){
        update_data_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editID.getText().toString().trim();
                String condition = editCondition.getText().toString().trim();
                String treatment = editTreatment.getText().toString().trim();

                if (condition.isEmpty() || treatment.isEmpty()){
                    Toast.makeText(TreatmentSuggestionAdmin.this, "Required all fields..", Toast.LENGTH_LONG).show();
                    return;
                }else if (id.isEmpty()){
                    Toast.makeText(TreatmentSuggestionAdmin.this, "Required Disease ID.", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean isUpdated = prescriptionDb.updatePrescription(id, condition, treatment);

                if (isUpdated){
                    Toast.makeText(TreatmentSuggestionAdmin.this, "updated successfully.", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(TreatmentSuggestionAdmin.this, "update unsuccessful.", Toast.LENGTH_LONG).show();
                }
                editID.setText("");
                editCondition.setText("");
                editTreatment.setText("");
            }
        });
    }

    public void deleteTreatmentSuggestion(){
        delete_data_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDeleted = prescriptionDb.deletePrescription(editID.getText().toString().trim());

                if(isDeleted){
                    Toast.makeText(TreatmentSuggestionAdmin.this, "deleted successfully.", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(TreatmentSuggestionAdmin.this, "unsuccessfully deleted.", Toast.LENGTH_LONG).show();

                }
                editID.setText("");
            }
        });
    }


    public void showMessage(String title, String message) {
        // ScrollView to enable scrolling
        ScrollView scrollView = new ScrollView(this);

        // TextView to display the message
        TextView textView = new TextView(this);

        // Set the text from the message
        textView.setText(message);

        // Customize the TextView's appearance
        textView.setTextSize(14); // Set text size
        textView.setPadding(16, 16, 16, 16);
        textView.setBackgroundColor(Color.parseColor("#A8E6CF"));
        textView.setTextColor(Color.BLACK);

        // Add the TextView to the ScrollView
        scrollView.addView(textView);


        // Custom TextView for the title
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setPadding(16, 16, 16, 16);
        titleView.setGravity(Gravity.CENTER);
        titleView.setBackgroundColor(Color.BLACK);
        titleView.setTextColor(Color.WHITE);


        // AlertDialog with the custom ScrollView
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setCustomTitle(titleView)
                .setView(scrollView)
                .show();
    }

}