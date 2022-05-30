package com.example.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import com.example.inventoryapp.data.CarContract.CarEntry;


public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Create a ContentValues object called "onActivityResultImageStringValues" that would hold the onActivityResult image String
    ContentValues onActivityResultImageStringValues = new ContentValues();

    // Create a ContentValues object called "onLoadFinishedImageStringValues" that would hold the onLoadFinished image String
    ContentValues onLoadFinishedImageStringValues = new ContentValues();

    // Set Uri as a global variable to be used in onLoadFinished and EditCar mode in saveCar method
    Uri currentCarImageUri;

    private static final int IMAGE_REQUEST_CODE = 1000;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 2000;

    // Loader ID
    private static final int EXISTING_CAR_LOADER_ID = 1;

    // Content URI for the existing car (null if it's a new car)
    private Uri mCurrentCarUri;

    // URI for the car image picked from the phone gallery
    private Uri mImageUri;

    private ImageView mCarImageView;
    private EditText mCarNameEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierEmail;
    private EditText mPrice;
    private ImageView mDecrementButtonImageView;
    private EditText mStock_available;
    private ImageView mIncrementButtonImageView;
    private Button mOrderMoreButton;

    // Stock available
    int carStock;

    // Boolean flag that keeps track of whether the car has been edited (true) or not (false)
    private boolean mCarHasChanged = false;


    /**
     * Listen for touch on a View, implying that the View is being modified
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mCarHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mCarImageView = (ImageView) findViewById(R.id.details_activity_car_image_imageView);
        mCarNameEditText = (EditText) findViewById(R.id.car_name_editText);
        mSupplierNameEditText = (EditText) findViewById(R.id.supplier_name_editText);
        mSupplierEmail = (EditText) findViewById(R.id.supplier_email_editText);
        mPrice = (EditText) findViewById(R.id.price_editText);
        mDecrementButtonImageView = (ImageView) findViewById(R.id.decrement_button_imageView);
        mStock_available = (EditText) findViewById(R.id.stock_available);
        mIncrementButtonImageView = (ImageView) findViewById(R.id.increment_button_imageView);
        mOrderMoreButton = (Button) findViewById(R.id.order_more_button);

        // Set the minimum and maximum value for the Stock EditText
        mStock_available.setFilters(new InputFilter[]{new InputFilterMinMax("0", "1000")});

        // Setup OnTouchListeners on all fields (EditText and ImageView)
        mCarImageView.setOnTouchListener(mTouchListener);
        mCarNameEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEmail.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mStock_available.setOnTouchListener(mTouchListener);
        mDecrementButtonImageView.setOnTouchListener(mTouchListener);
        mIncrementButtonImageView.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();

        // Get data(URI) from OverviewActivity
        mCurrentCarUri = intent.getData();

        // Check if in Add Car or Edit Car mode
        if (mCurrentCarUri == null) {
            setTitle("Add a Car");
            mOrderMoreButton.setVisibility(View.GONE);
            invalidateOptionsMenu();
            Uri imageUri = Uri.parse("android.resource://com.example.inventoryapp/drawable/add_a_photo");

            // Set generic image on the ImageView
            mCarImageView.setImageURI(imageUri);
        }
        else {
            setTitle("Edit Car");

            // Kick off the loader to read the car data from the database
            getLoaderManager().restartLoader(EXISTING_CAR_LOADER_ID, null, this);
        }

        mOrderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String supplierEmail = mSupplierEmail.getText().toString().trim();
                String carName = mCarNameEditText.getText().toString().trim();

                // Set up Intent
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{supplierEmail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Order for " + carName);
                intent.putExtra(Intent.EXTRA_TEXT, "We want to make more orders for " + carName +
                        " to increase our stock");

                try {
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(intent, "Choose Email app :"));
                        finish();
                    }
                    else {
                        Toast.makeText(DetailsActivity.this, "No email app installed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (android.content.ActivityNotFoundException e){
                    Toast.makeText(DetailsActivity.this, "No email app installed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Request runtime permission on Android api 23 and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    // Check for External storage permission
                    if (ContextCompat.checkSelfPermission(DetailsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {

                        // Request for permission
                        ActivityCompat.requestPermissions(DetailsActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
                    }
                    else {
                        intentToSelectImage();
                    }
                }
                else {
                    intentToSelectImage();
                }
            }
        });

    }


    /**
     * Pick image from gallery
     */
    private void intentToSelectImage() {
        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);
    }


    /**
     * Handle result of runtime permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            // Check for request code match
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:

                // Check for granted permission
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    intentToSelectImage();
                }
                else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * Handle result of image Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check for request and result code match
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // Check for error in picking image
            if (data != null) {
                mImageUri = data.getData();
                String imageUriString = null;
                if (mImageUri != null) {
                    imageUriString = mImageUri.toString();
                }
                mCarImageView.setImageURI(Uri.parse(imageUriString));

                // Pass imageUriString String to onActivityResultImageStringValues ContentValues to be used for validation
                // when saving image
                onActivityResultImageStringValues.put("onActivityResultImageString", imageUriString);
            }
            else {
                Toast.makeText(this, "Error while picking image", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }



    /**
     *  Insert or Update Car
     */
    private void saveCar() {
        // Check if in Add Car or Edit Car mode
        if (mCurrentCarUri == null) {
            ContentValues insertValues = new ContentValues();
            String carNameString = mCarNameEditText.getText().toString().trim();
            String supplierNameString = mSupplierNameEditText.getText().toString().trim();
            String supplierEmailString = mSupplierEmail.getText().toString().trim();
            String priceString = mPrice.getText().toString().trim();
            String stockString = mStock_available.getText().toString().trim();

            if (mImageUri == null && TextUtils.isEmpty(carNameString) && TextUtils.isEmpty(supplierNameString) &&
                    TextUtils.isEmpty(supplierEmailString) && TextUtils.isEmpty(priceString) &&
                    TextUtils.isEmpty(stockString)) {
                return;
            }
            if (mImageUri == null ) {
                Toast.makeText(this, "Image of Car is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // If image selected is valid, get imageString from onActivityResultImageStringValues ContentValues
            // in onActivityResult and pass to current ContentValues
            String externalStorageImageString = onActivityResultImageStringValues.getAsString("onActivityResultImageString");
            insertValues.put(CarEntry.COLUMN_CAR_IMAGE, externalStorageImageString);

            if (TextUtils.isEmpty(carNameString)) {
                Toast.makeText(this, "Name of Car is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(supplierNameString)) {
                Toast.makeText(this, "Name of Supplier is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(priceString)) {
                Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
                return;
            }
            int price = Integer.parseInt(priceString);

            if (TextUtils.isEmpty(stockString)) {
                Toast.makeText(this, "Stock available is required", Toast.LENGTH_SHORT).show();
                return;
            }
            int stock = Integer.parseInt(stockString);

            // Insert remaining attributes
            insertValues.put(CarEntry.COLUMN_CAR_NAME, carNameString);
            insertValues.put(CarEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
            insertValues.put(CarEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);
            insertValues.put(CarEntry.COLUMN_CAR_PRICE, price);
            insertValues.put(CarEntry.COLUMN_CAR_STOCK, stock);

            // Insert into database
            Uri newUri = getContentResolver().insert(CarEntry.CONTENT_URI, insertValues);

            // Check for error in inserting car
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.details_insert_car_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.details_insert_car_successful), Toast.LENGTH_SHORT).show();
            }
        }

        // If in Edit Car mode proceed to else statement
        else {
            ContentValues updateValues = new ContentValues();
            String carNameString = mCarNameEditText.getText().toString().trim();
            String supplierNameString = mSupplierNameEditText.getText().toString().trim();
            String supplierEmailString = mSupplierEmail.getText().toString().trim();
            String priceString = mPrice.getText().toString().trim();
            String stockString = mStock_available.getText().toString().trim();

            if (mImageUri == null && TextUtils.isEmpty(carNameString) && TextUtils.isEmpty(supplierNameString) &&
                    TextUtils.isEmpty(supplierEmailString) && TextUtils.isEmpty(priceString) &&
                    TextUtils.isEmpty(stockString)) {
                return;
            }

            // Check for invalid image from onLoadFinished and onActivityResult respectively
            if (currentCarImageUri == null && mImageUri == null) {
                Toast.makeText(this, "Image of Car is required", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (currentCarImageUri == null && mImageUri != null) {

                // Get imageString from onActivityResultImageStringValues ContentValues
                // in onActivityResult and pass to current ContentValues
                String externalStorageImageString = onActivityResultImageStringValues.getAsString("onActivityResultImageString");
                updateValues.put(CarEntry.COLUMN_CAR_IMAGE, externalStorageImageString);
            }
            else if (currentCarImageUri != null && mImageUri == null) {

                // Get imageString from onLoadFinishedImageStringValues ContentValues
                // in onLoadFinished and pass to current ContentValues
                String onLoadFinishedImageString = onLoadFinishedImageStringValues.getAsString("onLoadFinishedImageString");
                updateValues.put(CarEntry.COLUMN_CAR_IMAGE, onLoadFinishedImageString);
            }
            else if (currentCarImageUri != null && mImageUri != null) {

                // Get imageString from onActivityResultImageStringValues ContentValues
                // in onActivityResult and pass to current ContentValues
                String externalStorageImageString = onActivityResultImageStringValues.getAsString("onActivityResultImageString");
                updateValues.put(CarEntry.COLUMN_CAR_IMAGE, externalStorageImageString);
            }

            if (TextUtils.isEmpty(carNameString)) {
                Toast.makeText(this, "Name of Car is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(supplierNameString)) {
                Toast.makeText(this, "Name of Supplier is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(priceString)) {
                Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
                return;
            }
            int price = Integer.parseInt(priceString);

            if (TextUtils.isEmpty(stockString)) {
                Toast.makeText(this, "Stock available is required", Toast.LENGTH_SHORT).show();
                return;
            }
            int stock = Integer.parseInt(stockString);

            // Update remaining attributes
            updateValues.put(CarEntry.COLUMN_CAR_NAME, carNameString);
            updateValues.put(CarEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
            updateValues.put(CarEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);
            updateValues.put(CarEntry.COLUMN_CAR_PRICE, price);
            updateValues.put(CarEntry.COLUMN_CAR_STOCK, stock);

            // Update database
            int rowsAffected = getContentResolver().update(mCurrentCarUri, updateValues, null, null);

            // Check for error in updating car
            if (rowsAffected == 0) {
                Toast.makeText(this, "Error with updating car", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Car updated", Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Check if in Add Car or Edit Car mode
        if (mCurrentCarUri == null) {

            // Hide "delete" menuitem if in Add Car mode
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveCar();
                finish();
                return true;

            case R.id.action_delete:
                deleteSingleCarDialog("Delete this Car?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteSingleCar();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });
                return true;

            case android.R.id.home:

                // Check for touch event on the fields
                if (!mCarHasChanged) {

                    // Navigate away if touch is not detected
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }

                // Show dialog if touch is detected on fields
                DialogInterface.OnClickListener rightButtonAlertDialogListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    }
                };
                upButtonDialogMessage(rightButtonAlertDialogListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when the Activity has detected the user's press of the back key.
     */
    @Override
    public void onBackPressed() {

        // Check for touch event on the fields
        if (!mCarHasChanged) {

            // Navigate away if touch is not detected
            super.onBackPressed();
            return;
        }

        // Show dialog if touch is detected on fields
        backButtonDialogMessage();
        return;
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor
     */
    private void backButtonDialogMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard Changes");
        builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });

        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //  Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                CarEntry._ID,
                CarEntry.COLUMN_CAR_IMAGE,
                CarEntry.COLUMN_CAR_NAME,
                CarEntry.COLUMN_SUPPLIER_NAME,
                CarEntry.COLUMN_SUPPLIER_EMAIL,
                CarEntry.COLUMN_CAR_PRICE,
                CarEntry.COLUMN_CAR_STOCK};

        // Execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentCarUri,                // Query the content URI for the current car clicked
                projection,                    // Columns to include in the resulting Cursor
                null,                          // No selection clause
                null,                          // No selection arguments
                null);                         // Default sort order

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Move to the first row of the cursor and read data from it
        if (cursor.moveToNext()) {

            int carImageColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_IMAGE);
            int carNameColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_NAME);
            int supplierNameColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_SUPPLIER_EMAIL);
            int carPriceColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_PRICE);
            int carStockColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_STOCK);

            String currentCarImageString = cursor.getString(carImageColumnIndex);

            // Pass currentCarImageString String to onLoadFinishedImageStringValues ContentValues
            // to be used for validation when saving image
            onLoadFinishedImageStringValues.put("onLoadFinishedImageString", currentCarImageString);
            currentCarImageUri = Uri.parse(currentCarImageString);

            String currentCarName = cursor.getString(carNameColumnIndex);
            String currentSupplierName = cursor.getString(supplierNameColumnIndex);
            String currentSupplierEmail = cursor.getString(supplierEmailColumnIndex);
            int currentCarPrice = cursor.getInt(carPriceColumnIndex);
            int currentCarStock = cursor.getInt(carStockColumnIndex);

            mCarImageView.setImageURI(currentCarImageUri);
            mCarNameEditText.setText(currentCarName);
            mSupplierNameEditText.setText(currentSupplierName);
            mSupplierEmail.setText(currentSupplierEmail);
            mPrice.setText(String.valueOf(currentCarPrice));
            mStock_available.setText(String.valueOf(currentCarStock));
        }
    }



    /**
     * Clear the old data and remove any references to it
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCarNameEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierEmail.setText("");
        mCarImageView.setImageURI(null);
        mPrice.setText("");
        mStock_available.setText("");
    }



    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor
     */
    private void upButtonDialogMessage(DialogInterface.OnClickListener rightButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard Changes?");
        builder.setPositiveButton("Discard", rightButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Prompt the user to confirm that they want to delete the current car from the database
     */
    private void deleteSingleCarDialog(String message, DialogInterface.OnClickListener positiveButtonListener,
                                       DialogInterface.OnClickListener negativeButtonListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("Delete", positiveButtonListener);
        builder.setNegativeButton("Cancel", negativeButtonListener);

        // Create and show the AlertDialog
        builder.create();
        builder.show();
    }


    /**
     * Delete a single car from database
     */
    private void deleteSingleCar() {

        // Check if in Add Car or Edit Car mode
        if (mCurrentCarUri != null) {

            // Proceed to delete if in Edit Car mode
            int rowsDeleted = getContentResolver().delete(mCurrentCarUri, null, null);

            // Check for error in deleting car
            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error with deleting car", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Car deleted", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }


    /**
     * Increment stock currently available
     */
    public void incrementStock(View view) {

        // Get current value of stock
        String stockString = mStock_available.getText().toString().trim();
        if (stockString.equals("") || TextUtils.isEmpty(stockString)){
            Toast.makeText(this, "Stock cannot have an empty value", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            carStock = Integer.parseInt(stockString);
            carStock = carStock + 1;
        }

        if (carStock > 1000){
            Toast.makeText(this, "You cannot have a stock above 1000", Toast.LENGTH_SHORT).show();
            return;
        }
        String newCurrentCarStock = String.valueOf(carStock);
        mStock_available.setText(newCurrentCarStock);
    }


    /**
     * Decrement stock currently available
     */
    public void decrementStock(View view) {

        // Get current value of stock
        String stockString = mStock_available.getText().toString().trim();
        if (stockString.equals("") || TextUtils.isEmpty(stockString)){
            Toast.makeText(this, "Stock cannot have an empty value", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            carStock = Integer.parseInt(stockString);
            carStock = carStock - 1;
        }

        if (carStock < 0){
            Toast.makeText(this, "Stock cannot have a negative value", Toast.LENGTH_SHORT).show();
            return;
        }
        String newCurrentCarStock = String.valueOf(carStock);
        mStock_available.setText(newCurrentCarStock);
    }


}