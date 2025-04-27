package com.example.abedi.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDataModel extends SQLiteOpenHelper {

    // Database Name
    private static final  String My_DB_Name = "Users_Db.db";

    //table: User_Info
    private static final  String Table_User_Info =  "User_Info";
    private static final  String col1_user_info =  "student_id";
    private static final  String col2_user_info =  "name";

    //table: Lab_Visits
    private static final  String Table_Lab_Visits =  "Lab_Visits";

    private static final  String col2_lab_visits =  "time_of_seen";
    private static final  String col3_lab_visits =  "laboratory_purpose";
    private static final  String col4_lab_visits =  "laboratory_schedule";
    private static final  String col5_lab_visits =  "student_id";

    public UserDataModel(@Nullable Context context) {
        super(context, My_DB_Name, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //user_info table
        String user_info = " CREATE TABLE User_Info ( student_id INTEGER PRIMARY KEY NOT NULL, " +
                " name TEXT NOT NULL )";


//        //lab_visit table
        String lab_visits = " CREATE TABLE Lab_Visits ( visit_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " time_of_seen DATETIME NOT NULL," +
                " laboratory_purpose TEXT NOT NULL, " +
                " laboratory_schedule TEXT NOT NULL, " +
                " student_id INTEGER NOT NULL," +
                " FOREIGN KEY (student_id) REFERENCES User_Info (student_id) )";

        db.execSQL("PRAGMA foreign_keys = ON;");

        db.execSQL(user_info);
        db.execSQL(lab_visits);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String user_info = "DROP TABLE IF EXISTS User_Info";
        String lab_visits = "DROP TABLE IF EXISTS Lab_Visits";

        db.execSQL(user_info);
        db.execSQL(lab_visits);

        onCreate(db);
    }

    public boolean insertToUserInfo(int student_id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col1_user_info, student_id);
        contentValues.put(col2_user_info, name);
        long result = db.insert(Table_User_Info, null, contentValues);

        if(result == -1){
            return false;
        }
        return true;
    }


    public boolean checkIfUserExists(int student_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String checkStudentQuery = "SELECT * FROM User_Info WHERE student_id = ?";
        Cursor cursor = db.rawQuery(checkStudentQuery, new String[]{String.valueOf(student_id)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }


    public boolean insertToLabVisits(Date time_of_seen, String laboratory_purpose, String laboratory_schedule, int student_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

//        String checkStudentQuery = "SELECT * FROM User_Info WHERE student_id = ?";
//        Cursor cursor = db.rawQuery(checkStudentQuery, new String[]{String.valueOf(student_id)});
//        if (cursor.getCount() == 0) {
//            cursor.close();
//            return false; // Student ID doesn't exist in the User_Info table
//        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        String timeOfSeen = dateFormat.format(time_of_seen);

        contentValues.put(col2_lab_visits, timeOfSeen);
        contentValues.put(col3_lab_visits,laboratory_purpose);
        contentValues.put(col4_lab_visits,laboratory_schedule);
        contentValues.put(col5_lab_visits, student_id);


        long result = db.insert(Table_Lab_Visits, null, contentValues);

        if(result == -1){
            return false;
        }
        return true;
    }


    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to allow multiple entries for time_of_seen, purpose_in_lab, and schedule_time
        String query = "SELECT ui.student_id," +
                " ui.name, " +
                "lv.time_of_seen, " +
                "lv.laboratory_purpose, " +
                "lv.laboratory_schedule " +
                "FROM User_Info ui " +
                "LEFT JOIN Lab_Visits lv ON ui.student_id = lv.student_id " ;
        return db.rawQuery(query, null);
    }

    public Cursor fetchAllRecordsByStudentId(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT visit_id," +
                " name, " +
                "time_of_seen, " +
                "laboratory_purpose, " +
                "laboratory_schedule " +
                "FROM Lab_Visits " +
                "INNER JOIN User_Info ON Lab_Visits.student_id = User_Info.student_id " +
                "WHERE Lab_Visits.student_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(studentId)});
    }
    public boolean deleteLabVisitByVisitId(int visitId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete("Lab_Visits", "visit_id = ?", new String[]{String.valueOf(visitId)});

        return result > 0;
    }

    public boolean deleteByStudentId(int studentId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete lab visits for the student
        db.delete("Lab_Visits", "student_id = ?", new String[]{String.valueOf(studentId)});

        // Delete the student record from UserInfo table
        int result = db.delete("User_Info", "student_id = ?", new String[]{String.valueOf(studentId)});

        return result > 0;
    }


}
