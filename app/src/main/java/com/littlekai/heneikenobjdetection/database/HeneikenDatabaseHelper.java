package com.littlekai.heneikenobjdetection.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.littlekai.heneikenobjdetection.model.LocationInfo;

import java.util.ArrayList;

public class HeneikenDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "heneiken_database.db";
    private static final String TABLE_LOCATION = "location";

    private static final String TABLE_LOCATION_ID = "id";
    private static final String TABLE_LOCATION_NAME = "name";
    private static final String TABLE_LOCATION_MAP = "map";
    private static final String TABLE_LOCATION_IMAGE = "image";
    private static final String TABLE_LOCATION_DETECT = "detect";
    private static final String TABLE_LOCATION_DATE = "date";
    private static final String TABLE_LOCATION_USER = "user";
    private static final int DATABASE_VERSION = 1;

    public HeneikenDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("SQLiteDatabase", "onCreate: ");
        // Create the location table with the specified schema
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LOCATION + " (" +
                TABLE_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TABLE_LOCATION_NAME + " VARCHAR(255) NOT NULL, " +
                TABLE_LOCATION_MAP + " VARCHAR(255), " +
                TABLE_LOCATION_IMAGE + " BLOB, " +
                TABLE_LOCATION_DETECT + " TEXT, " +
                TABLE_LOCATION_DATE + " DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                TABLE_LOCATION_USER + " VARCHAR(255)" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @SuppressLint("Range")
    public ArrayList<LocationInfo> getAllLocations() {
        ArrayList<LocationInfo> locationInfos = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LOCATION, null);
        if (cursor.getCount() > 0)
            while (cursor.moveToNext()) {
                LocationInfo locationInfo = new LocationInfo();
                locationInfo.setId(cursor.getInt(cursor.getColumnIndex(TABLE_LOCATION_ID)));
                locationInfo.setName(cursor.getString(cursor.getColumnIndex(TABLE_LOCATION_NAME)));
                locationInfo.setMapUrl(cursor.getString(cursor.getColumnIndex(TABLE_LOCATION_MAP)));
                locationInfo.setImageData(cursor.getBlob(cursor.getColumnIndex(TABLE_LOCATION_IMAGE)));
                locationInfo.setDetect(cursor.getString(cursor.getColumnIndex(TABLE_LOCATION_DETECT)));
                locationInfo.setDateTime(cursor.getString(cursor.getColumnIndex(TABLE_LOCATION_DATE)));
                locationInfo.setUser(cursor.getString(cursor.getColumnIndex(TABLE_LOCATION_USER)));
                locationInfos.add(locationInfo);
            }

        cursor.close();

        return locationInfos;
    }

    public boolean insertLocation(LocationInfo locationInfo) {
        SQLiteDatabase db = getWritableDatabase();
        long rowId = -1; // Initialize rowId to -1 (invalid)

        try {
            ContentValues values = new ContentValues();
            values.put(TABLE_LOCATION_NAME, locationInfo.getName());
            values.put(TABLE_LOCATION_MAP, locationInfo.getMapUrl());
            values.put(TABLE_LOCATION_IMAGE, locationInfo.getImageData());
            values.put(TABLE_LOCATION_DETECT, locationInfo.getDetect());
            values.put(TABLE_LOCATION_USER, locationInfo.getUser());

            rowId = db.insert(TABLE_LOCATION, null, values);
        } finally {
            if (db != null) {
                close();
            }
        }

        return rowId != -1; // Return true if rowId is valid (insertion successful)
    }

    public void editLocation(LocationInfo locationInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TABLE_LOCATION_NAME, locationInfo.getName());
        values.put(TABLE_LOCATION_MAP, locationInfo.getMapUrl());
        values.put(TABLE_LOCATION_IMAGE, locationInfo.getImageData());
        values.put(TABLE_LOCATION_DETECT, locationInfo.getDetect());
        values.put(TABLE_LOCATION_DATE, locationInfo.getDateTime());
        values.put(TABLE_LOCATION_USER, locationInfo.getUser());

        String whereClause = TABLE_LOCATION_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(locationInfo.getId())};

        db.update(TABLE_LOCATION, values, whereClause, whereArgs);

        close();
    }
}
