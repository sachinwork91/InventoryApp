package com.udacity.submissions.sachin.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
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
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ActivityCompat.OnRequestPermissionsResultCallback {

    EditText productNametv;
    EditText productPricetv;
    EditText productQtytv;
    Button increaseButton;
    Button decreaseButton;
    Button placeOrderButton;
    ImageButton productImageButton;

    public static final int PRODUCTDETAILLOADER = 1;
    private static final int CAMERA_INTENT = 0;
    private static final int GALLERY_INTENT = 1;
    String userChoosenTask ="";

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
        productImageButton = (ImageButton) findViewById(R.id.productImage);
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
            //Setting up the Data
            productNametv.setText(cursor.getString(productNameIndex));
            productPricetv.setText(cursor.getInt(productPriceIndex) + "");
            productQtytv.setText(cursor.getInt(productQuantityIndex) + "");
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
                if (!dataChanged) {
                    Toast.makeText(ProductActivity.this, "Please enter the Detais", Toast.LENGTH_SHORT).show();
                    return;
                }

                insert();
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
            delete();
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

    void insert() {
        //If no data is changed then return
        if (!dataChanged) {
            return;
        }
        String productPrice = productPricetv.getText().toString();
        String productQty = productQtytv.getText().toString();

        if (!onlyNumbers(productPrice) || !onlyNumbers(productQty) ) {
            Toast.makeText(this, "Please enter Numerical Value in Price and Qty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(productNametv.getText().toString())){
            Toast.makeText(this, "Product Name Cannot be Empty! ", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductContract.ProductsEntry.PRODUCTNAME, productNametv.getText().toString());
        contentValues.put(ProductContract.ProductsEntry.PRICE, productPrice);
        contentValues.put(ProductContract.ProductsEntry.QUANTITY, productQty);

        if (uri == null) {
            getContentResolver().insert(ProductContract.CONTENT_URI, contentValues);
        } else {
            getContentResolver().update(uri, contentValues, null, null);
            Toast.makeText(ProductActivity.this, "Data updated Successfully", Toast.LENGTH_SHORT).show();
        }
        finish();// TO close the current Activity After Updating
    }

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

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductActivity.this);
        builder.setTitle("Add Photo!");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    boolean result=Utility.checkPermission(ProductActivity.this, 2);
                    Log.d("Test", "REsult from camera " + result);
                    userChoosenTask = "Take Photo";
                    if(result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    boolean result=Utility.checkPermission(ProductActivity.this, 1);
                    userChoosenTask = "Choose from Library";
                    if(result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), GALLERY_INTENT);
    }
    public  void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_INTENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TEST", "result code  => " + resultCode );

        if ( resultCode == Activity.RESULT_OK ) {
            if (requestCode == GALLERY_INTENT){
                Toast.makeText(this, "RETURNED Gallery", Toast.LENGTH_SHORT).show();
                onSelectFromGalleryResult(data);
            }else if (requestCode == CAMERA_INTENT){
                Toast.makeText(this, "RETURNED CAMERA", Toast.LENGTH_SHORT).show();
                onCaptureImageResult(data);
            }
               // onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productImageButton.setImageBitmap(bm);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == Utility.MY_PERMISSIONS_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
              cameraIntent();
            } else {
                Toast.makeText(this, "SOME HAPPENDED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
       // thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
       /* File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
       */

        productImageButton.setBackgroundResource(0);
       productImageButton.setImageBitmap(thumbnail);
    //   productImageButton.setImageBitmap(thumbnail, 0 , 12, false);
       //profileImage.setImageBitmap(Bitmap.createScaledBitmap(b, 120, 120, false));


    }




}
