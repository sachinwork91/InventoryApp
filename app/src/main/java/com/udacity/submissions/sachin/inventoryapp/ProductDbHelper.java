package com.udacity.submissions.sachin.inventoryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sachin on 2018-04-25.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "products.db";

    private static final String CREATE_TABLE = "CREATE TABLE " + ProductContract.ProductsEntry.TABLE_NAME + " ( "
            + ProductContract.ProductsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ProductContract.ProductsEntry.PRODUCTNAME + " TEXT NOT NULL, "
            + ProductContract.ProductsEntry.PRICE + " INTEGER NOT NULL,  "
            + ProductContract.ProductsEntry.QUANTITY + " INTEGER NOT NULL DEFAULT 0 ,  "
            + ProductContract.ProductsEntry.PRODUCTIMAGE + " BLOB ); "  ;


    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
