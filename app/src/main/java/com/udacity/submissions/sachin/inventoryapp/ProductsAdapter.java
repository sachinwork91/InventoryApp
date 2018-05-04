package com.udacity.submissions.sachin.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Sachin on 2018-04-26.
 */

public class ProductsAdapter extends CursorAdapter {

    Context context;

    public ProductsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView productNametv = (TextView) view.findViewById(R.id.productName);
        TextView productPricetv = (TextView) view.findViewById(R.id.productPrice);
        TextView productQuantity = (TextView) view.findViewById(R.id.productQuantity);
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.imageButton);

        int productNameIndex = cursor.getColumnIndex(ProductContract.ProductsEntry.PRODUCTNAME);
        int productPriceIndex = cursor.getColumnIndex(ProductContract.ProductsEntry.PRICE);
        int productQuantityIndex = cursor.getColumnIndex(ProductContract.ProductsEntry.QUANTITY);

        //Setting the Data in the views
        productNametv.setText(cursor.getString(productNameIndex));
        productPricetv.setText(cursor.getInt(productPriceIndex) + "");
        productQuantity.setText(cursor.getInt(productQuantityIndex) + "");
        final int quantity = cursor.getInt(productQuantityIndex);
        int productIdIndex = cursor.getColumnIndex(ProductContract.ProductsEntry._ID);
        final int productID = cursor.getInt(productIdIndex);
        final String productName = cursor.getString(productNameIndex);
        final int productPrice = cursor.getInt(productPriceIndex);
        final int productQty = cursor.getInt(productQuantityIndex);
        final Uri uri = ContentUris.withAppendedId(ProductContract.ProductsEntry.CONTENT_URI, productID);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Setting up Content Values
                if (productQty == 0) {
                    Toast.makeText(context, "Product not available ", Toast.LENGTH_SHORT).show();
                } else {
                    final ContentValues contentValues = new ContentValues();
                    contentValues.put(ProductContract.ProductsEntry.PRODUCTNAME, productName);
                    contentValues.put(ProductContract.ProductsEntry.PRICE, productPrice);
                    contentValues.put(ProductContract.ProductsEntry.QUANTITY, productQty - 1);
                    final String selection = ProductContract.ProductsEntry._ID + "=?";
                    final String[] selectionArgs = new String[]{productID + ""};

                    context.getContentResolver().update(uri, contentValues, selection, selectionArgs);
                }
            }
        });
     }
}
