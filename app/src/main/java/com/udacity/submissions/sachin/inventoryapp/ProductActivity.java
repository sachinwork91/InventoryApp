package com.udacity.submissions.sachin.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v4.app.ActivityCompat.requestPermissions;

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ActivityCompat.OnRequestPermissionsResultCallback {

    EditText productNametv;
    EditText productPricetv;
    EditText productQtytv;
    Button increaseButton;
    Button decreaseButton;
    Button placeOrderButton;
    ImageView productImageButton;
    Bitmap productimage = null;
    //static Variables Used
    public static final int PRODUCTDETAILLOADER = 1;
    private static final int CAMERA_INTENT = 0;
    private static final int GALLERY_INTENT = 1;
    public static final int GALLEY_READ_REQUEST = 101;
    public static final int EXTERNAL_WRITE_REQUEST = 102;
    public static final int CAMERA_REQUEST = 103;
    Uri uri;
    private boolean dataChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            dataChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        findViews();
        if (savedInstanceState != null) {
            byte[] bitmapdata = savedInstanceState.getByteArray("productImage");
            if (bitmapdata != null) {
                productimage = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
                productImageButton.setImageBitmap(productimage);
            }
        }
        Intent intent = getIntent();
        uri = intent.getData();
        if (uri == null) {
            setTitle("Add a Product");
        } else {
            setTitle("Edit a Product");
            getSupportLoaderManager().initLoader(PRODUCTDETAILLOADER, null, ProductActivity.this);
        }
        setListener();
    }

    //This method is used to find all the views in the current layout
    void findViews() {
        productNametv = (EditText) findViewById(R.id.productNameDetail);
        productPricetv = (EditText) findViewById(R.id.productPriceDetail);
        productQtytv = (EditText) findViewById(R.id.productQuantityDetail);
        increaseButton = (Button) findViewById(R.id.increaseQty);
        decreaseButton = (Button) findViewById(R.id.decreaseQty);
        placeOrderButton = (Button) findViewById(R.id.placeOrder);
        productImageButton = (ImageView) findViewById(R.id.productImage);
    }

    //This method displays the results
    void displayInfo(Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int productNameIndex = cursor.getColumnIndex(ProductContract.ProductsEntry.PRODUCTNAME);
            int productPriceIndex = cursor.getColumnIndex(ProductContract.ProductsEntry.PRICE);
            int productQuantityIndex = cursor.getColumnIndex(ProductContract.ProductsEntry.QUANTITY);
            int productImageIndex = cursor.getColumnIndex(ProductContract.ProductsEntry.PRODUCTIMAGE);
            //Setting up the Data
            productNametv.setText(cursor.getString(productNameIndex));
            productPricetv.setText(cursor.getInt(productPriceIndex) + "");
            productQtytv.setText(cursor.getInt(productQuantityIndex) + "");

            if (cursor.getBlob(productImageIndex) != null && productimage == null) {
                byte[] byteArray = cursor.getBlob(productImageIndex);

                Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                productimage = bm;
                Log.d("Test", "DisplayInfo Cursor");
                productImageButton.setImageBitmap(bm);
                productImageButton.setBackgroundResource(0);
            }
        }
    }

    //This method is used to set all the listeners
    void setListener() {
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(productQtytv.getText().toString());
                qty++;
                productQtytv.setText(qty + "");
            }
        });
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(productQtytv.getText().toString());

                if (qty == 0) {
                    Toast.makeText(ProductActivity.this, "You have reached the Minimum Qty", Toast.LENGTH_SHORT).show();
                } else {
                    qty--;
                    productQtytv.setText(qty + "");
                }
            }
        });
        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If no data is changed then return
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                /* Fill it with Data */
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"to@email.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Order Confirmation");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Please Confirm My Order for" + productNametv.getText().toString() +
                        " of " + productQtytv.getText().toString() + " Qty " + " At Price " + productPricetv.getText().toString());

                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        });

        productImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        productNametv.setOnTouchListener(mTouchListener);
        productPricetv.setOnTouchListener(mTouchListener);
        productQtytv.setOnTouchListener(mTouchListener);
        increaseButton.setOnTouchListener(mTouchListener);
        decreaseButton.setOnTouchListener(mTouchListener);
        productImageButton.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.product_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.save) {
            insert();
            return true;
        }
        if (id == R.id.delete) {
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, close the current activity.
                            delete();
                        }
                    };

            // Show dialog that there are unsaved changes
            showDeleteDialog(discardButtonClickListener);

            //   delete();
            return true;
        }
        if (id == android.R.id.home) {
            if (!dataChanged) {
                NavUtils.navigateUpFromSameTask(ProductActivity.this);
                Toast.makeText(this, "Data Not Changed ", Toast.LENGTH_SHORT).show();
                return true;
            }

            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that
            // changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(ProductActivity.this);
                        }
                    };

            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader = new CursorLoader(this, uri, null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        displayInfo(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productPricetv.setText("");
        productPricetv.setText("0");
        productQtytv.setText("0");
    }

    //Insert the data in the database
    void insert() {
        String productPrice = productPricetv.getText().toString();
        String productQty = productQtytv.getText().toString();
        //Validating Before Inserting ...
        checkValidation();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductContract.ProductsEntry.PRODUCTNAME, productNametv.getText().toString());
        contentValues.put(ProductContract.ProductsEntry.PRICE, productPrice);
        contentValues.put(ProductContract.ProductsEntry.QUANTITY, productQty);

        if (productimage != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            productimage.compress(Bitmap.CompressFormat.PNG, 0, stream);
            contentValues.put(ProductContract.ProductsEntry.PRODUCTIMAGE, stream.toByteArray());
        } else {
            Toast.makeText(this, "Please Select Product Image ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uri == null) {
            getContentResolver().insert(ProductContract.CONTENT_URI, contentValues);
        } else {
            getContentResolver().update(uri, contentValues, null, null);
            Toast.makeText(ProductActivity.this, "Data updated Successfully", Toast.LENGTH_SHORT).show();
        }
        finish();// To close the current Activity After Updating
    }

    //This Method Deletes
    void delete() {
        getContentResolver().delete(uri, null, null);
        finish();
        Toast.makeText(ProductActivity.this, "Data deleted Successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!dataChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (uri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    //This method checks if the price and qty contains only numbers
    public boolean onlyNumbers(String text) {
        if (text.contains("[a-zA-Z]+") == false && text.length() > 2) {
            return false;
        } else {
            return true;
        }
    }

    //This method selects the image
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductActivity.this);
        builder.setTitle("Add Photo!");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    checkCameraPermission(getBaseContext());
                } else if (items[item].equals("Choose from Library")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkGalleryPermission(getBaseContext());
                    }

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //Starting Gallery Intent
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), GALLERY_INTENT);
    }

    //Starting Camera Intent
    public void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_INTENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_INTENT) {
                Toast.makeText(this, "RETURNED Gallery", Toast.LENGTH_SHORT).show();
                onSelectFromGalleryResult(data);
            } else if (requestCode == CAMERA_INTENT) {
                Toast.makeText(this, "RETURNED CAMERA", Toast.LENGTH_SHORT).show();
                onCaptureImageResult(data);
            }
        }
    }

    //This method handles the result from Gallery Intent
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                productimage = bm;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productImageButton.setImageBitmap(bm);
        productImageButton.setBackgroundResource(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == ProductActivity.CAMERA_REQUEST) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                cameraIntent();
            }

        } else if (requestCode == ProductActivity.GALLEY_READ_REQUEST) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                galleryIntent();
            }
        }
    }

    //Handles the result from camera Intent
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        productimage = thumbnail;
        Bitmap fullImage = null;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        productImageButton.setImageBitmap(thumbnail);
        productImageButton.setBackgroundResource(0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (productimage != null) {
            Bitmap bmp = productimage;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            outState.putByteArray("productImage", byteArray);
        } else {
            outState.putByteArray("productImage", null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkGalleryPermission(final Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, ProductActivity.GALLEY_READ_REQUEST);
            }
        } else {
            galleryIntent();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkCameraPermission(final Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, ProductActivity.CAMERA_REQUEST);
        } else {
            cameraIntent();
        }
    }

    //This method checks for the Validation
    boolean checkValidation() {
        String productPrice = productPricetv.getText().toString();
        String productQty = productQtytv.getText().toString();

        //To handle the New Product
        if (uri == null && !dataChanged) {
            Toast.makeText(this, "Please enter the Details of the Product", Toast.LENGTH_SHORT).show();
            return false;
        }
        //If no data is changed then return
        if (!dataChanged) {
            Toast.makeText(this, "Data Not Changed", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        if (!onlyNumbers(productPrice) || !onlyNumbers(productQty)) {
            Toast.makeText(this, "Please enter Numerical Value in Price and Qty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(productNametv.getText().toString())) {
            Toast.makeText(this, "Product Name Cannot be Empty! ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(productPricetv.getText().toString())) {
            Toast.makeText(this, "Please Provide Product Price ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(productQtytv.getText().toString())) {
            Toast.makeText(this, "Product Quantity Cannot be Empty! ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (productimage == null) {
            Toast.makeText(this, "Please select a Product Image Before inserting", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //This Dailog is used to prompt user while deleting a product
    private void showDeleteDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to Delete?");
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Yes, Please", discardButtonClickListener);
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
