package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;


public class ProductCursorAdapter extends CursorAdapter {

    Context mContext;
    Cursor mCursor;

    int newQuant;
    int newSales;
    int rowsAffected;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /*Makes a new blank list item view. No data is set (or bound) to the views yet.*/
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) { // why newView? we had usually getView here?

        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.product_list_item, parent, false);

    }

    /* This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.*/
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {


        mContext = context;
        mCursor = cursor;

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.product_price);
        final TextView quantTextView = (TextView) view.findViewById(R.id.quantity_value);
        final TextView salesTextView = (TextView) view.findViewById(R.id.sales_value);
        ImageView imgImageView = (ImageView) view.findViewById(R.id.product_thumbnail);

        // Find the columns of attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int quantColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
        int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SALES);
        int imgColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PIC);

        // Find the columns of attributes that we're interested in
        int itemIdIndex = cursor.getColumnIndex(ProductEntry._ID);


        // Read the attributes from the Cursor for the current product
        final int itemId = cursor.getInt(itemIdIndex);

        // Read the attributes from the Cursor for the current product
        String name = cursor.getString(nameColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        int quant = cursor.getInt(quantColumnIndex);
        int sales = cursor.getInt(salesColumnIndex);
        byte[] imgProductArray = cursor.getBlob(imgColumnIndex);
        if (imgProductArray != null) {
            Bitmap bmpProduct = BitmapFactory.decodeByteArray(imgProductArray, 0, imgProductArray.length);
            imgImageView.setImageBitmap(bmpProduct);
        }

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(name);
        priceTextView.setText(String.valueOf(price));
        quantTextView.setText(String.valueOf(quant));
        salesTextView.setText(String.valueOf(sales));

        TextView sale1Btn = (TextView) view.findViewById(R.id.button_sale1);
        sale1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //on click, update the current product
                rowsAffected = saleOfAproduct(itemId, quantTextView, salesTextView);
                if (rowsAffected != 0) {
                    quantTextView.setText(String.valueOf(newQuant));
                    salesTextView.setText(String.valueOf(newSales));
                } else {
                    Toast.makeText(mContext, "Not possible to update.", Toast.LENGTH_SHORT).show();

                }

            }


        });


    }

    public int saleOfAproduct(int rowId, TextView qTextView, TextView sTextView) {
        mCursor.moveToPosition(rowId);
        int oldSales = Integer.parseInt(sTextView.getText().toString());
        int oldQuant = Integer.parseInt(qTextView.getText().toString());

        if (oldQuant > 0) {
            newQuant = oldQuant - 1;
            newSales = oldSales + 1;

            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_QUANTITY, String.valueOf(newQuant));
            values.put(ProductEntry.COLUMN_SALES, String.valueOf(newSales));
            Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, rowId);

            rowsAffected = mContext.getContentResolver().update(currentProductUri, values, null, null);
        }
        return rowsAffected;

    }
}


