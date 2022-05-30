package com.example.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 *  Helper Class to help create, open and manage database connections and also version management.
 */
public class CarDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cars.db";
    private static final int DATABASE_VERSION = 1;

    public CarDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_CAR_TABLE = "CREATE TABLE " + CarContract.CarEntry.TABLE_NAME + " ("
                + CarContract.CarEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CarContract.CarEntry.COLUMN_CAR_IMAGE + " TEXT NOT NULL, "
                + CarContract.CarEntry.COLUMN_CAR_NAME + " TEXT NOT NULL, "
                + CarContract.CarEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + CarContract.CarEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, "
                + CarContract.CarEntry.COLUMN_CAR_PRICE + " INTEGER NOT NULL DEFAULT 1, "
                + CarContract.CarEntry.COLUMN_CAR_STOCK + " INTEGER NOT NULL DEFAULT 0);";

        db.execSQL(SQL_CREATE_CAR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
