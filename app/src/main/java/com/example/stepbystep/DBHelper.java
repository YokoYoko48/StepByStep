package com.example.stepbystep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "step_tracker.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "activities";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_SHAKES = "shakes";
    public static final String COLUMN_DURATION = "duration";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_STEPS + " INTEGER, " +
                COLUMN_SHAKES + " INTEGER, " +
                COLUMN_DURATION + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // เมธอดสำหรับบันทึกข้อมูลใหม่ลงฐานข้อมูล
    public boolean insertActivity(String date, int steps, int shakes, String duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_STEPS, steps);
        values.put(COLUMN_SHAKES, shakes);
        values.put(COLUMN_DURATION, duration);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1; // ถ้าคืนค่า true แปลว่าบันทึกสำเร็จ
    }

    // เมธอดสำหรับดึงข้อมูลประวัติทั้งหมดมาแสดงในแอป
    public List<ActivityItem> getAllActivities() {
        List<ActivityItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS));
                int shakes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SHAKES));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION));

                list.add(new ActivityItem(id, date, steps, shakes, duration));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // เมธอดสำหรับลบประวัติกิจกรรม
    public void deleteActivity(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    // เมธอดดึงผลรวมก้าวทั้งหมดจากประวัติ
    public int getTotalSteps() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_STEPS + ") FROM " + TABLE_NAME, null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    // เมธอดดึงผลรวมการเขย่าทั้งหมดจากประวัติ
    public int getTotalShakes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_SHAKES + ") FROM " + TABLE_NAME, null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return total;
    }
}