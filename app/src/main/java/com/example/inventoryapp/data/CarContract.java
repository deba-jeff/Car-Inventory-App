package com.example.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public final class CarContract {

    // This prevent someone from accidentally instantiating the contract class,
    // so it is given an empty constructor
    private CarContract(){

    }

    public static final String CONTENT_AUTHORITY = "com.example.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CARS = "cars";

    public static final class CarEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CARS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CARS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CARS;

        public final static String TABLE_NAME = "cars";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_CAR_IMAGE = "image";
        public final static String COLUMN_CAR_NAME = "product_name";
        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";
        public final static String COLUMN_SUPPLIER_EMAIL = "supplier_email";
        public final static String COLUMN_CAR_PRICE = "price";
        public final static String COLUMN_CAR_STOCK = "stock";
    }


}
