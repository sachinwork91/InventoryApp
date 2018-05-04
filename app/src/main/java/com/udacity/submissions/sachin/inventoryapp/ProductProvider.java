package com.udacity.submissions.sachin.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Sachin on 2018-04-26.
 */

public class ProductProvider extends ContentProvider {

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, "products", PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, "products/#", PRODUCT_ID);
    }

    ProductDbHelper productDbHelper;

    @Override
    public boolean onCreate() {
        productDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = productDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        if (match == PRODUCTS) {
            cursor = db.query(ProductContract.ProductsEntry.TABLE_NAME, null, null, null, null, null, null);
        } else if (match == PRODUCT_ID) {
            selection = ProductContract.ProductsEntry._ID + "=?";
            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            cursor = db.query(ProductContract.ProductsEntry.TABLE_NAME, null,
                    selection,
                    selectionArgs,
                    null,
                    null, sortOrder);

        } else {
            throw new IllegalArgumentException("Cannot Query Unknown URI  " + uri);
        }

        // Setting the notification URI on the Cursor,
        // To what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
        default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = productDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        if( match == PRODUCTS ){
        selection = null;
        selectionArgs = null;
        }else if (match==PRODUCT_ID){
            selection = ProductContract.ProductsEntry._ID + "=?";
            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

        }

        int del_id = db.delete(ProductContract.ProductsEntry.TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return del_id;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = productDbHelper.getWritableDatabase();
        selection = ProductContract.ProductsEntry._ID + "=?";
        selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
        int id = db.update(ProductContract.ProductsEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        SQLiteDatabase db = productDbHelper.getWritableDatabase();
        long newRowId = db.insert(ProductContract.ProductsEntry.TABLE_NAME, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, newRowId);
    }
}
