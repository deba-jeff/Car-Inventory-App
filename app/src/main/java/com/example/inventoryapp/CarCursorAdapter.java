package com.example.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.inventoryapp.data.CarContract.CarEntry;


public class CarCursorAdapter extends CursorAdapter {

    public CarCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ImageView carImageView = (ImageView) view.findViewById(R.id.list_item_car_image_imageView);
        TextView carNameTextView = (TextView) view.findViewById(R.id.car_name_textView);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_textView);
        final TextView stockTextView = (TextView) view.findViewById(R.id.stock_textView);
        Button saleButton = (Button) view.findViewById(R.id.sale_decrement_button);

        int carImageColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_IMAGE);
        int carNameColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_NAME);
        int priceColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_PRICE);
        int stockColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_STOCK);
        int carIdColumnIndex = cursor.getColumnIndex(CarEntry._ID);

        String currentCarImageString = cursor.getString(carImageColumnIndex);
        String currentCarName = cursor.getString(carNameColumnIndex);
        int currentPrice = cursor.getInt(priceColumnIndex);
        final int currentStock = cursor.getInt(stockColumnIndex);
        final int currentCarId = cursor.getInt(carIdColumnIndex);

        carImageView.setImageURI(Uri.parse(currentCarImageString));
        carNameTextView.setText(currentCarName);
        priceTextView.setText("Price: $" +    String.valueOf(currentPrice));
        stockTextView.setText("Stock: " +  String.valueOf(currentStock));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get Uri of car to be updated
                Uri currentCarUri = ContentUris.withAppendedId(CarEntry.CONTENT_URI, currentCarId);

                // Decrement available stock
                decrementSale(context, currentStock, currentCarUri);
            }
        });

    }


    /**
     * Decrement stock currently available
     */
    private void decrementSale(Context context, int stock , Uri carUri){

        // Check for available stock
        if (stock > 0){
            stock = stock - 1;
            ContentValues values = new ContentValues();
            values.put(CarEntry.COLUMN_CAR_STOCK, stock);
            int rowsUpdated = context.getContentResolver().update(carUri, values, null, null);

            // If 1 or more rows were updated, display Car Sold
            if (rowsUpdated > 0){
                Toast.makeText(context,"Car sold", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        }

        else {
            Toast.makeText(context, "Out of Stock", Toast.LENGTH_SHORT).show();
        }
    }


}
