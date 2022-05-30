package com.example.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.example.inventoryapp.data.CarContract.CarEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;


public class OverviewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Loader ID
    private static final int CAR_LOADER_ID = 0;
    CarCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        FloatingActionButton r = (FloatingActionButton) findViewById(R.id.fab);
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OverviewActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });
        ListView carListView = (ListView) findViewById(R.id.list_view_cars);
        View emptyView = findViewById(R.id.empty_view);
        // Set emptyView to be displayed
        carListView.setEmptyView(emptyView);

        mCursorAdapter = new CarCursorAdapter(this, null);
        carListView.setAdapter(mCursorAdapter);
        carListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Create new intent to go to DetailsActivity
                Intent intent = new Intent(OverviewActivity.this, DetailsActivity.class);

                // Form the content URI that represents the specific car clicked on,
                // by appending the id onto the CarEntry.CONTENT_URI
                Uri currentCarUri = ContentUris.withAppendedId(CarEntry.CONTENT_URI, id);

                // Pass the URI to the data field of the intent
                intent.setData(currentCarUri);

                // Launch the DetailsActivity
                startActivity(intent);
            }
        });
        // Kick off the Loader
        getLoaderManager().initLoader(CAR_LOADER_ID, null, this);
    }


    /**
     * Insert dummy data to the database
     */
    private void insertCar(){
        Uri imageUri2 = (new Uri.Builder())
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(getResources().getResourcePackageName(R.drawable.lamborghini_venonan))
                .appendPath(getResources().getResourceTypeName(R.drawable.lamborghini_venonan))
                .appendPath(getResources().getResourceEntryName(R.drawable.lamborghini_venonan))
                .build();
        String carImage = String.valueOf(imageUri2);

        ContentValues values = new ContentValues();
        values.put(CarEntry.COLUMN_CAR_IMAGE, carImage);
        values.put(CarEntry.COLUMN_CAR_NAME, "2015 Lamborghini Venona Roadster");
        values.put(CarEntry.COLUMN_SUPPLIER_NAME, "JD Automobile");
        values.put(CarEntry.COLUMN_SUPPLIER_EMAIL, "JD@gmail.com");
        values.put(CarEntry.COLUMN_CAR_PRICE, 4500000);
        values.put(CarEntry.COLUMN_CAR_STOCK, 2);
        Uri newUri = getContentResolver().insert(CarEntry.CONTENT_URI, values);
    }


    /**
     * Delete all Cars from database
     */
    private void deleteAllCars(){
        int rowsDeleted = getContentResolver().delete(CarEntry.CONTENT_URI, null, null);

        // Check for error in deleting cars
        if (rowsDeleted == 0){
            Toast.makeText(this, "Error with Deleting Cars", Toast.LENGTH_SHORT).show();
        }
        else {
            // If 1 or more rows were deleted,
            // notify all listeners attached to this resolver of a change at the given URI
            if (rowsDeleted != 0) {
                this.getContentResolver().notifyChange(CarEntry.CONTENT_URI, null);
            }
            Toast.makeText(this, "Cars deleted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_insert_dummy_data:
                insertCar();
                return true;

            case R.id.action_delete_all_cars:
                deleteAllCarsDialog("Do you want to delete all Cars?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllCars();
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //  Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                CarEntry._ID,
                CarEntry.COLUMN_CAR_IMAGE,
                CarEntry.COLUMN_CAR_NAME,
                CarEntry.COLUMN_CAR_PRICE,
                CarEntry.COLUMN_CAR_STOCK };

        // Execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,     // Parent activity context
                CarEntry.CONTENT_URI,            // Provider content URI to query
                projection,                      // Columns to include in the resulting Cursor
                null,                    // No selection clause
                null,                 // No selection arguments
                null );                  // Default sort order
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {

            // Update CarCursorAdapter with this new cursor containing updated car data
            mCursorAdapter.swapCursor(data);
        }
        else if (data != null && data.getCount() == 0){

            // Clear the old data
            mCursorAdapter.swapCursor(null);
        }
    }


    /**
     * Clear the old data and remove any references to it
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


    /**
     * Prompt the user to confirm that they want to delete all Cars from the database
     */
    private void deleteAllCarsDialog(String message,DialogInterface.OnClickListener positiveButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("Delete" ,positiveButtonListener);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        builder.create();
        builder.show();
    }

}