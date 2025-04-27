package com.example.abedi.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class TreatmentSuggestionModel extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Prescription.db";

    public static final String TABLE_NAME = "Treatment_Suggestion";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_CONDITION = "condition";
    public static final String COLUMN_TREATMENT = "treatment";

    public TreatmentSuggestionModel(Context context) {
        super(context , DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String treatment_suggestion =   "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CONDITION + " TEXT UNIQUE NOT NULL, " +
                COLUMN_TREATMENT + " TEXT NOT NULL )";
        db.execSQL(treatment_suggestion);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertToPrescription(String condition, String treatment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CONDITION, condition);
        contentValues.put(COLUMN_TREATMENT, treatment);
        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return result != -1;
    }
    public boolean updatePrescription(String id, String condition, String treatment){
//        SQLiteDatabase db = getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(COLUMN_CONDITION, condition);
//        contentValues.put(COLUMN_TREATMENT, treatment);
//        db.update(TABLE_NAME, contentValues,"ID = ?", new String[]{id});
//        return true;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CONDITION, condition);
        contentValues.put(COLUMN_TREATMENT, treatment);
        int rowsAffected = db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{id});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deletePrescription(String id){
//        SQLiteDatabase db = getWritableDatabase();
//        long result = db.delete(TABLE_NAME, "ID = ?", new String[]{id});
//        if(result == -1){
//            return false;
//        }
//        return true;
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{id});
        db.close();
        return rowsAffected > 0;
    }
    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor res  = db.rawQuery(" SELECT * FROM " + TABLE_NAME,null);
        return res;
    }

    // **Method to fetch treatment based on condition (disease name)**
    public String getTreatmentFromDatabase(String diseaseName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String treatment = "No treatment information available.";

        Cursor cursor = db.rawQuery("SELECT " + COLUMN_TREATMENT + " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_CONDITION + " = ? COLLATE NOCASE", new String[]{diseaseName});
        if (cursor.moveToFirst()) {
            treatment = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return treatment;
    }

}
