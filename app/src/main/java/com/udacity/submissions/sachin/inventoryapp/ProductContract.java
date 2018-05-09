package com.udacity.submissions.sachin.inventoryapp;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Sachin on 2018-04-25.
 */

public class ProductContract {

    public static final String CONTENT_AUTHORITY = "com.udacity.submissions.sachin.inventoryapp.products";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

    //An empty private constructor makes sure that the class is not going to be initialised.
    private ProductContract() {
    };

    public static final class ProductsEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        public static final String TABLE_NAME = "products";
        public static final String _ID = BaseColumns._ID;
        public static final String PRODUCTNAME = "product_name";
        public static final String PRICE = "price";
        public static final String QUANTITY = "quantity";
        public static final String PRODUCTIMAGE = "product_image";
    }
}
