package com.udacity.submissions.sachin.inventoryapp;


import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView listView;
    ProductsAdapter productsAdapter;
    private static final int LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list);
        productsAdapter = new ProductsAdapter(this, null);
        listView.setAdapter(productsAdapter);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        TextView emptyView = findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Uri uri = ContentUris.withAppendedId(ProductContract.ProductsEntry.CONTENT_URI, id);
                Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                startActivity(intent);
            }
        });

        //Setting up the Loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.deleteAllAction) {

            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, close the current activity.
                            deleteAll();
                        }
                    };

            // Show dialog that there are unsaved changes
            showDeleteAllDialog(discardButtonClickListener);
            return true;
        }
        if (id == R.id.dummyAction) {
            insert();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //This method inserts the data
    public void insert() {
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductsEntry.PRODUCTNAME, "PHONE");
        values.put(ProductContract.ProductsEntry.PRICE, 12);
        values.put(ProductContract.ProductsEntry.QUANTITY, 1);
        //Insert the Dummy Data in the Database
        getContentResolver().insert(ProductContract.ProductsEntry.CONTENT_URI, values);
    }

    //This method delets all the inventory data
    public void deleteAll() {
        getContentResolver().delete(ProductContract.ProductsEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader = new CursorLoader(this, ProductContract.ProductsEntry.CONTENT_URI,
                null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        productsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productsAdapter.swapCursor(null);
    }

    private void showDeleteAllDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete All Data");
        builder.setPositiveButton("Confirm Delete! ?", discardButtonClickListener);
        builder.setNegativeButton("Cancel", null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
