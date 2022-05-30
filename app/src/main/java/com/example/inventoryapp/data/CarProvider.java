package com.example.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.inventoryapp.data.CarContract.CarEntry;



public class CarProvider extends ContentProvider{

    /** Tag for the log messages */
    public static final String LOG_TAG = CarProvider.class.getSimpleName();

    private CarDBHelper mDBHelpler;

    // Integer code returned for URI request for the entire cars table
    private static final int CARS = 100;

    // Integer code returned for URI request for a single row in the cars table
    private static final int CAR_ID = 101;

    // The URI Matcher used by this Content provider.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // First URI pattern ContentProvider accepts
        // This corresponds to the entire database table
        sUriMatcher.addURI(CarContract.CONTENT_AUTHORITY, CarContract.PATH_CARS, CARS);

        // Second URI pattern ContentProvider accepts
        // This corresponds to a single row
        sUriMatcher.addURI(CarContract.CONTENT_AUTHORITY, CarContract.PATH_CARS + "/#", CAR_ID);
    }



    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDBHelpler = new CarDBHelper(getContext());
        return true;
    }


    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDBHelpler.getReadableDatabase();

        // Hold the result of the query
        Cursor cursor;

        // Match the URI and returns a code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CARS:
                // Query the entire cars table with the given
                // projection, selection, selection arguments, and sort order
                cursor = db.query(CarEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case CAR_ID:
                selection = CarEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // Query the row with the given ID from the selection and selectionArgs
                cursor =  db.query(CarEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Notify the listener attached to this resolver of a change
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CARS:
                return CarEntry.CONTENT_LIST_TYPE;
            case CAR_ID:
                return CarEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }

    }


    /**
     * Insert new data into the provider with the given ContentValues
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        // Match the URI and returns a code
        final int match = sUriMatcher.match(uri);
        switch (match){
            case CARS:
                // Insert a car into the database table with the given ContentValues
                return insertCar(uri, values);

            default:
                throw new IllegalArgumentException("Insertion is not supported for" + uri);
        }

    }


    /**
     * Insert a car into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertCar (Uri uri, ContentValues values){

        // Perform sanity checks
        String carName = values.getAsString(CarEntry.COLUMN_CAR_NAME);
        if (carName == null){
            throw new IllegalArgumentException("Car requires a name");
        }

        String supplierName = values.getAsString(CarEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null){
            throw new IllegalArgumentException("Car requires a Supplier name");
        }

        String supplierEmail = values.getAsString(CarEntry.COLUMN_SUPPLIER_EMAIL);
        if (supplierEmail == null){
            throw new IllegalArgumentException("Car requires a Supplier Email");
        }

        Integer carPrice = values.getAsInteger(CarEntry.COLUMN_CAR_PRICE);
        if (carPrice == null || carPrice < 0){
            throw new IllegalArgumentException("Car requires a valid price");
        }

        Integer carStock = values.getAsInteger(CarEntry.COLUMN_CAR_STOCK);
        if (carStock == null || carStock < 0 ){
            throw new IllegalArgumentException("Car requires a valid stock");
        }

        String carImage = values.getAsString(CarEntry.COLUMN_CAR_IMAGE);
        if (carImage == null ){
            throw new IllegalArgumentException("Car requires a valid image");
        }

        // Proceed with calling the database
        // Create and/or open a database to write to it
        SQLiteDatabase db = mDBHelpler.getWritableDatabase();

        // Insert a car into the database table with the given ContentValues
        Long newRowId = db.insert(CarEntry.TABLE_NAME, null, values);

        // Check for error in inserting
        if (newRowId == -1){
            Log.e(LOG_TAG, "Failed to insert row for" +  uri);
            return null;
        }

        // Notify the listener attached to this resolver of a change on the car content URI
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, newRowId);
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Create and/or open a database to write to it
        SQLiteDatabase database = mDBHelpler.getWritableDatabase();
        int rowsDeleted;

        // Match the URI and returns a code
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CARS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(CarEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case CAR_ID:
                selection = CarEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete(CarEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted,
        // notify all listeners attached to this resolver of a change at the given URI
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    /**
     * Update the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Match the URI and returns a code
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CARS:
                return updateCar(uri, values, selection, selectionArgs);

            case CAR_ID:
                selection = CarEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Update the row with the given ID from the selection and selectionArgs
                // with the given content values
                return updateCar(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }


    /**
     * Update the row with the given ID from the selection and selectionArgs
     * with the given content values
     */
    private int updateCar(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        // Confirm if attribute was updated and perform sanity checks
        if (values.containsKey(CarEntry.COLUMN_CAR_NAME)){
            String carName = values.getAsString(CarEntry.COLUMN_CAR_NAME);
            if (carName == null){
                throw new IllegalArgumentException("Car requires a name");
            }
        }

        if (values.containsKey(CarEntry.COLUMN_SUPPLIER_NAME)){
            String supplierName = values.getAsString(CarEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null){
                throw new IllegalArgumentException("Car requires a Supplier name");
            }
        }

        if (values.containsKey(CarEntry.COLUMN_SUPPLIER_EMAIL)){
            String supplierEmail = values.getAsString(CarEntry.COLUMN_SUPPLIER_EMAIL);
            if (supplierEmail == null){
                throw new IllegalArgumentException("Car requires a Supplier Email");
            }
        }

        if (values.containsKey(CarEntry.COLUMN_CAR_PRICE)){
            Integer carPrice = values.getAsInteger(CarEntry.COLUMN_CAR_PRICE);
            if (carPrice == null || carPrice < 0){
                throw new IllegalArgumentException("Car requires a valid price");
            }
        }

        if (values.containsKey(CarEntry.COLUMN_CAR_STOCK)){
            Integer carStock = values.getAsInteger(CarEntry.COLUMN_CAR_STOCK);
            if (carStock == null || carStock < 0 ){
                throw new IllegalArgumentException("Car requires a valid stock");
            }
        }

        if (values.containsKey(CarEntry.COLUMN_CAR_IMAGE)){
            String carImage = values.getAsString(CarEntry.COLUMN_CAR_IMAGE);
            if (carImage == null){
                throw new IllegalArgumentException("Car requires a valid image");
            }
        }

        // Check if any attribute was updated
        if (values.size() == 0){
            return 0;
        }

        // Proceed with calling the database
        // Create and/or open a database to write to it
        SQLiteDatabase database = mDBHelpler.getWritableDatabase();
        int rowsUpdated = database.update(CarEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated,
        // notify all listeners attached to this resolver of a change at the given URI
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

}
